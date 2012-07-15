package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Bootstrapper;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CoreConfiguration;
import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.DatabaseConfiguration;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.event.BukkitEventManager;
import de.cubeisland.cubeengine.core.event.EventManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.Perm;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import java.io.File;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class BukkitCore implements Core
{
    private Database database;
    private PermissionRegistration permissionRegistration;
    private UserManager userManager;
    private FileManager fileManager;
    private ModuleManager moduleManager;
    private I18n i18n;
    private CoreConfiguration config;
    private CubeLogger coreLogger = new CubeLogger("CubeCore");
    private EventManager eventRegistration;
    private Bootstrapper bootstrapper;
    private Server server;
    private CommandManager commandManager;

    public BukkitCore(BukkitBootstrapper bootstrapper)
    {
        this.server = bootstrapper.getServer();
        PluginManager pm = this.server.getPluginManager();
        this.bootstrapper = bootstrapper;
        this.permissionRegistration = new BukkitPermissionRegistration(pm);
        this.fileManager = new FileManager(bootstrapper.getDataFolder().getParentFile());
        this.i18n = new I18n(this);
        this.eventRegistration = new BukkitEventManager(pm);
        this.moduleManager = new ModuleManager(this);
        this.commandManager = new BukkitCommandManager(this);
    }

    public Database getDatabase()
    {
        return this.database;
    }

    public void enable()
    {
        this.config = Configuration.load(CoreConfiguration.class, new File(fileManager.getConfigDir(), "core.yml"));
        try
        {
            DatabaseConfiguration databaseConfig = Configuration.load(DatabaseConfiguration.class, new File(fileManager.getConfigDir(), "database.yml"));
            this.database = new Database(databaseConfig);
        }
        catch (Throwable e)
        {
            this.coreLogger.log(Level.SEVERE, "Error while initializing database", e);
            this.server.getPluginManager().disablePlugin((Plugin)bootstrapper);
            return;
        }
        this.coreLogger.addFileHandler(new File(fileManager.getLogDir(), "core.log"), Level.WARNING);
        this.coreLogger.addDatabaseHandler(database, "corelog", Level.SEVERE);//Needs Database
        this.userManager = new UserManager(this, this.server);//Needs Database

        this.registerPermissions(Perm.values());
        this.fileManager.dropResources(CoreResource.values());
        this.moduleManager.loadModules(this.fileManager.getModulesDir());
    }

    public void disable()
    {
        this.moduleManager.clean();
        this.moduleManager = null;

        this.fileManager = null;

        this.userManager.clean();
        this.userManager = null;

        this.permissionRegistration = null;

        this.i18n.clean();
        this.i18n = null;
    }

    /**
     * Returns the BukkitPermissionRegistration
     *
     * @return the BukkitPermissionRegistration
     */
    public PermissionRegistration getPermissionRegistration()
    {
        return this.permissionRegistration;
    }

    /**
     * Returns the UserManager
     *
     * @return the UserManager
     */
    public UserManager getUserManager()
    {
        return userManager;
    }

    /**
     * Returns the FileManager
     *
     * @return the FileManager
     */
    public FileManager getFileManager()
    {
        return this.fileManager;
    }

    /**
     * This method is a proxy to BukkitPermissionRegistration.registerPermissions
     *
     * @see de.cubeisland.cubeengine.core.permission.BukkitPermissionRegistration
     */
    public void registerPermissions(Permission... values)
    {
        this.permissionRegistration.registerPermissions(values);
    }

    /**
     * Returns the module manager
     *
     * @return the module manager
     */
    public ModuleManager getModuleManager()
    {
        return this.moduleManager;
    }

    public I18n getI18n()
    {
        return this.i18n;
    }

    public CubeLogger getLogger()
    {
        return this.coreLogger;
    }

    public File getDataFolder()
    {
        return this.fileManager.getDataFolder();
    }

    public EventManager getEventManager()
    {
        return this.eventRegistration;
    }

    public CoreConfiguration getConfiguration()
    {
        return this.config;
    }

    public Bootstrapper getBootstrapper()
    {
        return this.bootstrapper;
    }

    public CommandManager getCommandManager()
    {
        return this.commandManager;
    }
}
