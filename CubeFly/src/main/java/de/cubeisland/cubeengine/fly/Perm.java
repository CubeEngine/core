package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author Faithcaio
 */
public enum Perm implements Permission
{
    COMMAND_FLY("command.fly", PermissionDefault.OP),
    COMMAND_FLY_BYPASS("command.fly.bypass", PermissionDefault.OP);
    private String BASE = "cubeengine.fly.";
    private String permission;
    private PermissionDefault permissionDefault;

    private Perm(String permission, PermissionDefault permissionDefault)
    {
        this.permission = BASE + permission;
        this.permissionDefault = permissionDefault;
        CubeCore.getInstance().getPermissionRegistration().registerPermission(this);
    }

    public boolean isAuthorized(Player player)
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
