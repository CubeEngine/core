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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import de.cubeisland.engine.core.module.Module;

import gnu.trove.set.hash.THashSet;

public abstract class PermissionContainer<T extends Module>
{
    private Set<Permission> getPermissions()
    {
        THashSet<Permission> perms = new THashSet<>();
        for (Field field : this.getClass().getFields())
        {
            int mask = field.getModifiers();
            if ((((mask & Modifier.STATIC) == Modifier.STATIC)))
            {
                if (Permission.class.isAssignableFrom(field.getType()))
                {
                    try
                    {
                        Permission perm = (Permission)field.get(this);
                        if (!(perm instanceof WildcardPermission))
                        {
                            perms.add(perm);
                        }
                    }
                    catch (IllegalAccessException ignored)
                    {}
                }
            }
        }
        return perms;
    }

    public void registerAllPermissions(T module)
    {
        Set<Permission> permissions = getPermissions();
        WildcardPermission basePerm = module.getBasePermission();
        // First make sure all permissions are child-perms of the modules base-perm
        for (Permission perm : permissions)
        {
            if (perm.parent == null)
            {
                basePerm.addChild(perm);
            }
            // else perm is a sub-perm
        }
        // Now register the permissions
        for (Permission perm : permissions)
        {
            module.getCore().getPermissionManager().registerPermission(module, perm);
        }
    }
}
