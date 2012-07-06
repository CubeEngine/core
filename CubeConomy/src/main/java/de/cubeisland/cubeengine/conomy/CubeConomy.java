package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.core.module.Module;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginManager;

public class CubeConomy extends Module
{
    protected static Logger logger = null;
    public static boolean debugMode = false;
    
    protected Server server;
    protected PluginManager pm;
    protected File dataFolder;
    private static final String PERMISSION_BASE = "cubewar.conomy";

    @Override
    public void onEnable()
    {
        logger = this.getLogger();
        //this.server = this.getServer();
        this.pm = this.server.getPluginManager();
        
        
        //Configuration configuration = this.getConfig();
        //configuration.options().copyDefaults(true);
        //debugMode = configuration.getBoolean("debug");
        
               
        //CubeCore.getInstance().getPermissionRegistration().registerPermissions(Perm.values());

    }

    @Override
    public void onDisable()
    {
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
}
