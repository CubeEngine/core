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
    private static final Permission ACCOUNT_USER = ACCOUNT.createAbstractChild("user");

    public static final Permission USER_ALLOWUNDERMIN = ACCOUNT_USER.createChild("allow-under-min");
    public static final Permission USER_SHOWHIDDEN = ACCOUNT_USER.createChild("show-hidden");

    public static final Permission BANK_SHOWHIDDEN = ACCOUNT.createAbstractChild("bank").createChild("show-hidden");

    private static final Permission COMMAND = Permission.createAbstractPermission("command");
    private static final Permission COMMAND_MONEY_PAY = COMMAND.createAbstractChild("money").createAbstractChild("pay");
    public static final Permission COMMAND_MONEY_PAY_FORCE = COMMAND_MONEY_PAY.createChild("force");
    public static Permission COMMAND_PAY_ASOTHER = COMMAND_MONEY_PAY.createChild("as-other");

    private static final Permission COMMAND_BANK =  COMMAND.createAbstractChild("bank");
    public static final Permission COMMAND_BANK_BALANCE_SHOWHIDDEN = COMMAND_BANK.createAbstractChild("balance").createChild("show-hidden");

    public static final Permission COMMAND_BANK_LISTINVITES_OTHER = COMMAND_BANK.createAbstractChild("listinvites").createChild("force");

    public static final Permission COMMAND_BANK_INVITE_FORCE = COMMAND_BANK.createAbstractChild("invite").createChild("force");

    private static Permission COMMAND_BANK_JOIN = COMMAND_BANK.createAbstractChild("join");

    public static Permission COMMAND_BANK_JOIN_FORCE = COMMAND_BANK_JOIN.createChild("force");
    public static Permission COMMAND_BANK_JOIN_OTHER = COMMAND_BANK_JOIN.createChild("other");
    public static Permission COMMAND_BANK_LEAVE_OTHER = COMMAND_BANK.createAbstractChild("leave").createChild("other");
    public static Permission COMMAND_BANK_RENAME_FORCE = COMMAND_BANK.createAbstractChild("rename").createChild("force");

    public static Permission COMMAND_BANK_SETOWNER_FORCE = COMMAND_BANK.createAbstractChild("setowner").createChild("force");

    public static Permission COMMAND_BANK_DEPOSIT_FORCE = COMMAND_BANK.createAbstractChild("deposit").createChild("force");
    public static Permission COMMAND_BANK_WITHDRAW_FORCE = COMMAND_BANK.createAbstractChild("withdraw").createChild("force");
    public static Permission COMMAND_BANK_PAY_FORCE  = COMMAND_BANK.createAbstractChild("pay").createChild("force");

    private static Permission COMMAND_BANK_DELETE = COMMAND_BANK.createAbstractChild("delete");
    public static final Permission COMMAND_BANK_DELETE_OWN = COMMAND_BANK_DELETE.createChild("own");
    public static final Permission COMMAND_BANK_DELETE_OTHER = COMMAND_BANK_DELETE.createChild("other");

    private static final Permission COMMAND_ECO_CREATE = COMMAND.createAbstractChild("eco").createAbstractChild("create");
    public static final Permission ECO_CREATE_OTHER = COMMAND_ECO_CREATE.createChild("other");
    public static final Permission ECO_CREATE_FORCE = COMMAND_ECO_CREATE.createChild("force");
}
