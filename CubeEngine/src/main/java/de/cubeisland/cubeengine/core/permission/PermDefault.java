package de.cubeisland.cubeengine.core.permission;

import org.bukkit.permissions.PermissionDefault;

public enum PermDefault
{
    TRUE(PermissionDefault.TRUE),
    FALSE(PermissionDefault.FALSE),
    OP(PermissionDefault.OP),
    NOT_OP(PermissionDefault.NOT_OP);

    private final PermissionDefault value;

    private PermDefault(PermissionDefault value)
    {
        this.value = value;
    }

    public PermissionDefault getValue()
    {
        return this.value;
    }
}
