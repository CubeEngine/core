package de.cubeisland.CubeWar;

import static de.cubeisland.CubeWar.CubeWar.t;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
        if (event.getEntity() instanceof Monster)
        {
            if (event.getEntity().getKiller()!=null)
                Heroes.kill(event.getEntity().getKiller(), (Monster)event.getEntity());
        }
    }
    
    @EventHandler
    public void damage(final EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player)event.getEntity();
        //player.chat(Heroes.getHeroKD(Heroes.getHero(player), null, 0));//TESTCHAT
        if (player.isFlying()||player.getAllowFlight())
        {
            boolean fall=false;
            if ((event.getDamager() instanceof Player))//event.getDamager kommt oft vor...
            {
                fall = true;
                ((Player)event.getDamager()).setFlying(false);
            }
            if ((event.getDamager() instanceof Projectile)) 
                if (((Projectile)event.getDamager()).getShooter() instanceof Player)
                {
                    fall = true;
                    ((Player)((Projectile)event.getDamager()).getShooter()).setFlying(false);
                    player.sendMessage(t("event_arrow"));
                }
            if (!fall) return;
            player.setFlying(false);
            player.setAllowFlight(false);
            //TODO prevent player to fly again no event for that :(
        }

    }
    
}
