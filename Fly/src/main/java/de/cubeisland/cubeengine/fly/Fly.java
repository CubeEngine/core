package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.module.Module;

public class Fly extends Module
{
    private FlyConfig config;

    @Override
    public void onEnable()
    {
        if (this.config.flyfeather)
        {
            this.getCore().getEventManager().registerListener(this, new FlyListener(this));
        }
        this.getCore().getFileManager().dropResources(FlyResource.values());
        this.getCore().getPermissionManager().registerPermissions(this, FlyPerm.values());
    }
}
