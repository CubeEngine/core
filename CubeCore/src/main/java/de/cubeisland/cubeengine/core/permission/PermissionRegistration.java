package de.cubeisland.cubeengine.core.permission;

import de.cubeisland.cubeengine.BukkitDependend;
import org.bukkit.plugin.PluginManager;

/**
 * Registrates Permissions to the server
 *
 * @author Phillip Schichtel
 */
@BukkitDependend("Uses Bukkit's permission API")
public class PermissionRegistration
{
    private final PluginManager pm;

    public PermissionRegistration(PluginManager pm)
    {
        this.pm = pm;
    }

    /**
     * Registeres a permission
     *
     * @param permission the permission
     * @return fluent interface
     */
    public PermissionRegistration registerPermission(Permission permission)
    {
        this.pm.addPermission(new org.bukkit.permissions.Permission(permission.getPermission(), permission.getPermissionDefault()));
        return this;
    }

    /**
     * Registered an array of permissions
     *
     * @param permissions the array of permissions
     * @return fluent interface
     */
    public PermissionRegistration registerPermissions(Permission[] permissions)
    {
        for (Permission permission : permissions)
        {
            this.registerPermission(permission);
        }
        return this;
    }
}