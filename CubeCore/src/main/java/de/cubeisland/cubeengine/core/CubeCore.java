package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.persistence.Database;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CubeCore extends JavaPlugin
{
    protected static Logger logger = null;
    public static boolean debugMode = false;
    private static CubeCore instance;

    public static Database getDB()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public static CubeCore getInstance()
    {
        return instance;
    }
    
    protected Server server;
    protected PluginManager pm;
    protected File dataFolder;

    public void onEnable()
    {
    }

    public void onDisable()
    {
    }
}
