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
package de.cubeisland.engine.roles.role;

import java.nio.file.Path;

import org.bukkit.World;

public class GlobalRoleProvider extends RoleProvider
{
    public GlobalRoleProvider(RolesManager manager)
    {
        super(manager, manager.module.getBasePermission().childWildcard("global"));
    }

    @Override
    protected Path getFolder()
    {
        return this.manager.getRolesFolder();
    }

    @Override
    public void recalculateRoles()
    {
        if (this.roles.isEmpty())
        {
            return;
        }
        this.module.getLog().debug("Calculating global Roles...");
        super.recalculateRoles();
    }

    @Override
    public Role getRole(String name)
    {
        assert name != null: "The role name may not be null!";

        name = name.toLowerCase();
        if (name.startsWith("g:"))
        {
            return super.getRole(name.substring(2));
        }
        return super.getRole(name);
    }

    @Override
    public World getMainWorld()
    {
        return null;
    }
}
