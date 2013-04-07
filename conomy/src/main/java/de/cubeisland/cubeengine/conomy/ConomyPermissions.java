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
package de.cubeisland.cubeengine.conomy;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.PermDefault;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

import org.bukkit.permissions.Permissible;

import java.util.Locale;

import static de.cubeisland.cubeengine.core.permission.PermDefault.OP;

public class ConomyPermissions extends PermissionContainer
{
    public ConomyPermissions(Module module)
    {
        super(module);
    }

    private static final Permission CONOMY = Permission.BASE.createAbstractChild("conomy");
    private static final Permission ACCOUNT = CONOMY.createAbstractChild("account");

    public static final Permission ACCOUNT_ALLOWUNDERMIN = ACCOUNT.createChild("allowundermin");
    public static final Permission ACCOUNT_SHOWHIDDEN = ACCOUNT.createChild("showhidden");

    public static final Permission COMMAND_PAY_FORCE = CONOMY.createChild("command.pay.force");
}
