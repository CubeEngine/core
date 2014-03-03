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
package de.cubeisland.engine.fly;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.task.Task;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class FlyListener implements Listener
{
    private final UserManager usermanager;
    private final HashMap<Player, Task> tasks = new HashMap<>();
    private final Fly fly;
    private final Location helperLocation = new Location(null, 0, 0, 0);

    private final Permission FLY_FEATHER;

    public FlyListener(Fly fly)
    {
        this.FLY_FEATHER = fly.getBasePermission().child("feather");
        fly.getCore().getPermissionManager().registerPermission(fly,FLY_FEATHER);
        this.fly = fly;
        this.usermanager = fly.getCore().getUserManager();
    }

    @EventHandler
    public void playerInteract(final PlayerInteractEvent event)
    {
        final Player player = event.getPlayer();
        if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
        {
            return;
        }
        if (!player.getItemInHand().getType().equals(Material.FEATHER))
        {
            return;
        }
        User user = usermanager.getExactUser(player.getName());
        if (user == null)//User does not exist
        {
            return;
        }

        if (!FLY_FEATHER.isAuthorized(player))
        {
            user.sendTranslated(MessageType.NEGATIVE, "You dont have permission to use this!");
            player.setAllowFlight(false); //Disable when player is flying
            return;
        }

        FlyStartEvent flyStartEvent = new FlyStartEvent(fly.getCore(), user);
        if (flyStartEvent.isCancelled())
        {
            user.sendTranslated(MessageType.NEGATIVE, "You are not allowed to fly now!");
            player.setAllowFlight(false); //Disable when player is flying
            return;
        }
        //I Believe I Can Fly ...     
        player.setAllowFlight(!player.getAllowFlight());
        if (player.getAllowFlight())
        {
            final ItemStack feather = new ItemStack(Material.FEATHER, 1);
            player.getInventory().removeItem(feather);
            player.setVelocity(player.getVelocity().setY(player.getVelocity().getY() + 1));
            player.teleport(player.getLocation(this.helperLocation).add(new Vector(0, 0.05, 0))); //make sure the player stays flying
            player.setFlying(true);
            user.sendTranslated(MessageType.POSITIVE, "You can now fly!");
            Task flymore = new Task(fly)
            {
                public void run()//2 feather/min
                {
                    if (!player.isFlying())
                    {
                        player.setAllowFlight(false);
                        this.cancelTask();
                        return;
                    }
                    if (player.isFlying())
                    {
                        if (player.getInventory().contains(Material.FEATHER))
                        {
                            player.getInventory().removeItem(feather);
                        }
                        else
                        {
                            player.setAllowFlight(false);
                            this.cancelTask();
                        }
                    }
                }
            };
            flymore.scheduleAsyncRepeatingTask(1000 * 30, 1000 * 30);
            Task oldTask = this.tasks.put(player, flymore);
            if (oldTask != null)
            {
                oldTask.cancelTask();
            }
        }
        else
        {//or not
            player.setFallDistance(0);
            user.sendTranslated(MessageType.NEUTRAL, "You cannot fly anymore!");
        }
    }
}
