package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.event.EventListener;
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
 * @author Anselm Brehme
 */
public class FlyListener implements Listener, EventListener
{
    private UserManager usermanager;
    private HashMap<Player, Task> tasks = new HashMap<Player, Task>();
    private CubeFly fly;

    public FlyListener(CubeFly fly)
    {
        this.fly = fly;
        this.usermanager = fly.getUserManager();
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
        User user = usermanager.getUser(player);
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
                //TODO return;
            }
            FlyStartEvent flyStartEvent = new FlyStartEvent(CubeEngine.getCore(), user);
            if (flyStartEvent.isCancelled())
            {
                //TODO user.sendTMessage("&cYou are not allowed to fly now!");
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
            //TODO user.sendTMessage("&6You can now fly!");
            //&6Du kannst jetzt fliegen!
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
            //TODO user.sendTMessage("&6You cannot fly anymore!");
            //&6Du kannst jetzt nicht mehr fliegen!
        }
    }
}
