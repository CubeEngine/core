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

import de.cubeisland.engine.core.permission.ParentPermission;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionContainer;
import de.cubeisland.engine.core.permission.WildcardPermission;

public class MarketSignPerm extends PermissionContainer<Signmarket>
{
    public MarketSignPerm(Signmarket module, SignMarketCommands smCmds)
    {
        USER_CREATE.attach(SIGN_CREATE_USER, SIGN_CREATE_USER_BUY,  SIGN_CREATE_USER_SELL,
                           SIGN_CREATE_USER_DEMAND, SIGN_SIZE_CHANGE, SIGN_DESTROY_OWN,
                       Permission.create(smCmds.getChild("editmode").getPermission())
                           );
        USE.attach(SIGN_INVENTORY_SHOW, USE_BUY, USE_SELL);
        USER.attach(USE, USER_CREATE);
        ADMIN_CREATE.attach(SIGN_CREATE_ADMIN, SIGN_CREATE_ADMIN_BUY, SIGN_CREATE_ADMIN_NOSTOCK, SIGN_CREATE_ADMIN_STOCK,
                     SIGN_CREATE_ADMIN_SELL, SIGN_SETSTOCK, SIGN_SIZE_INFINITE, SIGN_DESTROY_ADMIN);
        ADMIN.attach(ADMIN_CREATE, USER, SIGN_CREATE_USER_OTHER, SIGN_DESTROY_OTHER, SIGN_INVENTORY_ACCESS_OTHER);
        this.registerAllPermissions(module);
    }

    /**
     * Allow buying and selling to signs
     */
    public static final ParentPermission USE = Permission.createParent("use");

    public static final Permission USE_BUY = USE.newPerm("buy");
    public static final Permission USE_SELL = USE.newPerm("sell");
    /**
     * Allow creating user signs
     */
    public static final ParentPermission USER_CREATE = Permission.createParent("user-create");
    public static final ParentPermission USER = Permission.createParent("user");
    /**
     * Allow creating admin signs
     */
    public static final ParentPermission ADMIN_CREATE = Permission.createParent("admin-create");
    /**
     * full access
     */
    public static final ParentPermission ADMIN = Permission.createParent("admin");

    // -----------------------------------------------------------------------------

    private static final WildcardPermission SIGN = Permission.createWildcard("sign");

    private static final WildcardPermission SIGN_DESTROY = SIGN.childWildcard("destroy");
    public static final Permission SIGN_DESTROY_OWN = SIGN_DESTROY.child("own");
    public static final Permission SIGN_DESTROY_ADMIN = SIGN_DESTROY.child("admin");
    public static final Permission SIGN_DESTROY_OTHER = SIGN_DESTROY.child("other");

    private static final WildcardPermission SIGN_INVENTORY = SIGN.childWildcard("inventory");
    public static final Permission SIGN_INVENTORY_SHOW = SIGN_INVENTORY.child("show");

    public static final Permission SIGN_INVENTORY_ACCESS_OTHER = SIGN_DESTROY.child("access.other");

    private static final WildcardPermission SIGN_CREATE = SIGN.childWildcard("create");

    /**
     * Allows creating and editing user-signs
     */
    private static final WildcardPermission SIGN_CREATE_USER = SIGN_CREATE.childWildcard("user");
    public static final Permission SIGN_CREATE_USER_CREATE = SIGN_CREATE_USER.child("create");
    public static final Permission SIGN_CREATE_USER_BUY = SIGN_CREATE_USER.child("buy");
    public static final Permission SIGN_CREATE_USER_SELL = SIGN_CREATE_USER.child("sell");
    public static final Permission SIGN_CREATE_USER_DEMAND = SIGN_CREATE_USER.child("demand");

    private static final WildcardPermission SIGN_SIZE = SIGN.childWildcard("size");
    public static final Permission SIGN_SIZE_CHANGE = SIGN_SIZE.child("change");

    /**
     * Allows creating and editing admin-signs
     */
    private static final WildcardPermission SIGN_CREATE_ADMIN = SIGN_CREATE.childWildcard("admin");
    public static final Permission SIGN_CREATE_ADMIN_CREATE = SIGN_CREATE_ADMIN.child("create");
    public static final Permission SIGN_CREATE_ADMIN_BUY = SIGN_CREATE_ADMIN.child("buy");
    public static final Permission SIGN_CREATE_ADMIN_SELL = SIGN_CREATE_ADMIN.child("sell");
    public static final Permission SIGN_CREATE_ADMIN_STOCK = SIGN_CREATE_ADMIN.child("stock");
    public static final Permission SIGN_CREATE_ADMIN_NOSTOCK = SIGN_CREATE_ADMIN.child("nostock");

    public static final Permission SIGN_SETSTOCK = SIGN.child("setstock");
    public static final Permission SIGN_SIZE_INFINITE = SIGN_SIZE.child("infinite");

    /**
     * Detached perm / allows to create & edit signs of an other user
     */
    public static final Permission SIGN_CREATE_USER_OTHER = SIGN_CREATE_USER.newPerm("other");

}
