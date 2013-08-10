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

import java.io.IOException;
import java.nio.file.Path;

import de.cubeisland.engine.roles.Roles;
import org.jooq.impl.DSL;

import static de.cubeisland.engine.roles.storage.TableRole.TABLE_ROLE;

public class GlobalRoleProvider extends RoleProvider
{
    public GlobalRoleProvider(Roles module, RolesManager manager)
    {
        super(module, manager, 0);
        this.basePerm = module.getBasePermission().createAbstractChild("global");
    }

    @Override
    public Path getFolder()
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
    protected boolean renameRole(Role role, String newName)
    {
        if (super.renameRole(role,newName))
        {
            this.manager.dsl.update(TABLE_ROLE).set(DSL.row(TABLE_ROLE.ROLENAME), DSL.row("g:" + newName)).
                where(TABLE_ROLE.ROLENAME.eq("g:" + role.getName())).execute();
            return true;
        }
        return false;
    }

    @Override
    public Role getRole(String name)
    {
        if (name.startsWith("g:"))
        {
            return super.getRole(name.substring(2));
        }
        return super.getRole(name);
    }
}
