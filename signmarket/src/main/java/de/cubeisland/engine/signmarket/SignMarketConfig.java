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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.World;

import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.world.WorldManager;
import org.jooq.types.UInteger;

public class  SignMarketConfig extends YamlConfiguration
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

    public List<String> disableInWorlds = new ArrayList<>();

    @Comment({"If empty all signs in all worlds can sync.",
    "Example:",
    "world:",
    "  - world_the_end",
    "  - world_nether",
    "world2:",
    "  - world2_the_end",
    "  - world2_nether",})
    public Map<String, List<String>> syncWorlds = new HashMap<>();

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

    public boolean canSync(WorldManager manager, UInteger world1, UInteger world2)
    {
        if (world1.equals(world2) || syncWorlds == null || syncWorlds.isEmpty())
        {
            return true;
        }
        World w1 = manager.getWorld(world1.longValue());
        World w2 = manager.getWorld(world2.longValue());
        for (Entry<String, List<String>> entry : syncWorlds.entrySet())
        {
            List<String> list = new ArrayList<>(entry.getValue());
            list.add(entry.getKey());
            if (list.contains(w1.getName()) && list.contains(w2.getName()))
            {
                return true;
            }
        }
        return false;
    }
}
