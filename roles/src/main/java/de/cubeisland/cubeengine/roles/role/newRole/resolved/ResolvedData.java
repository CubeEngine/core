package de.cubeisland.cubeengine.roles.role.newRole.resolved;

import de.cubeisland.cubeengine.roles.role.newRole.Role;

public class ResolvedData
{
    private final Role origin;
    private String key;

    public ResolvedData(Role origin, String key)
    {
        this.origin = origin;
        this.key = key;
    }

    public Role getOrigin()
    {
        return origin;
    }

    public String getKey()
    {
        return key;
    }

    public int getPriorityValue()
    {
        return origin.getPriorityValue();
    }
}
