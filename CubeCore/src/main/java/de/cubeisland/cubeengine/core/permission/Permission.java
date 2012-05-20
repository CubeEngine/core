package de.cubeisland.cubeengine.core.permission;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

/**
 * Represents a permission
 * 
 * @author Phillip Schichtel
 */
public interface Permission
{
    public boolean isAuthorized(Permissible player);
    public String getPermission();
    public PermissionDefault getPermissionDefault();
}
