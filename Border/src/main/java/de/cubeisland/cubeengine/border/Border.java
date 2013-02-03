package de.cubeisland.cubeengine.border;

import de.cubeisland.cubeengine.core.module.Module;

public class Border extends Module
{
    private BorderConfig config;

    @Override
    public void onEnable()
    {
        this.registerListener(new BorderListener(this.config));
    }
}
