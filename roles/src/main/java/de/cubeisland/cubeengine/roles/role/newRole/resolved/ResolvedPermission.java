package de.cubeisland.cubeengine.roles.role.newRole.resolved;

import de.cubeisland.cubeengine.roles.role.newRole.Role;

public class ResolvedPermission extends ResolvedData
{
    private boolean value;

    public ResolvedPermission(Role origin, String key, boolean value)
    {
        super(origin, key);
        this.value = value;
    }

    public boolean isSet()
    {
        return value;
    }
}
