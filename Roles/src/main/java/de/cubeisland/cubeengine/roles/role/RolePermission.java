package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.roles.role.config.Priority;

public class RolePermission
{
    private String perm;
    private boolean isSet;
    private Priority prio;

    public RolePermission(String perm, boolean isSet, Priority prio)
    {
        this.perm = perm;
        this.isSet = isSet;
        this.prio = prio;
    }

    public String getPerm()
    {
        return perm;
    }

    public Priority getPrio()
    {
        return prio;
    }

    public boolean isSet()
    {
        return isSet;
    }
    
    
}
