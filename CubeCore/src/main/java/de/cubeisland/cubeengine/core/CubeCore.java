package de.cubeisland.cubeengine.core;

import com.maxmind.geoip.LookupService;
import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.permission.Perm;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.persistence.filesystem.CubeConfiguration;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.io.IOException;
import java.net.InetAddress;
import org.bukkit.plugin.java.JavaPlugin;

public class CubeCore extends JavaPlugin
{
    private static CubeCore instance;

    private LookupService lookupService;
    private Database database;
    private PermissionRegistration permissionRegistration;
    private UserManager userManager;
    private FileManager fileManager;

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
        this.fileManager = new FileManager(this);
        final CubeConfiguration coreConfig = this.fileManager.getCoreConfig();
        coreConfig.safeSave();

        final CubeConfiguration databaseConfig = this.fileManager.getDatabaseConfig();
        databaseConfig.safeSave();

        try
        {
            this.lookupService = new LookupService(this.fileManager.getGeoipFile());
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

        this.userManager = new UserManager(this);
        this.permissionRegistration = new PermissionRegistration(getServer().getPluginManager());

        CubeEngine.initialize(this);
        this.permissionRegistration.registerPermissions(Perm.values());
    }

    @Override
    public void onDisable()
    {
        CubeEngine.clean();
        
        this.fileManager.clean();
        this.fileManager = null;

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
    public UserManager getUserManager()
    {
        return userManager;
    }

    public FileManager getFileManager()
    {
        return this.fileManager;
    }
}
