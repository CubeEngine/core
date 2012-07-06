package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.command.BaseCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.InvalidConfigurationException;
import java.io.File;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

public class CubeFly extends Module
{
    public static boolean debugMode = false;
    protected Server server;
    protected PluginManager pm;
    protected File dataFolder;
    private static final String PERMISSION_BASE = "cubewar.fly";
    private BaseCommand baseCommand;
    private FlyConfiguration config;

    @Override
    public void onEnable()
    {
        this.pm = this.getCore().getServer().getPluginManager();
        this.config = Configuration.load(this, FlyConfiguration.class);  
        //this.baseCommand = new BaseCommand(this, PERMISSION_BASE);
        //this.baseCommand.registerCommands(new FlyCommand()).setDefaultCommand("fly").unregisterCommand("reload");
        //this.getCommand("fly").setExecutor(baseCommand);

        this.pm.registerEvents(new FlyListener(this.getCore().getUserManager(), this), this.getCore());

        CubeEngine.registerPermissions(Perm.values());
    }

    @Override
    public void onDisable()
    {
    }
}
