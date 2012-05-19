package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.configuration.ConfigurationManager;
import de.cubeisland.cubeengine.core.configuration.CubeConfiguration;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CubeCore extends JavaPlugin
{
    private static CubeCore instance;

    private Database database;
    private CubeUserManager cuManager;
    private PermissionRegistration permissionRegistration;
    private CubeUserManager userManager;
    private ConfigurationManager configManager;

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
        this.configManager = new ConfigurationManager(this);
        final CubeConfiguration coreConfig = this.configManager.getCoreConfig();
        coreConfig.safeSave();

        final CubeConfiguration databaseConfig = this.configManager.getDatabaseConfig();
        databaseConfig.safeSave();
        
        this.database = new Database(
            databaseConfig.getString("mysql.host"),
            (short)databaseConfig.getInt("mysql.port"),
            databaseConfig.getString("mysql.user"),
            databaseConfig.getString("mysql.password"),
            databaseConfig.getString("mysql.database"),
            databaseConfig.getString("mysql.tableprefix")
        );

        this.userManager = new CubeUserManager(this);
        this.permissionRegistration = new PermissionRegistration(getServer().getPluginManager());
    }

    @Override
    public void onDisable()
    {
        this.configManager.clean();
        this.configManager = null;

        this.userManager.clean();
        this.userManager = null;

        this.permissionRegistration = null;
    }

    public PermissionRegistration getPermissionRegistration()
    {
        return this.permissionRegistration;
    }
}
