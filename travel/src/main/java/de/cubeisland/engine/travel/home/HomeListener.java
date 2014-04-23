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
package de.cubeisland.engine.travel.home;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static org.bukkit.Material.BED;
import static org.bukkit.Material.BED_BLOCK;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class HomeListener implements Listener
{
    private final Travel module;
    private final HomeManager homeManager;

    public HomeListener(Travel module)
    {
        this.module = module;
        this.homeManager = module.getHomeManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void rightClickBed(PlayerInteractEvent event)
    {
        if (event.getAction() != RIGHT_CLICK_BLOCK)
        {
            return;
        }
        Material block = event.getClickedBlock().getType();
        if (block == BED_BLOCK || block == BED)
        {
            User user = module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
            if (user.isSneaking())
            {
                if (homeManager.has(user, "home"))
                {
                    Home home = homeManager.findOne(user, "home");
                    if (user.getLocation().equals(home.getLocation()))
                    {
                        return;
                    }
                    home.setLocation(user.getLocation());
                    home.update();
                    user.sendTranslated(POSITIVE, "Your home has been set!");
                }
                else
                {
                    if (this.homeManager.getCount(user) == this.module.getConfig().homes.max)
                    {
                        user.sendTranslated(CRITICAL, "You have reached your maximum number of homes!");
                        user.sendTranslated(NEGATIVE, "You have to delete a home to make a new one");
                        return;
                    }
                    homeManager.create(user, "home", user.getLocation(), false);
                    user.sendTranslated(POSITIVE, "Your home has been created!");
                }
                event.setCancelled(true);
            }
        }
    }
}
