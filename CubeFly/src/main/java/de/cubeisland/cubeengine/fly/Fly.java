package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fly.database.FlyManager;
import java.io.File;
import org.bukkit.Server;


public class Fly extends Module
{
    public static boolean debugMode = false;
    protected Server server;
    protected File dataFolder;
    
    @From("fly.yml")
    protected FlyConfiguration config;
    
    private FlyManager flyManager;

    @Override
    public void onEnable()
    {
        this.registerCommands(new FlyCommand(this));
        this.registerListener(new FlyListener(this));
        this.config = Configuration.load(FlyConfiguration.class, this);
        this.getFileManager().dropResources(FlyResource.values());
        this.registerPermissions(FlyPerm.values());
        this.flyManager = new FlyManager(this.getDatabase(), this.getInfo().getRevision());
    }
    
    public FlyManager getFlyManager()
    {
        return this.flyManager;
    }
}
