package de.cubeisland.cubeengine.core.bukkit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CoreConfiguration;
import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.PermissionManager;
import de.cubeisland.cubeengine.core.storage.TableManager;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseFactory;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import de.cubeisland.cubeengine.core.util.log.FileHandler;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This represents the Bukkit-JavaPlugin that gets loaded and implements the Core
 */
public class BukkitCore extends JavaPlugin implements Core
{
    private boolean debug = true;
    private Database database;
    private PermissionManager permissionRegistration;
    private UserManager userManager;
    private FileManager fileManager;
    private ModuleManager moduleManager;
    private I18n i18n;
    private CoreConfiguration config;
    private CubeLogger logger;
    private EventManager eventRegistration;
    private Server server;
    private CommandManager commandManager;
    private TaskManager taskManager;
    private TableManager tableManager;
    private ObjectMapper jsonObjectMapper;

    @Override
    public void onEnable()
    {
        CubeEngine.initialize(this);

        this.jsonObjectMapper = new ObjectMapper();
        this.jsonObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        this.server = this.getServer();
        PluginManager pm = this.server.getPluginManager();

        this.logger = new CubeLogger("Core", this.getLogger());
        // TODO RemoteHandler is not yet implemented this.logger.addHandler(new RemoteHandler(Level.SEVERE, this));

        try
        {
            this.fileManager = new FileManager(this.getDataFolder().getAbsoluteFile());
        }
        catch (IOException e)
        {
            this.logger.log(Level.SEVERE, "Failed to initialize the FileManager", e);
            pm.disablePlugin(this);
            return;
        }

        this.fileManager.dropResources(CoreResource.values());

        try
        {
            // depends on: file manager
            this.logger.addHandler(new FileHandler(Level.ALL, new File(this.fileManager.getLogDir(), "core").toString()));
        }
        catch (IOException e)
        {
            this.logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

        // depends on: file manager
        this.config = Configuration.load(CoreConfiguration.class, new File(fileManager.getDataFolder(), "core.yml"));

        CubeLogger.setLoggingLevel(this.config.loggingLevel);
        this.debug = this.config.debugMode;

        // depends on: core config and file manager
        this.database = DatabaseFactory.loadDatabase(this.config.database, new File(fileManager.getDataFolder(), "database.yml"));
        if (this.database == null)
        {
            this.logger.log(Level.SEVERE, "Could not connect to the database type ''{0}''", this.config.database);
            pm.disablePlugin(this);
            return;
        }
        // depends on: database
        this.tableManager = new TableManager(this);

        // depends on: plugin manager
        this.permissionRegistration = new PermissionManager(pm);

        // depends on: plugin manager
        this.eventRegistration = new EventManager(this);

        // depends on: core config, server
        this.taskManager = new TaskManager(this, Executors.newScheduledThreadPool(this.config.executorThreads), this.getServer().getScheduler());

        // depends on: executor, database, Server, core config and event registration
        this.userManager = new UserManager(this);

        // register Listerer for UserManger
        pm.registerEvents(this.userManager, this);

        // depends on: file manager, core config
        this.i18n = new I18n(this.fileManager, this.config.defaultLanguage);

        // depends on: Server
        this.commandManager = new CommandManager(this);

        // depends on: database
        this.moduleManager = new ModuleManager(this);

        // depends on: file manager
        this.moduleManager.loadModules(this.fileManager.getModulesDir());

        // depends on: finshed loading modules
        this.getUserManager().cleanup();

        // depends on: server
        BukkitUtils.registerPacketHookInjector(this, pm);
    }

    @Override
    public void onDisable()
    {
        CubeEngine.clean();

        if (this.moduleManager != null)
        {
            this.moduleManager.clean();
            this.moduleManager = null;
        }

        this.fileManager = null;

        if (this.userManager != null)
        {
            this.userManager.clean();
            this.userManager = null;
        }

        this.permissionRegistration = null;

        if (this.i18n != null)
        {
            this.i18n.clean();
            this.i18n = null;
        }
        if (this.taskManager != null)
        {
            try
            {
                this.taskManager.getExecutorService().shutdown();
                this.taskManager.getExecutorService().awaitTermination(config.executorTermination, TimeUnit.SECONDS);
            }
            catch (InterruptedException ex)
            {
                this.logger.log(Level.SEVERE, "Could not execute all pending tasks", ex);
            }
            finally
            {
                this.taskManager = null;
            }
        }
    }

    @Override
    public Database getDB()
    {
        return this.database;
    }

    @Override
    public PermissionManager getPermissionManager()
    {
        return this.permissionRegistration;
    }

    @Override
    public UserManager getUserManager()
    {
        return userManager;
    }

    @Override
    public FileManager getFileManager()
    {
        return this.fileManager;
    }

    @Override
    public ModuleManager getModuleManager()
    {
        return this.moduleManager;
    }

    @Override
    public I18n getI18n()
    {
        return this.i18n;
    }

    @Override
    public CubeLogger getCoreLogger()
    {
        return this.logger;
    }

    @Override
    public EventManager getEventManager()
    {
        return this.eventRegistration;
    }

    @Override
    public CoreConfiguration getConfiguration()
    {
        return this.config;
    }

    @Override
    public CommandManager getCommandManager()
    {
        return this.commandManager;
    }

    @Override
    public TaskManager getTaskManager()
    {
        return taskManager;
    }

    @Override
    public TableManager getTableManger()
    {
        return this.tableManager;
    }

    @Override
    public boolean isDebug()
    {
        return this.debug;
    }

    @Override
    public ObjectMapper getJsonObjectMapper()
    {
        return this.jsonObjectMapper;
    }
}