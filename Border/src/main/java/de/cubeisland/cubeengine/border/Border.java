package de.cubeisland.cubeengine.border;

import de.cubeisland.cubeengine.core.module.Module;

public class Border extends Module
{
    protected BorderConfig config;

    @Override
    public void onEnable()
    {
        this.getCore().getPermissionManager().registerPermissions(this, BorderPerms.values());
        this.getCore().getEventManager().registerListener(this, new BorderListener(this));
    }
}
