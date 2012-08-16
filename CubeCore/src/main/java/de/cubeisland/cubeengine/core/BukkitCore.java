package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.BukkitDependend;
import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.event.EventManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.Perm;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.persistence.AttrType;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.log.ConsoleHandler;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import de.cubeisland.cubeengine.core.util.log.DatabaseHandler;
import de.cubeisland.cubeengine.core.util.log.RemoteHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Phillip Schichtel
 */
@BukkitDependend("This is the bukkit plugin")
public class BukkitCore extends JavaPlugin implements Core
{
    private Database database;
    private PermissionRegistration permissionRegistration;
    private UserManager userManager;
    private FileManager fileManager;
    private ModuleManager moduleManager;
    private I18n i18n;
    private CoreConfiguration config;
    private CubeLogger logger;
    private EventManager eventRegistration;
    private Server server;
    private CommandManager commandManager;

    @Override
    public void onEnable()
    {
        CubeEngine.initialize(this);

        this.logger = new CubeLogger("Core");
        this.logger.addHandler(new ConsoleHandler(Level.ALL, "[{0}] {2}"));
        this.logger.addHandler(new RemoteHandler(Level.SEVERE, this));

        this.server = this.getServer();
        PluginManager pm = this.server.getPluginManager();
        this.permissionRegistration = new PermissionRegistration(pm);
        try
        {
            this.fileManager = new FileManager(this.getDataFolder().getParentFile());
        }
        catch (IOException e)
        {
            this.logger.log(Level.SEVERE, "Failed to initialize the FileManager", e);
            pm.disablePlugin(this);
            return;
        }
        try
        {
            this.logger.addHandler(new FileHandler(new File(this.fileManager.getLogDir(), "core.log").toString()));
        }
        catch (IOException e)
        {
            this.logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        this.eventRegistration = new EventManager(pm);
        this.moduleManager = new ModuleManager(this);
        this.commandManager = new CommandManager(this);
        this.config = Configuration.load(CoreConfiguration.class, new File(fileManager.getConfigDir(), "core.yml"));
        this.i18n = new I18n(this, this.config.defaultLanguage);
        try
        {
            DatabaseConfiguration databaseConfig = Configuration.load(DatabaseConfiguration.class, new File(fileManager.getConfigDir(), "database.yml"));
            this.database = new Database(databaseConfig);
        }
        catch (SQLException e)
        {
            this.logger.log(Level.SEVERE, "Error while initializing database", e);
            this.server.getPluginManager().disablePlugin(this);
            return;
        }
        
        
        //TODO remove this
        String query = this.database.buildQuery()
            .createTable("users", true)
                .startFields()
                    .field("id", AttrType.INT, 11, true, true, true)
                    .field("name", AttrType.VARCHAR, 16)
                    .field("lang", AttrType.VARCHAR, 10)
                    .primaryKey("id")
                .endFields()
                .engine("MyISAM")
                .defaultcharset("latin1")
                .autoincrement(1)
            .toString();
        
        System.out.println("#########################################################");
        System.out.println(query);
        
        System.out.println(
            this.database.buildQuery().select("id","item").from("users").where("id").limit(1)
                );
        //TODO remove this
        
        
        this.logger.addHandler(new DatabaseHandler(Level.WARNING, this.database, "log"));
        this.userManager = new UserManager(this, this.server);//Needs Database

        this.registerPermissions(Perm.values());
        this.fileManager.dropResources(CoreResource.values());
        this.moduleManager.loadModules(this.fileManager.getModulesDir());
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
    }

    public Database getDB()
    {
        return this.database;
    }

    public PermissionRegistration getPermissionRegistration()
    {
        return this.permissionRegistration;
    }

    public UserManager getUserManager()
    {
        return userManager;
    }

    public FileManager getFileManager()
    {
        return this.fileManager;
    }

    public void registerPermissions(Permission... values)
    {
        this.permissionRegistration.registerPermissions(values);
    }

    public ModuleManager getModuleManager()
    {
        return this.moduleManager;
    }

    public I18n getI18n()
    {
        return this.i18n;
    }

    public CubeLogger getCoreLogger()
    {
        return this.logger;
    }

    public EventManager getEventManager()
    {
        return this.eventRegistration;
    }

    public CoreConfiguration getConfiguration()
    {
        return this.config;
    }

    public CommandManager getCommandManager()
    {
        return this.commandManager;
    }
}