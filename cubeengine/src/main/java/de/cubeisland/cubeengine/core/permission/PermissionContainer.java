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
package de.cubeisland.cubeengine.core.permission;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import de.cubeisland.cubeengine.core.module.Module;

import gnu.trove.set.hash.THashSet;

public abstract class PermissionContainer
{
    private final PermissionManager permissionManager;
    private final Module module;


    protected PermissionContainer(Module module)
    {
        this.permissionManager = module.getCore().getPermissionManager();
        this.module = module;
    }

    /**
     * Nulls all static fields in this
     */
    public void cleanup()
    {
        for (Field field : this.getClass().getDeclaredFields())
        {
            int mask = field.getModifiers();
            if ((((mask & Modifier.STATIC) == Modifier.STATIC)))
            {
                if (Permission.class.isAssignableFrom(field.getType()))
                {
                    try
                    {
                        field.set(this,null);
                    }
                    catch (IllegalAccessException e)
                    {
                    }
                }
            }
        }
    }

    public Set<Permission> getPermissions()
    {
        THashSet<Permission> perms = new THashSet<Permission>();
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
                        if (perm.canRegister)
                        {
                            perms.add(perm);
                        }
                    }
                    catch (IllegalAccessException e)
                    {
                    }
                }
            }
        }
        return perms;
    }

    public void registerAllPermissions()
    {
        String prefix = "cubeengine." + this.module.getId()+ ".";
        for (Permission perm : getPermissions())
        {
            if (!perm.getName().startsWith(prefix))
            {
                throw new IllegalArgumentException("Permissions must start with 'cubeengine.<module>' !");
            }
            this.permissionManager.registerPermission(module,perm);
        }
    }
}
