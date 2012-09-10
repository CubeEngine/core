package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import java.io.File;
import org.bukkit.Server;


public class Fly extends Module
{
    public static boolean debugMode = false;
    protected Server server;
    protected File dataFolder;
    private FlyConfiguration config;

    @Override
    public void onEnable()
    {
        this.config = Configuration.load(FlyConfiguration.class, this);  
        //this.baseCommand = new BaseCommand(this, PERMISSION_BASE);
        //this.baseCommand.registerCommands(new FlyCommand()).setDefaultCommand("fly").unregisterCommand("reload");
        //this.getCommand("fly").setExecutor(baseCommand);
        this.getCore().getEventManager().registerListener(new FlyListener(this), this);
        this.config = Configuration.load(FlyConfiguration.class, this);
        this.getFileManager().dropResources(FlyResource.values());
        this.getCore().getPermissionRegistration().registerPermissions(Perm.values());
    }

    @Override
    public void onDisable()
    {
    }
}
