package de.cubeisland.cubeengine.core.permission;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author Faithcaio
 */
public class CubePermission implements Permission
{

    private String permission;
    private PermissionDefault permissionDefault;
    
    public CubePermission(String permission, PermissionDefault permissionDefault)
    {
        this.permission = permission;
        this.permissionDefault = permissionDefault;
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
