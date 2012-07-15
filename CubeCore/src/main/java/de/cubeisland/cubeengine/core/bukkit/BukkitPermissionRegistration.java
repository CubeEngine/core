package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import org.bukkit.plugin.PluginManager;

/**
 * Registrates Permissions to the server
 *
 * @author Phillip Schichtel
 */
public class BukkitPermissionRegistration implements PermissionRegistration
{
    private final PluginManager pm;

    public BukkitPermissionRegistration(PluginManager pm)
    {
        this.pm = pm;
    }

    /**
     * Registeres a permission
     *
     * @param permission the permission
     * @return fluent interface
     */
    public void registerPermission(Permission permission)
    {
        this.pm.addPermission(new org.bukkit.permissions.Permission(permission.getPermission(), permission.getPermissionDefault()));
    }

    /**
     * Registered an array of permissions
     *
     * @param permissions the array of permissions
     * @return fluent interface
     */
    public void registerPermissions(Permission[] permissions)
    {
        for (Permission permission : permissions)
        {
            this.registerPermission(permission);
        }
    }
}
