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
package de.cubeisland.engine.basics.command.teleport;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.LocationUtil;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.COMPASS;
import static org.bukkit.event.Event.Result.DENY;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN;

public class TeleportListener implements Listener
{
    private final Basics module;

    public TeleportListener(Basics basics)
    {
        this.module = basics;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event)
    {
        User user = module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        switch (event.getCause())
        {
            case COMMAND:
            case PLUGIN:
                user.get(BasicsAttachment.class).setLastLocation(event.getFrom());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        User user = this.module.getCore().getUserManager().getExactUser(event.getEntity().getUniqueId());
        if (module.perms().COMMAND_BACK_ONDEATH.isAuthorized(user))
        {
            user.get(BasicsAttachment.class).setDeathLocation(user.getLocation());
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event)
    {
        if (event.getPlayer().getItemInHand().getType() == COMPASS)
        {
            if (event.useItemInHand().equals(DENY))
            {
                return;
            }
            event.setUseItemInHand(DENY);
            switch (event.getAction())
            {
                case LEFT_CLICK_AIR:
                case LEFT_CLICK_BLOCK:
                    if (module.perms().COMPASS_JUMPTO_LEFT.isAuthorized(event.getPlayer()))
                    {
                        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
                        Location loc;
                        if (event.getClickedBlock() != null && event.getClickedBlock().getType().isSolid())
                        {
                            loc = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);
                        }
                        else
                        {
                            Block block = user.getTargetBlock(this.module.getConfiguration().navigation.jumpToMaxRange);
                            if (block == null || block.getType() == AIR)
                            {
                                return;
                            }
                            loc = block.getLocation().add(0.5, 1, 0.5);
                        }
                        user.safeTeleport(loc, PLUGIN, true);
                        user.sendTranslated(NEUTRAL, "Poof!");
                        event.setCancelled(true);
                    }
                    return;
                case RIGHT_CLICK_AIR:
                case RIGHT_CLICK_BLOCK:
                    if (module.perms().COMPASS_JUMPTO_RIGHT.isAuthorized(event.getPlayer()))
                    {
                        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
                        Location loc = LocationUtil.getBlockBehindWall(user, this.module.getConfiguration().navigation.thru.maxRange,
                                this.module.getConfiguration().navigation.thru.maxWallThickness);
                        if (loc == null)
                        {
                            user.sendTranslated(NEGATIVE, "Nothing to pass through!");
                            return;
                        }
                        loc.setY(loc.getY() + 1);
                        user.safeTeleport(loc, PLUGIN, true);
                        user.sendTranslated(NEUTRAL, "You passed through a wall");
                        event.setCancelled(true);
                    }
            }
        }
    }
}
