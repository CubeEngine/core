package de.cubeisland.CubeWar;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 *
 * @author Faithcaio
 */
public class CubeWarListener implements Listener
{

    public CubeWarListener() 
    {
    
    }
    
    @EventHandler
    public void death(final EntityDeathEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            if (event.getEntity().getKiller()!=null)
                Heroes.kill(event.getEntity().getKiller(), (Player)event.getEntity());
        }
    }
    
    @EventHandler
    public void damage(final EntityDamageByEntityEvent event)
    {
        Player player = (Player)event.getEntity();
        if (player.isFlying()||player.getAllowFlight())
        {
            boolean fall=false;
            if ((event.getDamager() instanceof Player)) fall = true;
            if ((event.getDamager() instanceof Projectile)) 
                if (((Projectile)event.getDamager()).getShooter() instanceof Player) fall = true;
            if (!fall) return;
            if (event.getEntity() instanceof Player)
            {
                player.chat("&cDamn! &fI got an arrow to the knee!");
                player.setFlying(false);
                player.setAllowFlight(false);
                //TODO prevent player to fly again no event for that :(
            }  
        }

    }
    
}
