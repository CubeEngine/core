package de.cubeisland.cubeengine.core.permission;

import de.cubeisland.cubeengine.core.BukkitDependend;
import org.bukkit.permissions.PermissionDefault;
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
     * Registers a String as a permission
     *
     * @param perm the permission node
     * @param permDefault the default value
     * @return fluent interface
     */
    public PermissionRegistration registerPermission(String perm, PermissionDefault permDefault)
    {
        try
        {
            this.pm.addPermission(new org.bukkit.permissions.Permission(perm, permDefault));
        }
        catch (IllegalArgumentException e)
        {}
        
        return this;
    }

    /**
     * Registeres a permission
     *
     * @param permission the permission
     * @return fluent interface
     */
    public PermissionRegistration registerPermission(Permission permission)
    {
        return this.registerPermission(permission.getPermission(), permission.getPermissionDefault());
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