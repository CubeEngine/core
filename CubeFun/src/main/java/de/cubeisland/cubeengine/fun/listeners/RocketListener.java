package de.cubeisland.cubeengine.fun.listeners;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.fun.Fun;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author Wolfi
 */
public class RocketListener implements Listener
{
    public static Set<String> rocketPlayers = new HashSet<String>();
    
    public UserManager userManager;
    
    public static void addPlayer(User user)
    {
        rocketPlayers.add(user.getName());
    }
    
    public static void removePlayer(String name)
    {
        rocketPlayers.remove(name);
    }

    public RocketListener(Fun module) 
    {
        this.userManager = module.getUserManager();
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        if(event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL)
        {
            User user = userManager.getUser((Player)event.getEntity());
            if(user == null)
            {
                return;
            }
            if(rocketPlayers.contains(user.getName()))
            {
                event.setCancelled(true);
                removePlayer(user.getName());
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event)
    {
        /*
         * DOES NOT WORK!!
        if(rocketPlayers.contains(event.getPlayer().getName()))
        {
            Block fromBlock = event.getFrom().subtract(0, 1, 0).getBlock();
            Block toBlock = event.getTo().subtract(0, 1, 0).getBlock();
            
            if(fromBlock.getType() != Material.AIR && toBlock.getType() != Material.AIR)
            {
                String name = event.getPlayer().getName();
                removePlayer(name);
                System.out.println(name + " removed");
            }
        }
        */
    }
}
