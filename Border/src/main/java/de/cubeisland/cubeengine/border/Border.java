package de.cubeisland.cubeengine.border;

import de.cubeisland.cubeengine.core.module.Module;

public class Border extends Module
{
    protected BorderConfig config;
    private BorderPerms perm;

    @Override
    public void onEnable()
    {
        this.perm = new BorderPerms(this);
        this.registerListener(new BorderListener(this));
    }

    @Override
    public void onDisable()
    {
        this.perm.cleanup();
    }
}
