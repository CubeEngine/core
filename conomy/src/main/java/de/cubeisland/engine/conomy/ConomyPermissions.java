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

public class ConomyPermissions extends PermissionContainer<Conomy>
{
    public ConomyPermissions(Conomy module)
    {
        super(module);
        this.registerAllPermissions();
    }

    private final Permission ACCOUNT = getBasePerm().childWildcard("account");
    private final Permission ACCOUNT_USER = ACCOUNT.childWildcard("user");

    public final Permission USER_ALLOWUNDERMIN = ACCOUNT_USER.child("allow-under-min");
    public final Permission USER_SHOWHIDDEN = ACCOUNT_USER.child("show-hidden");

    public final Permission BANK_SHOWHIDDEN = ACCOUNT.childWildcard("bank").child("show-hidden");

    private final Permission COMMAND = getBasePerm().childWildcard("command");
    private final Permission COMMAND_MONEY_PAY = COMMAND.childWildcard("money").childWildcard("pay");
    public final Permission COMMAND_MONEY_PAY_FORCE = COMMAND_MONEY_PAY.child("force");
    public Permission COMMAND_PAY_ASOTHER = COMMAND_MONEY_PAY.child("as-other");

    private final Permission COMMAND_BANK =  COMMAND.childWildcard("bank");
    public final Permission COMMAND_BANK_BALANCE_SHOWHIDDEN = COMMAND_BANK.childWildcard("balance").child("show-hidden");

    public final Permission COMMAND_BANK_LISTINVITES_OTHER = COMMAND_BANK.childWildcard("listinvites").child("force");

    public final Permission COMMAND_BANK_UNINVITE_FORCE = COMMAND_BANK.childWildcard("uninvite").child("force");
    public final Permission COMMAND_BANK_INVITE_FORCE = COMMAND_BANK.childWildcard("invite").child("force");

    private Permission COMMAND_BANK_JOIN = COMMAND_BANK.childWildcard("join");

    public Permission COMMAND_BANK_JOIN_FORCE = COMMAND_BANK_JOIN.child("force");
    public Permission COMMAND_BANK_JOIN_OTHER = COMMAND_BANK_JOIN.child("other");
    public Permission COMMAND_BANK_LEAVE_OTHER = COMMAND_BANK.childWildcard("leave").child("other");
    public Permission COMMAND_BANK_RENAME_FORCE = COMMAND_BANK.childWildcard("rename").child("force");

    public Permission COMMAND_BANK_SETOWNER_FORCE = COMMAND_BANK.childWildcard("setowner").child("force");

    public Permission COMMAND_BANK_DEPOSIT_FORCE = COMMAND_BANK.childWildcard("deposit").child("force");
    public Permission COMMAND_BANK_WITHDRAW_FORCE = COMMAND_BANK.childWildcard("withdraw").child("force");
    public Permission COMMAND_BANK_PAY_FORCE  = COMMAND_BANK.childWildcard("pay").child("force");

    private Permission COMMAND_BANK_DELETE = COMMAND_BANK.childWildcard("delete");
    public final Permission COMMAND_BANK_DELETE_OWN = COMMAND_BANK_DELETE.child("own");
    public final Permission COMMAND_BANK_DELETE_OTHER = COMMAND_BANK_DELETE.child("other");

    private final Permission COMMAND_ECO_CREATE = COMMAND.childWildcard("eco").childWildcard("create");
    public final Permission ECO_CREATE_OTHER = COMMAND_ECO_CREATE.child("other");
    public final Permission ECO_CREATE_FORCE = COMMAND_ECO_CREATE.child("force");
}
