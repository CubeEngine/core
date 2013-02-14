package de.cubeisland.cubeengine.border;

import de.cubeisland.cubeengine.core.module.Module;

public class Border extends Module
{
    protected BorderConfig config;

    @Override
    public void onEnable()
    {
        this.registerPermissions(BorderPerms.values());
        this.registerListener(new BorderListener(this));
    }
}
