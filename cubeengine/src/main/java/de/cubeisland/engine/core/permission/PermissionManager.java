/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.permission;

import java.util.Set;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.Cleanable;

/**
 * Registers permissions to the server.
 */
public interface PermissionManager extends Cleanable
{
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

    /**
     * Removes a permission of a module
     *
     * @param module the module
     * @param permission the permission
     */
    void removePermission(Module module, Permission permission);
}
