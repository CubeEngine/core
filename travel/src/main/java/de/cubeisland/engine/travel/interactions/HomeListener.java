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
package de.cubeisland.engine.travel.interactions;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.Home;
import de.cubeisland.engine.travel.storage.TelePointManager;
import de.cubeisland.engine.travel.storage.TeleportPointModel;

public class HomeListener implements Listener
{
    private final Travel module;
    private final TelePointManager tpManager;

    public HomeListener(Travel module)
    {
        this.module = module;
        this.tpManager = module.getTelepointManager();
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void rightClickBed(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            Material block = event.getClickedBlock().getType();
            if (block == Material.BED_BLOCK || block == Material.BED)
            {
                User user = module.getCore().getUserManager().getUser(event.getPlayer().getName());
                if (user.isSneaking())
                {
                    if (tpManager.hasHome("home", user))
                    {
                        Home home = tpManager.getHome(user, "home");
                        home.setLocation(user.getLocation());
                        home.update();
                        user.sendTranslated(MessageType.POSITIVE, "Your home have been set!");
                    }
                    else
                    {
                        if (this.tpManager.getNumberOfHomes(user) == this.module.getConfig().homes.max)
                        {
                            user.sendTranslated(MessageType.CRITICAL, "You have reached your maximum number of homes!");
                            user.sendTranslated(MessageType.NEGATIVE, "You have to delete a home to make a new one");
                            return;
                        }
                        Home home = tpManager.createHome(user.getLocation(), "home", user, TeleportPointModel.VISIBILITY_PRIVATE);
                        user.sendTranslated(MessageType.POSITIVE, "Your home has been created!");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
