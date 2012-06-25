package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.Task;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class FlyListener implements Listener
{
    UserManager cuManager;
    CubeFly plugin;
    HashMap<Player, Task> tasks = new HashMap<Player, Task>();

    public FlyListener(UserManager cuManager, CubeFly plugin)
    {
        this.cuManager = cuManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void playerInteract(final PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR)
                || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
        {
            return;
        }
        if (!player.getItemInHand().getType().equals(Material.FEATHER))
        {
            return;
        }
        User user = cuManager.getUser(player);
        if (Perm.FLY_BYPASS.isAuthorized(player));
        {
            if (!Perm.FLY_FEAHTER.isAuthorized(player))
            {
                user.sendTMessage("&cYou dont have permission to use this!");
                //TODO Translation: 
                //&cDu bist nicht berechtigt dies zu nutzen!
                player.setAllowFlight(false); //Disable when player is flying
                return;
            }
            if (user == null)
            {
                //User does not exist -> No Permissions for using any CubeModule
                return;
            }
            FlyStartEvent flyStartEvent = new FlyStartEvent(CubeCore.getInstance(), user);
            if (flyStartEvent.isCancelled())
            {
                user.sendTMessage("&cYou are not allowed to fly now!");
                //&cDu darfst jetzt nicht fliegen!
                player.setAllowFlight(false); //Disable when player is flying
                return;
            }
        }
        //I Believe I Can Fly ...     
        player.setAllowFlight(!player.getAllowFlight());
        if (player.getAllowFlight())
        {
            final ItemStack feather = new ItemStack(Material.FEATHER, 1);
            player.getInventory().removeItem(feather);
            user.sendTMessage("&6You can now fly!");
            //&6Du kannst jetzt fliegen!
            Task flymore = new Task(plugin)
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
                            CubeFly.debug("FlyTime extended!");
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
            user.sendTMessage("&6You cannot fly anymore!");
            //&6Du kannst jetzt nicht mehr fliegen!
        }
    }
}
