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
package org.cubeengine.service.permission;

import de.cubeisland.engine.modularity.core.Module;
import org.spongepowered.api.service.permission.PermissionDescription;

public abstract class PermissionContainer<T extends Module>
{
    public final T module;
    protected final PermissionManager pm;

    public PermissionContainer(T module)
    {
        this.module = module;
        pm = module.getModularity().provide(PermissionManager.class);
    }

    protected PermissionDescription register(String permission, String description, PermissionDescription parent, PermissionDescription... assigned)
    {
        return pm.register(module, permission, description, parent, assigned);
    }

    protected PermissionDescription registerS(String permission, String description, PermissionDescription parent, String... assigned)
    {
        return pm.registerS(module, permission, description, parent, assigned);
    }
}
