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
package org.cubeengine.service.command.property;

import de.cubeisland.engine.modularity.core.Module;
import org.cubeengine.service.permission.PermissionManager;
import org.spongepowered.api.service.permission.PermissionDescription;

public class RawPermission
{
    private boolean registered = false;
    private PermissionDescription register;
    private String[] groups;

    public RawPermission(String permission, String description)
    {
        this.name = permission;
        this.description = description;
    }

    public RawPermission assign(String[] groups)
    {
        this.groups = groups == null ? new String[0] : groups;
        return this;
    }

    private String name;
    private String description;

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public PermissionDescription registerPermission(Module module, PermissionManager pm, PermissionDescription parent)
    {
        if (!registered)
        {
            String modulePerm = pm.getModulePermission(module).getId();
            for (int i = 0; i < groups.length; i++)
            {
                groups[i] = modulePerm + "." + groups[i];
            }

            this.register = pm.registerS(module, name, description == null ? "" : description, parent, groups);
            name = register.getId();
            registered = true;
        }
        return register;
    }

    public RawPermission fallbackDescription(String desc)
    {
        if (description == null)
        {
            description = desc;
        }
        return this;
    }

    public boolean isRegistered()
    {
        return registered;
    }

    public PermissionDescription getRegistered()
    {
        return register;
    }
}
