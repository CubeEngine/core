package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.command.BaseCommand;
import de.cubeisland.cubeengine.core.module.ModuleBase;
import de.cubeisland.cubeengine.core.persistence.filesystem.Configuration;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

public class CubeFly extends ModuleBase
{
    protected static Logger logger = null;
    public static boolean debugMode = false;
    protected Server server;
    protected PluginManager pm;
    protected File dataFolder;
    private static final String PERMISSION_BASE = "cubewar.fly";
    private BaseCommand baseCommand;
    private FlyConfiguration config;

    public CubeFly()
    {
        super("fly");
    }

    @Override
    public void onEnable()
    {
        logger = this.getLogger();
        this.server = this.getServer();
        this.pm = this.server.getPluginManager();
        this.dataFolder = this.getDataFolder();

        this.dataFolder.mkdirs();

        this.config = Configuration.load(this, FlyConfiguration.class);

        debugMode = this.config.debugMode;

        this.baseCommand = new BaseCommand(this, PERMISSION_BASE);
        this.baseCommand.registerCommands(new FlyCommand()).setDefaultCommand("fly").unregisterCommand("reload");
        this.getCommand("fly").setExecutor(baseCommand);

        this.pm.registerEvents(new FlyListener(CubeCore.getInstance().getUserManager(), this), this);

        CubeCore.getInstance().getPermissionRegistration().registerPermissions(Perm.values());

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
}
