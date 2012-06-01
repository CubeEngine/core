package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.module.ModuleBase;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginManager;

public class CubeBasics extends ModuleBase
{
    protected static Logger logger = null;
    public static boolean debugMode = false;
    
    protected Server server;
    protected PluginManager pm;
    protected File dataFolder;
    private static final String PERMISSION_BASE = "cubeengine.basics";
    
    private UserManager cuManager;
    private BasicUserManager buManager;
    
    private static CubeBasics instance;

    public CubeBasics()
    {
        super("basics");
        instance = this;
    }

    @Override
    public void onEnable()
    {
        logger = this.getLogger();
        this.server = this.getServer();
        this.pm = this.server.getPluginManager();
        this.dataFolder = this.getDataFolder();

        this.dataFolder.mkdirs();
        
        Configuration configuration = this.getConfig();
        configuration.options().copyDefaults(true);
        debugMode = configuration.getBoolean("debug");
        
        this.saveConfig();
        CubeCore core = CubeCore.getInstance();
        cuManager = core.getUserManager();
        core.registerPermissions(Perm.values());
        buManager = new BasicUserManager();

        log("Version " + this.getDescription().getVersion() + " enabled");
    }

    @Override
    public void onDisable()
    {
        log("Version " + this.getDescription().getVersion() + " disabled");
    }

    public static void log(String msg)
    {
        logger.log(Level.INFO, msg);
    }

    public static void error(String msg)
    {
        logger.log(Level.SEVERE, msg);
    }

    public static void error(String msg, Throwable t)
    {
        logger.log(Level.SEVERE, msg, t);
    }

    public static void debug(String msg)
    {
        if (debugMode)
        {
            log("[debug] " + msg);
        }
    }

    /**
     * @return the cuManager
     */
    public UserManager getCuManager()
    {
        return cuManager;
    }

    /**
     * @return the buManager
     */
    public BasicUserManager getBuManager()
    {
        return buManager;
    }

    /**
     * @return the instance
     */
    public static CubeBasics getInstance()
    {
        return instance;
    }
}
