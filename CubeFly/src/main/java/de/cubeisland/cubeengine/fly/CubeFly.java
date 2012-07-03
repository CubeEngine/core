package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.command.BaseCommand;
import de.cubeisland.cubeengine.core.module.ModuleBase;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import java.io.File;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

public class CubeFly extends ModuleBase
{
    public static boolean debugMode = false;
    protected Server server;
    protected PluginManager pm;
    protected File dataFolder;
    private static final String PERMISSION_BASE = "cubewar.fly";
    private BaseCommand baseCommand;
    private FlyConfiguration config;

    public CubeFly()
    {
        super("CubeFly");
    }

    @Override
    public void onEnable()
    {
        CubeEngine.registerModule(this);

        this.pm = this.getServer().getPluginManager();

        this.config = Configuration.loadYaml(this, FlyConfiguration.class);

        this.baseCommand = new BaseCommand(this, PERMISSION_BASE);
        this.baseCommand.registerCommands(new FlyCommand()).setDefaultCommand("fly").unregisterCommand("reload");
        this.getCommand("fly").setExecutor(baseCommand);

        this.pm.registerEvents(new FlyListener(CubeCore.getInstance().getUserManager(), this), this);

        CubeEngine.registerPermissions(Perm.values());

        //TODO Test später löschen:
        this.getLogger().addFileHandler("FlyTestLog.log", Level.WARNING);
        this.logger.info("No Information at all");
        this.logger.warning("No Warning at all");
        this.logger.log(Level.INFO, "Version {0} enabled", this.getDescription().getVersion());
    }

    @Override
    public void onDisable()
    {
        this.logger.log(Level.INFO, "Disabling {0} {1}", new Object[]{this.getModuleName(), this.getDescription().getVersion()});
    }
}
