package de.cubeisland.cubeengine.auctions;

import de.cubeisland.cubeengine.core.module.ModuleBase;
import de.cubeisland.cubeengine.core.persistence.filesystem.Configuration;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

public class CubeAuctions extends ModuleBase
{
    protected static Logger logger = null;
    public static boolean debugMode = false;
    protected Server server;
    protected PluginManager pm;
    protected File dataFolder;
    private AuctionsConfiguration config;
    private static CubeAuctions instance = null;

    public CubeAuctions()
    {
        super("auctions");
    }

    @Override
    public void onEnable()
    {
        logger = this.getLogger();
        this.server = this.getServer();
        this.pm = this.server.getPluginManager();
        this.dataFolder = this.getDataFolder();

        this.dataFolder.mkdirs();

        this.config = Configuration.load(this, AuctionsConfiguration.class);
        this.config.convert(); // StringList -> MaterialList

        debugMode = this.config.debugMode;

        this.getCore().getPermissionRegistration().registerPermissions(Perm.values());
        this.pm.registerEvents(new CubeAuctionListener(this), this);

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

    public static CubeAuctions getInstance()
    {
        return instance;
    }

    public AuctionsConfiguration getConfiguration()
    {
        return config;
    }
}
