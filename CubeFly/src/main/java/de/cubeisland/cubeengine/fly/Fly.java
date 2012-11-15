package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.fly.database.FlyManager;

public class Fly extends Module
{
    private FlyConfiguration config;
    private FlyManager       flyManager;

    @Override
    public void onEnable()
    {
        this.registerCommands(new FlyCommand(this));
        this.registerListener(new FlyListener(this));
        this.getFileManager().dropResources(FlyResource.values());
        this.registerPermissions(FlyPerm.values());
        this.flyManager = new FlyManager(this.getDatabase(), this.getInfo().getRevision());
    }

    public FlyManager getFlyManager()
    {
        return this.flyManager;
    }

    public FlyConfiguration getConfiguration()
    {
        return this.config;
    }
}
