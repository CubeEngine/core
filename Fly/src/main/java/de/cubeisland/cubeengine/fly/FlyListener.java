package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.Task;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class FlyListener implements Listener
{
    private UserManager usermanager;
    private HashMap<Player, Task> tasks = new HashMap<Player, Task>();
    private Fly fly;
    private final Location helperLocation = new Location(null, 0, 0, 0);

    public FlyListener(Fly fly)
    {
        this.fly = fly;
        this.usermanager = fly.getUserManager();
    }

    @EventHandler
    public void playerInteract(final PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
        {
            return;
        }
        if (!player.getItemInHand().getType().equals(Material.FEATHER))
        {
            return;
        }
        User user = usermanager.getExactUser(player);
        if (user == null)//User does not exist
        {
            return;
        }

        if (!FlyPerm.FLY_FEATHER.isAuthorized(player))
        {
            user.sendTranslated("You dont have permission to use this!");
            player.setAllowFlight(false); //Disable when player is flying
            return;
        }

        FlyStartEvent flyStartEvent = new FlyStartEvent(CubeEngine.getCore(), user);
        if (flyStartEvent.isCancelled())
        {
            user.sendTranslated("You are not allowed to fly now!");
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
            user.sendTranslated("You can now fly!");
            Task flymore = new Task(fly)
            {
                public void run()//2 feather/min
                {
                    Player player = event.getPlayer();
                    if ((player == null) || (!player.isFlying()))
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
            user.sendTranslated("You cannot fly anymore!");
        }
    }
}
