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

import java.io.File;

import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.core.CubeEngine;

public class SignMarketConfig extends YamlConfiguration
{

    @Name("sign.admin.enable")
    public boolean enableAdmin = true;
    @Name("sign.user.enable")
    public boolean enableUser = true;

    @Name("sign.admin.stock.allow")
    public boolean allowAdminStock = true;
    @Name("sign.admin.no-stock.allow")
    public boolean allowAdminNoStock = true;

    @Name("sign.overstack.in-sign")
    @Comment("Allows items in signs to be overstacked up to 64.")
    public boolean allowOverStackedInSign = false;

    @Name("sign.overstack.out-sign")
    @Comment("Allows items taken out of signs to be overstacked up to 64.")
    public boolean allowOverStackedOutOfSign = false;

    @Comment("The maximum amount of inventory-lines a admin-sign can have.\n" +
            "Use -1 for infinite stock-size OR values from 1-6!")
    @Name("sign.admin.stock.max")
    public int maxAdminStock = -1;

    @Name("sign.admin.stock.buy-if-empty.enable")
    public boolean allowBuyIfAdminSignIsEmpty = true;

    @Comment("Prices of admin signs will me multiplied by this factor if their stock is empty.")
    @Name("sign.admin.stock.buy-if-empty.fee")
    public float factorIfAdminSignIsEmpty = 10;

    @Comment("The maximum amount of inventory-lines a user-sign can have.\n" +
            "Use -1 for infinite stock-size OR values from 1-6!")
    @Name("sign.user.stock.max")
    public int maxUserStock = 6;

    @Override
    public void onLoaded(File loadFrom)
    {
        if (!allowAdminNoStock && !allowAdminStock)
        {
            this.enableAdmin = false;
        }
        if (!enableAdmin && ! enableUser)
        {
            CubeEngine.getCore().getLog().warn("[MarketSign] All SignTypes are disabled in the configuration!");
        }
    }
}
