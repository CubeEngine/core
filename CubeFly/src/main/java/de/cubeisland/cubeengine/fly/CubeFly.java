package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.command.BaseCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import java.io.File;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

public class CubeFly extends Module
{
    public static boolean debugMode = false;
    protected Server server;
    protected File dataFolder;
    private static final String PERMISSION_BASE = "cubewar.fly";
    private BaseCommand baseCommand;
    private FlyConfiguration config;

    @Override
    public void onEnable()
    {
        this.config = Configuration.load(this, FlyConfiguration.class);  
        //this.baseCommand = new BaseCommand(this, PERMISSION_BASE);
        //this.baseCommand.registerCommands(new FlyCommand()).setDefaultCommand("fly").unregisterCommand("reload");
        //this.getCommand("fly").setExecutor(baseCommand);
        this.getPluginManager().registerEvents(new FlyListener(this.getCore().getUserManager()), this.getCore());
        CubeEngine.registerPermissions(Perm.values());
    }

    @Override
    public void onDisable()
    {
    }
}
