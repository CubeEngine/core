package de.cubeisland.cubeengine.core.permission;

import java.util.Locale;

import org.bukkit.permissions.Permissible;

import de.cubeisland.cubeengine.core.CubeEngine;

/**
 * Represents a permission.
 */
public interface Permission
{
    public static final String BASE = CubeEngine.class.getSimpleName().toLowerCase(Locale.US);

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
