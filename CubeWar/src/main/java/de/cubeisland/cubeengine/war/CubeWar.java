package de.cubeisland.cubeengine.war;

import de.cubeisland.cubeengine.core.modules.CubeModuleBase;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.war.area.AreaControl;
import de.cubeisland.cubeengine.war.commands.ByPassCommand;
import de.cubeisland.cubeengine.war.commands.ClaimCommands;
import de.cubeisland.cubeengine.war.commands.GroupCommands;
import de.cubeisland.cubeengine.war.commands.UserCommands;
import de.cubeisland.cubeengine.war.database.AreaStorage;
import de.cubeisland.cubeengine.war.database.DenyUsageStorage;
import de.cubeisland.cubeengine.war.database.GroupStorage;
import de.cubeisland.cubeengine.war.database.UserStorage;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import de.cubeisland.cubeengine.war.user.PvP;
import de.cubeisland.cubeengine.war.user.UserControl;
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
//Channel-Chats with pw
//Blocks with "Life"
//lift Schilder blocken wenn feindlich

/**
 * Main Class
 */
public class CubeWar extends CubeModuleBase implements TranslatablePlugin
{

    private static CubeWar instance = null;
    private static Logger logger = null;
    public static boolean debugMode = false;
    private static Translation translation;
    private static final String PERMISSION_BASE = "cubeengine.war.commands.";
    private static Database database;
    private BaseCommand baseCommand;
    private Server server;
    private PluginManager pm;
    private CubeWarConfiguration config;
    private File dataFolder;
    private Economy economy = null;
    private AreaControl areas;
    private GroupControl groups;
    private UserControl users;
    private PvP pvp;
    private AreaStorage areaDB;
    private DenyUsageStorage denyuseDB;
    private GroupStorage groupDB;
    private UserStorage userDB;

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
        groups = GroupControl.get();
        this.saveConfig();

        database = new Database(config.war_database_host,
                config.war_database_port,
                config.war_database_user,
                config.war_database_pass,
                config.war_database_name);

        translation = Translation.get(this.getClass(), config.cubewar_language);
        if (translation == null)
        {
            translation = Translation.get(this.getClass(), "en");
        }
        
        //Load in DB...
        users = new UserControl();
        areaDB = new AreaStorage();
        areas = new AreaControl();
        denyuseDB = new DenyUsageStorage();
        groupDB = new GroupStorage();
        userDB = new UserStorage();
        users.loadDB();
        groups.loadDB();
        areaDB.load();
        
        pvp = new PvP();
        
        this.baseCommand = new BaseCommand(this, PERMISSION_BASE);
        this.baseCommand.registerCommands(new ClaimCommands()).registerCommands(new GroupCommands()).registerCommands(new UserCommands()).registerCommands(new ByPassCommand());
        this.getCommand("cubewar").setExecutor(baseCommand);
        this.pm.registerEvents(new CubeWarListener(), this);
    }

    @Override
    public void onDisable()
    {
        this.config = null;
    }

    public static Database getDB()
    {
        return database;
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

    /**
     * @return the areas
     */
    public AreaControl getAreas()
    {
        return areas;
    }

    /**
     * @return the pvp
     */
    public PvP getPvp()
    {
        return pvp;
    }

    /**
     * @return the areaDB
     */
    public AreaStorage getAreaDB()
    {
        return areaDB;
    }

    /**
     * @return the denyuseDB
     */
    public DenyUsageStorage getDenyuseDB()
    {
        return denyuseDB;
    }

    /**
     * @return the groupDB
     */
    public GroupStorage getGroupDB()
    {
        return groupDB;
    }

    /**
     * @return the userDB
     */
    public UserStorage getUserDB()
    {
        return userDB;
    }

    /**
     * @return the users
     */
    public UserControl getUserControl()
    {
        return users;
    }
}
