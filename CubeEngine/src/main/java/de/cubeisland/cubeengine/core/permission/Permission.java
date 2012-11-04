package de.cubeisland.cubeengine.core.permission;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;

/**
 * Represents a permission.
 */
public interface Permission
{
    /**
     * Checks whether the given Permissible is authorized with this permission
     *
     * @param permissible
     * @return true
     */
    public boolean isAuthorized(Permissible permissible);

    /**
     * Gets the permission as a string
     *
     * @return the permission string
     */
    public String getPermission();

    /**
     * Returns the permission default
     *
     * @return the permission default
     */
    public PermissionDefault getPermissionDefault();
}