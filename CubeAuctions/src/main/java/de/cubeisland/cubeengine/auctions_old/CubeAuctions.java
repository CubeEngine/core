package de.cubeisland.cubeengine.auctions_old;

import de.cubeisland.cubeengine.auctions_old.database.AuctionBoxStorage;
import de.cubeisland.cubeengine.auctions_old.database.AuctionStorage;
import de.cubeisland.cubeengine.auctions_old.database.BidStorage;
import de.cubeisland.cubeengine.auctions_old.database.BidderStorage;
import de.cubeisland.cubeengine.auctions_old.database.PriceStorage;
import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.module.ModuleBase;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.user.UserManager;
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

/**
 * Main Class
 */
public class CubeAuctions extends ModuleBase implements TranslatablePlugin
{
    private static CubeAuctions instance = null;
    private static Logger logger = null;
    public static boolean debugMode = false;
    private static Translation translation;
    private Server server;
    private PluginManager pm;
    private File dataFolder;
    private Economy economy = null;
    private static Database database;
    private BaseCommand baseCommand;
    private static final String PERMISSION_BASE = "cubeengine.auctions.commands.";
    private static UserManager cuManager;
//TODO später eigene AuktionsBox als Kiste mit separatem inventar 
//TODO flatfile mit angeboten
//TODO ah rem last / l

    public CubeAuctions()
    {
        super("auctions");
        instance = this;
    }

    public static CubeAuctions getInstance()
    {
        return instance;
    }

    public static Database getDB()
    {
        return database;
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



        this.economy = this.setupEconomy();


        this.getCommand("cubeauctions").setExecutor(baseCommand);


        cuManager = CubeCore.getInstance().getUserManager();

        this.loadDataBase();

    }

    @Override
    public void onDisable()
    {
        //this.database.close(); //TODO gibt nach reload Fehler weil verbindung geschlossen
        database = null;
        economy = null;

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

    public static void log(String msg)
    {
        logger.log(LogLevelINFO, msg);
    }

    public static void error(String msg)
    {
        logger.log(LogLevelERROR, msg);
    }

    public static void error(String msg, Throwable t)
    {
        logger.log(LogLevelERROR, msg, t);
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

    private void loadDataBase()
    {
        BidderStorage bidderDB = new BidderStorage();
        AuctionBoxStorage boxDB = new AuctionBoxStorage();

        AuctionStorage auctionDB = new AuctionStorage();
        BidStorage bidDB = new BidStorage();

        PriceStorage priceDB = new PriceStorage();
        //SubscriptionStorage subDB = new SubscriptionStorage();


    }
}
