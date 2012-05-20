package de.cubeisland.cubeengine.core.permission;

import org.bukkit.plugin.PluginManager;

/**
 * Registrates Permissions
 *
 * @author CodeInfection
 */
public class PermissionRegistration
{
    private final PluginManager pm;

    public PermissionRegistration(PluginManager pm)
    {
        this.pm = pm;
    }

    public PermissionRegistration registerPermission(Permission permission)
    {
        this.pm.addPermission(new org.bukkit.permissions.Permission(permission.getPermission(), permission.getPermissionDefault()));

        return this;
    }

    public PermissionRegistration registerPermissions(Permission[] permissions)
    {
        for (Permission permission : permissions)
        {
            this.registerPermission(permission);
        }
        return this;
    }
}
