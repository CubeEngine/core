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
            this.registerListener(new FlyListener(this));
        }
        this.getFileManager().dropResources(FlyResource.values());
    }
}
