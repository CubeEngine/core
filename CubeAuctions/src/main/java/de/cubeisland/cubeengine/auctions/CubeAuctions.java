package de.cubeisland.cubeengine.auctions;

import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.auctions.commands.AddCommand;
import de.cubeisland.cubeengine.auctions.commands.BidCommand;
import de.cubeisland.cubeengine.auctions.commands.ConfirmCommand;
import de.cubeisland.cubeengine.auctions.commands.GetItemsCommand;
import de.cubeisland.cubeengine.auctions.commands.HelpCommand;
import de.cubeisland.cubeengine.auctions.commands.InfoCommand;
import de.cubeisland.cubeengine.auctions.commands.ListCommand;
import de.cubeisland.cubeengine.auctions.commands.NotifyCommand;
import de.cubeisland.cubeengine.auctions.commands.ReloadCommand;
import de.cubeisland.cubeengine.auctions.commands.RemoveCommand;
import de.cubeisland.cubeengine.auctions.commands.SearchCommand;
import de.cubeisland.cubeengine.auctions.commands.SubscribeCommand;
import de.cubeisland.cubeengine.auctions.commands.UnSubscribeCommand;
import de.cubeisland.cubeengine.auctions.commands.UndoBidCommand;
import de.cubeisland.cubeengine.core.modules.CubeModuleBase;
import de.cubeisland.cubeengine.core.persistence.Database;
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

/**
 * Main Class
 */
public class CubeAuctions extends CubeModuleBase implements TranslatablePlugin
{
    private static CubeAuctions instance = null;
    private static Logger logger = null;
    public static boolean debugMode = false;
    private static Translation translation;
    
    private Server server;
    private PluginManager pm;
    private AuctionHouseConfiguration config;
    private File dataFolder;
    private Economy economy = null;
    private Database database;
//TODO später eigene AuktionsBox als Kiste mit separatem inventar 
//TODO flatfile mit angeboten
//TODO DatenBankNutzung schöner machen
//TODO ah rem last / l
    public CubeAuctions()
    {
        instance = this;
    }
    
    public static CubeAuctions getInstance()
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
        this.config = new AuctionHouseConfiguration(configuration);
        this.saveConfig();
        
        this.economy = this.setupEconomy();
        
        translation = Translation.get(this.getClass(), config.auction_language);
        if (translation == null) translation = Translation.get(this.getClass(), "en");

        database = new Database(config.auction_database_host,
                                config.auction_database_port,
                                config.auction_database_user,
                                config.auction_database_pass,
                                config.auction_database_name);
        
        //database.loadDatabase();//TODO
        Manager.getInstance().removeOldAuctions();
        
        this.pm.registerEvents(new AuctionHouseListener(this), this);
        
        BaseCommand baseCommand = new BaseCommand(this);
        baseCommand
            .registerSubCommand(new         HelpCommand(baseCommand))
            .registerSubCommand(new          AddCommand(baseCommand))
            .registerSubCommand(new       RemoveCommand(baseCommand))
            .registerSubCommand(new          BidCommand(baseCommand))
            .registerSubCommand(new         InfoCommand(baseCommand))
            .registerSubCommand(new       SearchCommand(baseCommand))
            .registerSubCommand(new      UndoBidCommand(baseCommand))
            .registerSubCommand(new       NotifyCommand(baseCommand))
            .registerSubCommand(new     GetItemsCommand(baseCommand))
            .registerSubCommand(new    SubscribeCommand(baseCommand))
            .registerSubCommand(new  UnSubscribeCommand(baseCommand))
            .registerSubCommand(new         ListCommand(baseCommand))
            .registerSubCommand(new      ConfirmCommand(baseCommand))    
            .registerSubCommand(new       ReloadCommand(baseCommand)) 
        .setDefaultCommand("help");
        this.getCommand("auctionhouse").setExecutor(baseCommand);
        
        AuctionTimer.getInstance().firstschedule();
    }
    
    @Override
    public void onDisable()
    {
        //this.database.close(); //TODO gibt nach reload Fehler weil verbindung geschlossen
        this.database = null;
        this.economy = null;
        this.config = null;
        AuctionTimer.getInstance().stop();
        Bidder.getInstances().clear();
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
    
    public Economy getEconomy()
    {
        return this.economy;
    }
       
    public AuctionHouseConfiguration getConfiguration()
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
    
    public Database getDB()
    {
        return this.database;
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
