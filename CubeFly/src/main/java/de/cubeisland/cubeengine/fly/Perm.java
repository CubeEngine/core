package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author Anselm Brehme
 */
public enum Perm implements Permission
{
    COMMAND_FLY("command.fly", PermissionDefault.OP),
    COMMAND_FLY_BYPASS("command.fly.bypass", PermissionDefault.OP),
    FLY_FEAHTER("fly.feather", PermissionDefault.OP),
    FLY_BYPASS("fly.bypass", PermissionDefault.OP),
    ;
    private final String BASE = "cubeengine.fly.";
    private final String permission;
    private PermissionDefault permissionDefault;

    private Perm(String permission, PermissionDefault permissionDefault)
    {
        this.permission = BASE + permission;
        this.permissionDefault = permissionDefault;
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
        return this.permissionDefault;
    }
}
