package de.cubeisland.cubeengine.core.bukkit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CoreConfiguration;
import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.command.commands.CoreCommands;
import de.cubeisland.cubeengine.core.command.commands.ModuleCommands;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.module.event.FinishedLoadModulesEvent;
import de.cubeisland.cubeengine.core.permission.PermissionManager;
import de.cubeisland.cubeengine.core.storage.TableManager;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseFactory;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.log.CubeFileHandler;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.core.util.worker.CubeThreadFactory;
import de.cubeisland.cubeengine.core.webapi.ApiConfig;
import de.cubeisland.cubeengine.core.webapi.ApiServer;
import de.cubeisland.cubeengine.core.webapi.exception.ApiStartupException;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static de.cubeisland.cubeengine.core.util.log.LogLevel.ALL;
import static de.cubeisland.cubeengine.core.util.log.LogLevel.ERROR;

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
    private CommandManager commandManager;
    private TaskManager taskManager;
    private TableManager tableManager;
    private ObjectMapper jsonObjectMapper;
    private ApiServer apiServer;
    private WorldManager worldManager;

    @Override
    public void onEnable()
    {
        if (!BukkitUtils.isCompatible())
        {
            this.getLogger().log(ERROR, "Your Bukkit server is incompatible with this CubeEngine revision.");
            return;
        }
        CubeEngine.initialize(this);

        this.jsonObjectMapper = new ObjectMapper();
        this.jsonObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        final Server server = this.getServer();
        final PluginManager pm = server.getPluginManager();

        this.logger = new CubeLogger("Core", this.getLogger());
        // TODO RemoteHandler is not yet implemented this.logger.addHandler(new RemoteHandler(LogLevelERROR, this));

        try
        {
            this.fileManager = new FileManager(this.getDataFolder().getAbsoluteFile());
        }
        catch (IOException e)
        {
            this.logger.log(ERROR, "Failed to initialize the FileManager", e);
            pm.disablePlugin(this);
            return;
        }
        this.fileManager.clearTempDir();
        this.fileManager.dropResources(CoreResource.values());

        try
        {
            // depends on: file manager
            this.logger.addHandler(new CubeFileHandler(ALL, new File(this.fileManager.getLogDir(), "core").toString()));
        }
        catch (IOException e)
        {
            this.logger.log(ERROR, e.getLocalizedMessage(), e);
        }

        // depends on: file manager
        this.config = Configuration.load(CoreConfiguration.class, new File(this.fileManager.getDataFolder(), "core.yml"));

        CubeLogger.setLoggingLevel(this.config.loggingLevel);
        if (!this.config.logCommands)
        {
            BukkitUtils.disableCommandLogging();
        }

        // depends on: object mapper
        this.apiServer = new ApiServer(this);
        this.apiServer.configure(Configuration.load(ApiConfig.class, new File(this.fileManager.getDataFolder(), "webapi.yml")));

        // depends on: core config, server
        this.taskManager = new TaskManager(this, new CubeThreadFactory("CubeEngine"), config.executorThreads, this.getServer().getScheduler());

        if (this.config.userWebapi)
        {
            try
            {
                this.apiServer.start();
            }
            catch (ApiStartupException e)
            {
                this.logger.log(ERROR, "The web API will not be available as the server failed to start properly...", e);
            }
        }

        // depends on: core config, file manager, task manager
        this.database = DatabaseFactory.loadDatabase(this.config.database, new File(fileManager.getDataFolder(), "database.yml"));
        if (this.database == null)
        {
            this.logger.log(ERROR, "Could not connect to the database type ''{0}''", this.config.database);
            pm.disablePlugin(this);
            return;
        }
        // depends on: database
        this.tableManager = new TableManager(this);

        // depends on: plugin manager
        this.permissionRegistration = new BukkitPermissionManager(this);

        // depends on: plugin manager
        this.eventRegistration = new EventManager(this);

        // depends on: executor, database, Server, core config and event registration
        this.userManager = new UserManager(this);

        // register listeners for UserManger
        pm.registerEvents(this.userManager, this);

        pm.registerEvents(new CoreListener(this), this);

        // depends on: file manager, core config
        this.i18n = new I18n(this);

        // depends on: server
        this.commandManager = new CommandManager(this);

        // depends on: database
        this.moduleManager = new BukkitModuleManager(this);

        // depends on: server, module manager
        this.commandManager.registerCommand(new ModuleCommands(this.moduleManager));
        this.commandManager.registerCommand(new CoreCommands(this));

        // depends on: server
        BukkitUtils.registerPacketHookInjector(this);

        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                // depends on loaded worlds
                worldManager = new WorldManager(database);

                // depends on: file manager
                moduleManager.loadModules(fileManager.getModulesDir());

                pm.callEvent(new FinishedLoadModulesEvent(BukkitCore.this));

                // depends on: finished loading modules
                userManager.cleanup();
            }
        });
    }

    @Override
    public void onDisable()
    {
        BukkitUtils.cleanup();

        if (this.moduleManager != null)
        {
            this.moduleManager.clean();
            this.moduleManager = null;
        }

        if (this.commandManager != null)
        {
            this.commandManager.unregister();
            this.commandManager = null;
        }

        if (this.apiServer != null)
        {
            this.apiServer.stop();
            this.apiServer.unregisterApiHandlers();
            this.apiServer = null;
        }

        if (this.fileManager != null)
        {
            this.fileManager.clean();
            this.fileManager = null;
        }

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
                this.taskManager.getExecutorService().awaitTermination(this.config.executorTermination, TimeUnit.SECONDS);
                this.taskManager.getExecutorService().shutdownNow();
            }
            catch (InterruptedException ex)
            {
                this.logger.log(ERROR, "Could not execute all pending tasks", ex);
            }
            finally
            {
                this.taskManager = null;
            }
        }

        if (this.database != null)
        {
            this.database.shutdown();
        }

        CubeEngine.clean();
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
        return this.logger.getLevel().intValue() <= LogLevel.DEBUG.intValue();
    }

    @Override
    public ObjectMapper getJsonObjectMapper()
    {
        return this.jsonObjectMapper;
    }

    @Override
    public ApiServer getApiServer()
    {
        return this.apiServer;
    }

    @Override
    public WorldManager getWorldManager()
    {
        return this.worldManager;
    }
}
