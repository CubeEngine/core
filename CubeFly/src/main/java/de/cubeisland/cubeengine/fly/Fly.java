package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fly.database.FlyManager;
import java.io.File;
import org.bukkit.Server;


public class Fly extends Module
{
    public static boolean debugMode = false;
    protected Server server;
    protected File dataFolder;
    private FlyConfiguration config;
    private FlyManager flyManager;

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
        this.flyManager = new FlyManager(this.getDatabase(), this.getInfo().getRevision());
    }
    
    public FlyManager getFlyManager()
    {
        return this.flyManager;
    }

    @Override
    public void onDisable()
    {
    }
}
