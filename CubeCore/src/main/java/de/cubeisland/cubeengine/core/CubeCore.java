package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.event.BukkitEventRegistration;
import de.cubeisland.cubeengine.core.event.EventRegistration;
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
import org.bukkit.plugin.PluginManager;

public class CubeCore extends PluginWrapper
{
    private Database database;
    private PermissionRegistration permissionRegistration;
    private UserManager userManager;
    private FileManager fileManager;
    private PluginManager pm;
    private ModuleManager moduleManager;
    private I18n i18n;
    private CoreConfiguration config;
    private CubeLogger coreLogger = new CubeLogger("CubeCore");
    private EventRegistration eventRegistration;
    
    public CubeCore(BukkitCore core)
    {
        super(core);
    }
    
    public Database getDB()
    {
        return this.database;
    }

    public void onEnable()
    {
        CubeEngine.initialize(this);

        this.pm = getServer().getPluginManager();
        this.permissionRegistration = new PermissionRegistration(this.pm);
        this.registerPermissions(Perm.values());

        this.fileManager = new FileManager(this.getBukkitPlugin().getDataFolder().getParentFile());
        this.fileManager.dropResources(CoreResource.values());

        this.coreLogger.addFileHandler(new File(fileManager.getLogDir(),"core.log"), Level.WARNING);
        this.config = Configuration.load(CoreConfiguration.class, new File(fileManager.getConfigDir(), "core.yml"));
        this.i18n = new I18n(this);

        try
        {
            DatabaseConfiguration databaseConfig = Configuration.load(DatabaseConfiguration.class, new File(fileManager.getConfigDir(), "database.yml"));
            this.database = new Database(databaseConfig);
        }
        catch (Throwable e)
        {
            this.coreLogger.log(Level.SEVERE, "Error while initializing database", e);
            this.pm.disablePlugin(this.getBukkitPlugin());
            return;
        }
        this.userManager = new UserManager(this.database, this.getServer());

        this.coreLogger.addDatabaseHandler(database, "corelog", Level.SEVERE);

        this.eventRegistration = new BukkitEventRegistration(this.pm);

        this.moduleManager = new ModuleManager(this);
        this.moduleManager.loadModules(this.fileManager.getModulesDir());
    }

    public void onDisable()
    {
        if (this.moduleManager != null)
        {
            this.moduleManager.clean();
            this.moduleManager = null;
        }

        CubeEngine.clean();

        this.fileManager = null;

        if (this.userManager != null)
        {
            this.userManager.clean();
            this.userManager = null;
        }

        this.permissionRegistration = null;
        this.i18n.clean();
        this.i18n = null;
    }

    /**
     * Returns the PermissionRegistration
     *
     * @return the PermissionRegistration
     */
    public PermissionRegistration getPermissionRegistration()
    {
        return this.permissionRegistration;
    }

    /**
     * Returns the PluginManager
     *
     * @return the PluginManager
     */
    public PluginManager getPluginManager()
    {
        return this.pm;
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
     * This method is a proxy to PermissionRegistration.registerPermissions
     *
     * @see de.cubeisland.cubeengine.core.permission.PermissionRegistration
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

    public CubeLogger getCoreLogger()
    {
        return this.coreLogger;
    }

    public File getDataFolder()
    {
        return this.fileManager.getDataFolder();
    }

    public EventRegistration getEventRegistration()
    {
        return this.eventRegistration;
    }
}
