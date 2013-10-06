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
package de.cubeisland.engine.signmarket;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;

public class MarketSignPerm extends PermissionContainer<Signmarket>
{
    public MarketSignPerm(Signmarket module, SignMarketCommands smCmds)
    {
        super(module);
        this.bindToModule(SIGN, SIGN_CREATE_USER_OTHER, USE, USER, USER_CREATE, ADMIN, ADMIN_CREATE);
        USER_CREATE.attach(SIGN_CREATE_USER, SIGN_CREATE_USER_BUY,  SIGN_CREATE_USER_SELL,
                           SIGN_CREATE_USER_DEMAND, SIGN_SIZE_CHANGE, SIGN_DESTROY_OWN,
                       Permission.createPermission(smCmds.getChild("editmode").getPermission())
                           );
        USE.attach(SIGN_INVENTORY_SHOW);
        USER.attach(USE, USER_CREATE);
        ADMIN_CREATE.attach(SIGN_CREATE_ADMIN, SIGN_CREATE_ADMIN_BUY, SIGN_CREATE_ADMIN_NOSTOCK, SIGN_CREATE_ADMIN_STOCK,
                     SIGN_CREATE_ADMIN_SELL, SIGN_SETSTOCK, SIGN_SIZE_CHANGE_INFINITE, SIGN_DESTROY_ADMIN);
        ADMIN.attach(ADMIN_CREATE, USER, SIGN_CREATE_USER_OTHER, SIGN_DESTROY_OTHER, SIGN_INVENTORY_ACCESS_OTHER);
        this.registerAllPermissions();
    }

    /**
     * Allow buying and selling to signs
     */
    private static final Permission USE = Permission.createPermission("use");

    public static final Permission USE_BUY = USE.createChild("buy");
    public static final Permission USE_SELL = USE.createChild("sell");
    /**
     * Allow creating user signs
     */
    public static final Permission USER_CREATE = Permission.createPermission("user-create");
    public static final Permission USER = Permission.createPermission("user");
    /**
     * Allow creating admin signs
     */
    public static final Permission ADMIN_CREATE = Permission.createPermission("admin-create");
    /**
     * full access
     */
    public static final Permission ADMIN = Permission.createPermission("admin");

    // -----------------------------------------------------------------------------

    private static final Permission SIGN = Permission.createAbstractPermission("sign");

    private static final Permission SIGN_DESTROY = SIGN.createAbstractChild("destroy");
    public static final Permission SIGN_DESTROY_OWN = SIGN_DESTROY.createChild("own");
    public static final Permission SIGN_DESTROY_ADMIN = SIGN_DESTROY.createChild("admin");
    public static final Permission SIGN_DESTROY_OTHER = SIGN_DESTROY.createChild("other");

    private static final Permission SIGN_INVENTORY = SIGN.createAbstractChild("inventory");
    public static final Permission SIGN_INVENTORY_SHOW = SIGN_INVENTORY.createChild("show");

    public static final Permission SIGN_INVENTORY_ACCESS_OTHER = SIGN_DESTROY.createChild("access.other");

    private static final Permission SIGN_CREATE = SIGN.createAbstractChild("create");

    /**
     * Allows creating and editing user-signs
     */
    public static final Permission SIGN_CREATE_USER = SIGN_CREATE.createChild("user");
    public static final Permission SIGN_CREATE_USER_BUY = SIGN_CREATE_USER.createChild("buy");
    public static final Permission SIGN_CREATE_USER_SELL = SIGN_CREATE_USER.createChild("sell");
    public static final Permission SIGN_CREATE_USER_DEMAND = SIGN_CREATE_USER.createChild("demand");

    public static final Permission SIGN_SIZE_CHANGE = SIGN.createChild("size.change");

    /**
     * Allows creating and editing admin-signs
     */
    public static final Permission SIGN_CREATE_ADMIN = SIGN_CREATE.createChild("admin");
    public static final Permission SIGN_CREATE_ADMIN_BUY = SIGN_CREATE_ADMIN.createChild("buy");
    public static final Permission SIGN_CREATE_ADMIN_SELL = SIGN_CREATE_ADMIN.createChild("sell");
    public static final Permission SIGN_CREATE_ADMIN_STOCK = SIGN_CREATE_ADMIN.createChild("stock");
    public static final Permission SIGN_CREATE_ADMIN_NOSTOCK = SIGN_CREATE_ADMIN.createChild("nostock");

    public static final Permission SIGN_SETSTOCK = SIGN.createChild("setstock");
    public static final Permission SIGN_SIZE_CHANGE_INFINITE = SIGN_SIZE_CHANGE.createChild("infinite");

    /**
     * Detached perm / allows to create & edit signs of an other user
     */
    public static final Permission SIGN_CREATE_USER_OTHER = SIGN_CREATE_USER.createNew("other");

}
