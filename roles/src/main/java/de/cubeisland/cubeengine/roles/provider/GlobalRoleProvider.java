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
package de.cubeisland.cubeengine.roles.provider;

import java.io.File;
import java.util.Locale;

import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.ConfigRole;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;

public class GlobalRoleProvider extends RoleProvider
{
    public GlobalRoleProvider(Roles module)
    {
        super(module, true);
        this.createBasePerm();
    }

    @Override
    public void createBasePerm()
    {
        this.basePerm = this.module.getBasePermission().createAbstractChild("global");
    }

    @Override
    public void loadInConfigurations(File rolesFolder)
    {
        this.module.getLog().log(DEBUG, "Loading global roles...");
        if (this.init) // provider is already initialized!
        {
            return;
        }
        if (this.folder == null)
        {
            // Sets the folder for this provider
            this.folder = rolesFolder;
        }
        super.loadInConfigurations(rolesFolder);
    }

    @Override
    public ConfigRole getRole(String roleName)
    {
        if (roleName == null)
        {
            return null;
        }
        if (roleName.startsWith("g:"))
        {
            roleName = roleName.substring(2);
        }
        return this.roles.get(roleName.toLowerCase(Locale.ENGLISH));
    }
}
