package de.cubeisland.CubeWar;

import de.cubeisland.CubeWar.Commands.ClaimCommands;
import de.cubeisland.CubeWar.Commands.GroupCommands;
import de.cubeisland.CubeWar.Commands.UserCommands;
import de.cubeisland.libMinecraft.command.BaseCommand;
import de.cubeisland.libMinecraft.translation.TranslatablePlugin;
import de.cubeisland.libMinecraft.translation.Translation;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
//TODO LootChest testen
//Channel-Chats with pw
//Blocks with "Life"
//lift Schilder blocken wenn feindlich
//TODO HashMaps mit THashMaps ersetzen String auf --> gibts das?

/**
 * Main Class
 */
public class CubeWar extends JavaPlugin implements TranslatablePlugin
{
    private static CubeWar instance = null;
    private static Logger logger = null;
    public static boolean debugMode = false;
    private static Translation translation;
    private static final String PERMISSION_BASE = "cubewar.commands.";
    private BaseCommand baseCommand;
    
    private Server server;
    private PluginManager pm;
    private CubeWarConfiguration config;
    private File dataFolder;
    
    private static Economy economy = null;

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

        this.baseCommand = new BaseCommand(this, PERMISSION_BASE);
        this.baseCommand.registerCommands(new ClaimCommands())
                        .registerCommands(new GroupCommands())
                        .registerCommands(new UserCommands());
        
        this.getCommand("cubewar").setExecutor(baseCommand);

        this.pm.registerEvents(new CubeWarListener(), this);
    }
    
    @Override
    public void onDisable()
    {
        this.config = null;
    }

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
    
    public static Economy getEconomy()
    {
        return economy;
    }
       
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

    public Translation getTranslation()
    {
        return translation;
    }

    public void setTranslation(Translation newtranslation)
    {
        translation = newtranslation;
    }
   
}
