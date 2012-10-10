package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.permission.Permission;
import java.util.Locale;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

public enum FlyPerm implements Permission
{
    COMMAND_FLY_SELF,
    COMMAND_FLY_OTHER,
    FLY_CANFLY,
    FLY_FEATHER,;
    private final String permission;
    private PermissionDefault def;

    private FlyPerm()
    {
        this(PermissionDefault.OP);
    }

    private FlyPerm(PermissionDefault def)
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

    public PermissionDefault getPermissionDefault()
    {
        return this.def;
    }
}
