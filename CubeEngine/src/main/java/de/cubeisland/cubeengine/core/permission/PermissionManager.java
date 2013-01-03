package de.cubeisland.cubeengine.core.permission;

import de.cubeisland.cubeengine.core.module.Module;

/**
 * Registers permissions to the server.
 */
public interface PermissionManager
{
    /**
     * Registers a String as a permission
     *
     * @param perm        the permission node
     * @param permDefault the default valueW
     */
    void registerPermission(Module module, String perm, PermDefault permDefault);

    /**
     * Registers a permission
     *
     * @param permission the permission
     */
    void registerPermission(Module module, Permission permission);

    /**
     * Registered an array of permissions
     *
     * @param permissions the array of permissions
     */
    void registerPermissions(Module module, Permission[] permissions);

    /**
     * Unregisters a permission of a module
     *
     * @param module the module
     * @param perm the permission
     */
    void unregisterPermission(Module module, String perm);

    /**
     * Unregisters all the permissions of the given module
     *
     * @param module the module
     */
    void unregisterPermissions(Module module);
}
