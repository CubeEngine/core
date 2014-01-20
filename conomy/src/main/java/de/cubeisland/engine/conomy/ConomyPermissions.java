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
package de.cubeisland.engine.conomy;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;
import de.cubeisland.engine.core.permission.WildcardPermission;

public class ConomyPermissions extends PermissionContainer<Conomy>
{
    public ConomyPermissions(Conomy module)
    {
        this.registerAllPermissions(module);
    }

    private static final WildcardPermission ACCOUNT = Permission.createWildcard("account");
    private static final WildcardPermission ACCOUNT_USER = ACCOUNT.childWildcard("user");

    public static final Permission USER_ALLOWUNDERMIN = ACCOUNT_USER.child("allow-under-min");
    public static final Permission USER_SHOWHIDDEN = ACCOUNT_USER.child("show-hidden");

    public static final Permission BANK_SHOWHIDDEN = ACCOUNT.childWildcard("bank").child("show-hidden");

    private static final WildcardPermission COMMAND = Permission.createWildcard("command");
    private static final WildcardPermission COMMAND_MONEY_PAY = COMMAND.childWildcard("money").childWildcard("pay");
    public static final Permission COMMAND_MONEY_PAY_FORCE = COMMAND_MONEY_PAY.child("force");
    public static Permission COMMAND_PAY_ASOTHER = COMMAND_MONEY_PAY.child("as-other");

    private static final WildcardPermission COMMAND_BANK =  COMMAND.childWildcard("bank");
    public static final Permission COMMAND_BANK_BALANCE_SHOWHIDDEN = COMMAND_BANK.childWildcard("balance").child("show-hidden");

    public static final Permission COMMAND_BANK_LISTINVITES_OTHER = COMMAND_BANK.childWildcard("listinvites").child("force");

    public static final Permission COMMAND_BANK_UNINVITE_FORCE = COMMAND_BANK.childWildcard("uninvite").child("force");
    public static final Permission COMMAND_BANK_INVITE_FORCE = COMMAND_BANK.childWildcard("invite").child("force");

    private static WildcardPermission COMMAND_BANK_JOIN = COMMAND_BANK.childWildcard("join");

    public static Permission COMMAND_BANK_JOIN_FORCE = COMMAND_BANK_JOIN.child("force");
    public static Permission COMMAND_BANK_JOIN_OTHER = COMMAND_BANK_JOIN.child("other");
    public static Permission COMMAND_BANK_LEAVE_OTHER = COMMAND_BANK.childWildcard("leave").child("other");
    public static Permission COMMAND_BANK_RENAME_FORCE = COMMAND_BANK.childWildcard("rename").child("force");

    public static Permission COMMAND_BANK_SETOWNER_FORCE = COMMAND_BANK.childWildcard("setowner").child("force");

    public static Permission COMMAND_BANK_DEPOSIT_FORCE = COMMAND_BANK.childWildcard("deposit").child("force");
    public static Permission COMMAND_BANK_WITHDRAW_FORCE = COMMAND_BANK.childWildcard("withdraw").child("force");
    public static Permission COMMAND_BANK_PAY_FORCE  = COMMAND_BANK.childWildcard("pay").child("force");

    private static WildcardPermission COMMAND_BANK_DELETE = COMMAND_BANK.childWildcard("delete");
    public static final Permission COMMAND_BANK_DELETE_OWN = COMMAND_BANK_DELETE.child("own");
    public static final Permission COMMAND_BANK_DELETE_OTHER = COMMAND_BANK_DELETE.child("other");

    private static final WildcardPermission COMMAND_ECO_CREATE = COMMAND.childWildcard("eco").childWildcard("create");
    public static final Permission ECO_CREATE_OTHER = COMMAND_ECO_CREATE.child("other");
    public static final Permission ECO_CREATE_FORCE = COMMAND_ECO_CREATE.child("force");
}
