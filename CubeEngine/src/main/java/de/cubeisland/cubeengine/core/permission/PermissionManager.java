package de.cubeisland.cubeengine.core.permission;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.Cleanable;

/**
 * Registers permissions to the server.
 */
public interface PermissionManager extends Cleanable
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
     * Removes a permission of a module
     *
     * @param module the module
     * @param perm the permission
     */
    void removePermission(Module module, String perm);

    /**
     * Removes all the permissions of the given module
     *
     * @param module the module
     */
    void removePermissions(Module module);

    /**
     * Removes all the permissions
     */
    void removePermissions();

    /**
     * Returns the PermDefault for the given permission
     *
     * @param permission the permission to search for
     * @return the default value or null if the permission was not registered
     */
    PermDefault getDefaultFor(String permission);
}
