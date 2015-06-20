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
package de.cubeisland.engine.module.core.provider;

import de.cubeisland.engine.modularity.asm.marker.Provider;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.ValueProvider;
import de.cubeisland.engine.modularity.core.graph.DependencyInformation;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;
import de.cubeisland.engine.service.permission.Permission;

@Provider(Permission.class)
public class BasePermissionProvider implements ValueProvider<Permission>
{
    private Permission base = Permission.BASE;

    @Override
    public Permission get(DependencyInformation info, Modularity modularity)
    {
        if (info instanceof ModuleMetadata)
        {
            return base.childWildcard(((ModuleMetadata)info).getName());
        }
        throw new IllegalArgumentException(info.getIdentifier() + " is not a Module");
    }
}
