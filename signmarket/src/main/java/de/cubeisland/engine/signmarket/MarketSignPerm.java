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
    public MarketSignPerm(Signmarket module)
    {
        super(module);
        this.bindToModule(SIGN);
        this.registerAllPermissions();
    }

    private static final Permission SIGN = Permission.createAbstractPermission("sign");

    private static final Permission SIGN_DESTROY = SIGN.createAbstractChild("destroy");
    public static final Permission SIGN_DESTROY_OWN = SIGN_DESTROY.createChild("own");
    public static final Permission SIGN_DESTROY_ADMIN = SIGN_DESTROY.createChild("admin");
    public static final Permission SIGN_DESTROY_OTHER = SIGN_DESTROY.createChild("other");

    private static final Permission SIGN_INVENTORY = SIGN.createAbstractChild("inventory");
    public static final Permission SIGN_INVENTORY_SHOW = SIGN_INVENTORY.createChild("show");
    public static final Permission SIGN_INVENTORY_ACCESS_OTHER = SIGN_DESTROY.createChild("access.other");

    public static final Permission SIGN_CREATE = SIGN.createChild("create");

    public static final Permission SIGN_CREATE_USER = SIGN_CREATE.createChild("user");
    public static final Permission SIGN_CREATE_USER_OTHER = SIGN_CREATE_USER.createChild("other");
    public static final Permission SIGN_CREATE_USER_BUY = SIGN_CREATE_USER.createChild("buy");
    public static final Permission SIGN_CREATE_USER_SELL = SIGN_CREATE_USER.createChild("sell");

    public static final Permission SIGN_CREATE_ADMIN = SIGN_CREATE.createChild("admin");
    public static final Permission SIGN_CREATE_ADMIN_BUY = SIGN_CREATE_ADMIN.createChild("buy");
    public static final Permission SIGN_CREATE_ADMIN_SELL = SIGN_CREATE_ADMIN.createChild("sell");
    public static final Permission SIGN_CREATE_ADMIN_STOCK = SIGN_CREATE_ADMIN.createChild("stock");
    public static final Permission SIGN_CREATE_ADMIN_NOSTOCK = SIGN_CREATE_ADMIN.createChild("nostock");

    public static final Permission SIGN_EDIT = SIGN.createChild("edit");
    public static final Permission SIGN_SETSTOCK = SIGN.createChild("setstock");
    public static final Permission SIGN_SIZE_CHANGE = SIGN.createChild("size.change");
    public static final Permission SIGN_SIZE_CHANGE_INFINITE = SIGN_SIZE_CHANGE.createChild("infinite");
}
