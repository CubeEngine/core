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
package de.cubeisland.engine.module.core.permission;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import de.cubeisland.engine.modularity.core.Module;

public abstract class PermissionContainer<T extends Module>
{
    public final T module;

    public PermissionContainer(T module)
    {
        this.module = module;
    }

    private Set<Permission> getPermissions()
    {
        HashSet<Permission> perms = new HashSet<>();
        for (Field field : this.getClass().getFields())
        {
            int mask = field.getModifiers();
            if (!((mask & Modifier.STATIC) == Modifier.STATIC)) // ignore static
            {
                if (Permission.class.isAssignableFrom(field.getType()))
                {
                    try
                    {
                        perms.add((Permission)field.get(this));
                    }
                    catch (IllegalAccessException ignored)
                    {}
                }
            }
        }
        return perms;
    }

    public void registerAllPermissions()
    {
        for (Permission perm : getPermissions())
        {
            module.getModulatiry().start(PermissionManager.class).registerPermission(module, perm);
        }
    }

    public final Permission getBasePerm()
    {
        return module.getProvided(Permission.class);
    }
}
