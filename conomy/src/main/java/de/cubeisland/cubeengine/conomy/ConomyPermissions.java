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

import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

public class ConomyPermissions extends PermissionContainer<Conomy>
{
    public ConomyPermissions(Conomy module)
    {
        super(module);
        this.bindToModule(ACCOUNT,COMMAND);
        this.registerAllPermissions();
    }

    private static final Permission ACCOUNT = Permission.createAbstractPermission("account");

    public static final Permission ACCOUNT_ALLOWUNDERMIN = ACCOUNT.createChild("allowundermin");
    public static final Permission ACCOUNT_SHOWHIDDEN = ACCOUNT.createChild("showhidden");

    private static final Permission COMMAND = Permission.createAbstractPermission("command");
    public static final Permission COMMAND_PAY_FORCE = COMMAND.createAbstractChild("pay").createChild("force");

    private static final Permission COMMAND_ECO_CREATE = COMMAND.createAbstractChild("eco").createAbstractChild("create");
    public static final Permission ECO_CREATE_OTHER = COMMAND_ECO_CREATE.createChild("other");
    public static final Permission ECO_CREATE_FORCE = COMMAND_ECO_CREATE.createChild("force");
}
