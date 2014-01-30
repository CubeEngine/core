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
package de.cubeisland.engine.basics.command.moderation;

import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.permission.Permission;

public class EntityRemoval
{
    private final Permission perm;
    public final Class<?>[] interfaces;

    EntityRemoval(Permission perm, Class<?>... interfaces)
    {
        this.perm = perm;
        this.interfaces = interfaces;
    }

    public boolean doesMatch(Entity entity)
    {
        if (interfaces.length == 0) return this.extra(entity);
        for (Class<?> anInterface : interfaces)
        {
            if (anInterface.isAssignableFrom(entity.getClass()))
            {
                return this.extra(entity);
            }
        }
        return false;
    }

    public boolean isAllowed(Permissible permissible)
    {
        return this.perm.isAuthorized(permissible);
    }

    /**
     * Override this to check extra information
     *
     * @param entity
     * @return
     */
    public boolean extra(Entity entity)
    {
        return true;
    }


}
