package de.cubeisland.cubeengine.auctions;

import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.Bid;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.auctions.commands.*;
import de.cubeisland.cubeengine.auctions.database.AuctionBoxStorage;
import de.cubeisland.cubeengine.auctions.database.AuctionStorage;
import de.cubeisland.cubeengine.auctions.database.BidStorage;
import de.cubeisland.cubeengine.auctions.database.BidderStorage;
import de.cubeisland.cubeengine.auctions.database.PriceStorage;
import de.cubeisland.cubeengine.auctions.database.SubscriptionStorage;
import de.cubeisland.cubeengine.core.modules.CubeModuleBase;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import de.cubeisland.libMinecraft.command.BaseCommand;
import de.cubeisland.libMinecraft.translation.TranslatablePlugin;
import de.cubeisland.libMinecraft.translation.Translation;
import java.io.File;
import java.util.Stack;
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
    private static CubeAuctionsConfiguration config;
    private File dataFolder;
    private Economy economy = null;
    private static Database database;
    private BaseCommand baseCommand;
    private static final String PERMISSION_BASE = "cubeengine.auctions.commands.";
    private static CubeUserManager cuManager;
    private final Manager manager = Manager.getInstance();
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

    public static CubeAuctionsConfiguration getConfiguration()
    {
        return config;
    }

    public static CubeUserManager getCUManager()
    {
        return cuManager;
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
        config = new CubeAuctionsConfiguration(configuration);
        this.saveConfig();

        this.economy = this.setupEconomy();

        translation = Translation.get(this.getClass(), config.auction_language);
        if (translation == null)
        {
            translation = Translation.get(this.getClass(), "en");
        }

        database = new Database(config.auction_database_host,
            config.auction_database_port,
            config.auction_database_user,
            config.auction_database_pass,
            config.auction_database_name);

        this.pm.registerEvents(new CubeAuctionsListener(this), this);

        this.baseCommand = new BaseCommand(this, PERMISSION_BASE);
        this.baseCommand.registerCommands(new AddCommand()).registerCommands(new RemoveCommand()).registerCommands(new BidCommand()).registerCommands(new InfoCommand()).registerCommands(new SearchCommand()).registerCommands(new UndoBidCommand()).registerCommands(new NotifyCommand()).registerCommands(new GetItemsCommand()).registerCommands(new SubscribeCommand()).registerCommands(new UnSubscribeCommand()).registerCommands(new ListCommand()).registerCommands(new ConfirmCommand());
        this.getCommand("cubeauctions").setExecutor(baseCommand);

        AuctionTimer.getInstance().firstschedule();

        cuManager = new CubeUserManager(database, this.getServer());

        this.loadDataBase();
    }

    @Override
    public void onDisable()
    {
        //this.database.close(); //TODO gibt nach reload Fehler weil verbindung geschlossen
        database = null;
        economy = null;
        config = null;
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

    private void loadDataBase()
    {
        BidderStorage bidderDB = new BidderStorage();
        AuctionBoxStorage boxDB = new AuctionBoxStorage();
        
        AuctionStorage auctionDB = new AuctionStorage();
        BidStorage bidDB = new BidStorage();
        
        PriceStorage priceDB = new PriceStorage();
        //SubscriptionStorage subDB = new SubscriptionStorage();
        
        bidderDB.getAll();//create all Bidder + Subs
        for (Bidder bidder : Bidder.getInstances().values())
        {
            bidder.getBox().getItemList().addAll(boxDB.getAllByUser(bidder.getId()));//filled AuctionBox
        }
        manager.addAuctions(auctionDB.getAll());
        for (Auction auction : manager.getAuctions())
        {
            Stack<Bid> bids = auction.getBids();
            for (Bid bid : bidDB.getAllByAuction(auction.getId()))
            {
                bids.add(bid);//Fill Auction with bids
            }
        }
        priceDB.getAll();//Loaded in all PriceData
        
        manager.removeOldAuctions();//remove old Auctions if needed

    }
}
