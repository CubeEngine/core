package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.permission.PermDefault;

import org.bukkit.permissions.Permissible;

import java.util.Locale;

public enum FlyPerm implements Permission
{
    COMMAND_FLY_SELF,
    COMMAND_FLY_OTHER,
    FLY_CANFLY,
    FLY_FEATHER, ;
    private final String permission;
    private PermDefault def;

    private FlyPerm()
    {
        this(PermDefault.OP);
    }

    private FlyPerm(PermDefault def)
    {
        this.permission = "cubeengine.fly." + this.name().toLowerCase(Locale.ENGLISH).replace('_', '.');
        this.def = def;
    }

    public boolean isAuthorized(Permissible player)
    {
        return player.hasPermission(permission);
    }

    public String getPermission()
    {
        return this.permission;
    }

    public PermDefault getPermissionDefault()
    {
        return this.def;
    }
}
