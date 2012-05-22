package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.MyTask;
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
    HashMap<Player,MyTask> tasks = new HashMap<Player,MyTask>();

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
            ||event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;
        if (!player.getItemInHand().getType().equals(Material.FEATHER)) return;
        if (Perm.FLY_BYPASS.isAuthorized(player));
        {
            if (!Perm.FLY_FEAHTER.isAuthorized(player))
            {
                player.sendMessage("Permission fehlt");
                //TODO Translation: You dont have permission to use this!
                //Du bist nicht berechtigt dies zu nutzen!
                player.setAllowFlight(false); //Disable when player is flying
                return;
            }
            User user = cuManager.getUser(player);
            if (user == null)
            {
                //User does not exist -> No Permissions for using any CubeModule
                return;
            }
            if (user.hasFlag(User.BLOCK_FLY))
            {
                player.sendMessage("fly_block");
                //You are not allowed to fly now!
                //Du darfst jetzt nicht fliegen!
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
            player.sendMessage("fly_on");
            //You can now fly!
            //Du kannst jetzt fliegen!
            MyTask flymore = new MyTask(plugin)
            {
                public void run()//2 feather/min
                {
                    Player player = event.getPlayer();
                    if ((player == null)||(!player.isFlying()))
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
            final int taskId = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, flymore , 1000*30, 1000*30);
            flymore.setTaskId(taskId);
            MyTask oldTask = this.tasks.put(player, flymore);
            if (oldTask != null)
                oldTask.cancelTask();
        }
        else
        {//or not
            player.sendMessage("fly_off");
            //You cannot fly anymore!
            //Du kannst jetzt nicht mehr fliegen!
        }
    }
}
