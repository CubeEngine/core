package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author Faithcaio
 */
public enum Perm implements Permission
{
    COMMAND_ENCHANT_UNSAFE("command.enchant.unsafe", PermissionDefault.OP),
    ;
    private String BASE = "cubeengine.basics.";
    private String permission;
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
