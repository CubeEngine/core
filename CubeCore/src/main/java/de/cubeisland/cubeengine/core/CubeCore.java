package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.Perm;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.log.LoggerManager;
import java.io.File;
import java.util.logging.Level;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CubeCore extends JavaPlugin
{
    private static CubeCore instance;
    private Database database;
    private PermissionRegistration permissionRegistration;
    private UserManager userManager;
    private FileManager fileManager;
    private PluginManager pm;
    private ModuleManager moduleManager;
    private I18n i18n;
    private CoreConfiguration config;
    private LoggerManager coreLogger;

    public CubeCore()
    {
        instance = this;
    }

    public Database getDB()
    {
        return this.database;
    }

    public static CubeCore getInstance()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        this.moduleManager = new ModuleManager(this);
        this.fileManager = new FileManager(super.getDataFolder().getParentFile());
        this.config = Configuration.load(new File(getDataFolder(), "core.yml"), CoreConfiguration.class);
        DatabaseConfiguration databaseConfig = Configuration.load(new File(getDataFolder(), "database.yml"), DatabaseConfiguration.class);
        this.i18n = new I18n(this);

        this.pm = getServer().getPluginManager();

        this.database = new Database(databaseConfig);

        this.userManager = new UserManager(this.database, this.getServer());
        this.permissionRegistration = new PermissionRegistration(this.pm);
        this.registerPermissions(Perm.values());
        
        //TODO loggertests here:
        this.coreLogger = new LoggerManager();
        this.coreLogger.log("CubeCore", "cookie not found 404", Level.WARNING);
        this.coreLogger.log("CubeCore", "cookie got eaten 403", Level.SEVERE);
        this.coreLogger.log("CubeCore", "cookie are too many 1337", Level.INFO);
        CubeEngine.initialize(this);
    }

    @Override
    public void onDisable()
    {
        this.moduleManager.clean();
        this.moduleManager = null;

        CubeEngine.clean();

        this.fileManager = null;

        this.getUserManager().clean();
        this.userManager = null;

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

    public LoggerManager getCoreLogger()
    {
        return this.coreLogger;
    }

    @Override
    public File getDataFolder()
    {
        return this.fileManager.getDataFolder();
    }
}
