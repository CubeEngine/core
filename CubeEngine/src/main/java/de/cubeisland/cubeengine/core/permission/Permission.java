package de.cubeisland.cubeengine.core.permission;

import org.bukkit.permissions.Permissible;

/**
 * Represents a permission.
 */
public interface Permission
{
    public static final String BASE = "cubeengine.";

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
    public PermDefault getPermissionDefault();
}
