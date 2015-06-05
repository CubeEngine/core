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
package de.cubeisland.engine.module.service.permission;

import java.util.Map;
import com.google.common.base.Optional;
import de.cubeisland.engine.modularity.asm.marker.Service;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.util.Cleanable;

/**
 * Registers permissions to the server.
 */
@Service
@Version(1)
public interface PermissionManager extends Cleanable
{
    /**
     * Registers a permission
     *
     * @param permission the permission
     */
    void registerPermission(Module module, Permission permission);

    /**
     * Removes a permission of a module
     *
     * @param module the module
     * @param perm   the permission
     */
    void removePermission(Module module, String perm);

    /**
     * Removes all the permissions of the given module
     *
     * @param module the module
     */
    void removePermissions(Module module);

    /**
     * Removes a permission of a module
     *
     * @param module     the module
     * @param permission the permission
     */
    void removePermission(Module module, Permission permission);

    /**
     * Returns the permission node with given name or {@link Optional#absent()} if not found
     * @param permission the permissions name
     * @return the permission if found
     */
    Optional<Permission> getPermission(String permission);

    /**
     * Returns a unmodifiable map of all registered permissions
     * @return the registered permissions
     */
    Map<String, Permission> getPermissions();
}
