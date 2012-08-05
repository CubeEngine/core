package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import java.io.File;
import org.bukkit.Server;

public class CubeFly extends Module
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
        this.registerEvents(new FlyListener(this));
        this.getCore().getPermissionRegistration().registerPermissions(Perm.values());
    }

    @Override
    public void onDisable()
    {
    }
}
