package de.cubeisland.CubeWar;

import de.cubeisland.libMinecraft.translation.Translation;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main Class
 */
public class CubeWar extends JavaPlugin
{
    private static CubeWar instance = null;
    private static Logger logger = null;
    public static boolean debugMode = false;
    private static Translation translation;
    
    private Server server;
    private PluginManager pm;
    private CubeWarConfiguration config;
    private File dataFolder;

    public CubeWar()
    {
        instance = this;
    }
    
    public static CubeWar getInstance()
    {
        return instance;
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
        this.config = new CubeWarConfiguration(configuration);
        this.saveConfig();
        
        
        translation = Translation.get(this.getClass(), config.cubewar_language);
        if (translation == null) translation = Translation.get(this.getClass(), "en");


        this.pm.registerEvents(new CubeWarListener(), this);
        /*
        BaseCommand baseCommand = new BaseCommand(this);
        baseCommand
            .registerSubCommand(new         HelpCommand(baseCommand))
        .setDefaultCommand("help");
        this.getCommand("cubewar").setExecutor(baseCommand);
        */
    }
    
    @Override
    public void onDisable()
    {
        this.config = null;
    }
    /*
    private Economy setupEconomy()
    {
        if (this.pm.getPlugin("Vault") != null)
        {
            RegisteredServiceProvider<Economy> rsp = this.server.getServicesManager().getRegistration(Economy.class);
            if (rsp != null)
            {
                Economy eco = rsp.getProvider();
                if (eco != null)
                {
                    return eco;
                }
            }
        }
        throw new IllegalStateException("Failed to initialize with Vault!");
    }
    
    public Economy getEconomy()
    {
        return this.economy;
    }
    * 
    */
       
    public CubeWarConfiguration getConfiguration()
    {
        return this.config;
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
    
    public static String t(String key, Object... params)
    {
        return translation.translate(key, params);
    }
   
}
