package de.cubeisland.cubeengine.core;

import com.maxmind.geoip.LookupService;
import de.cubeisland.cubeengine.core.configuration.ConfigurationManager;
import de.cubeisland.cubeengine.core.configuration.CubeConfiguration;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import java.io.IOException;
import java.net.InetAddress;
import org.bukkit.plugin.java.JavaPlugin;

public class CubeCore extends JavaPlugin
{
    private static CubeCore instance;

    private LookupService lookupService;
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

        try
        {
            this.lookupService = new LookupService(this.configManager.getGeoipFile());
        }
        catch (IOException e)
        {
            throw new RuntimeException("CubeCore failed to load the GeoIP database!");
        }
        
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

        this.getUserManager().clean();
        this.userManager = null;

        this.permissionRegistration = null;
    }

    public PermissionRegistration getPermissionRegistration()
    {
        return this.permissionRegistration;
    }

    public String locateAddress(InetAddress address)
    {
        return this.lookupService.getCountry(address).getCode();
    }

    /**
     * @return the userManager
     */
    public CubeUserManager getUserManager()
    {
        return userManager;
    }
}
