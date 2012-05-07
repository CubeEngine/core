package de.cubeisland.CubeWar;

import de.cubeisland.CubeWar.Groups.GroupControl;
import de.cubeisland.CubeWar.User.PvP;
import de.cubeisland.CubeWar.User.Users;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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
    public void respawn(final PlayerRespawnEvent event)
    {
        CubeWar plugin = CubeWar.getInstance();
        int respawntime = GroupControl.getArea(event.getRespawnLocation()).getPvp_spawnprotect()*20;
        if (respawntime > 0)
        {
            Users.getUser(event.getPlayer()).setRespawning(true);
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
                new Runnable() {
                    public void run()
                    {
                        Users.getUser(event.getPlayer()).setRespawning(false);
                    }} , respawntime);
        }
    }
    
    
    @EventHandler
    public void death(final EntityDeathEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            if (event.getEntity().getKiller()!=null)
            {
                Users.kill(event.getEntity().getKiller(), (Player)event.getEntity());
                PvP.loot(event.getEntity().getKiller().getPlayer(), (Player)event.getEntity(), event.getDrops(), event.getEntity().getLocation());
                event.getDrops().clear();
            }
                
        }
        if (event.getEntity() instanceof Monster)
        {
            if (event.getEntity().getKiller()!=null)
                Users.kill(event.getEntity().getKiller(), (Monster)event.getEntity());
        }
    }
    
    @EventHandler
    public void damage(final EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player damagee = (Player)event.getEntity();
        Entity damagerEntity = event.getDamager();
        Player damager;
        Boolean ranged = false;
        if ((damagerEntity instanceof Player))
        {
            damager = (Player)damagerEntity; //MELEE
        }
        else
        {
            if ((damagerEntity instanceof Projectile)){
                if (((Projectile)damagerEntity).getShooter() instanceof Player)
                {
                    ranged = true; //RANGED
                    damager = (Player)((Projectile)damagerEntity).getShooter();
                }
                else return;
            }
            else return;
        }
        if (PvP.isPvPallowed(damager, damagee))
        {//PVP is ON Kill em all!
            //FLYMODE Disabler
            if (damagee.isFlying()||damagee.getAllowFlight())
            {
                if (ranged)
                    PvP.stopFlyArrow(damagee);//Arrow to the knee :)
                else
                    PvP.stopFlyAndFall(damagee);//Falling flies
            } 
            if (damager.isFlying())
                PvP.stopFly(damager); //Falling attacker
            //DAMAGE Disabler
            if (PvP.isDamageOn(damager, damagee))
            {
                //FRIENDLY Beschuss Disabler (Denglisch FTGewinn)
                if (!PvP.isFriendlyFireOn(damager, damagee))
                {
                    //TODO einstellung set dmg = 0 oder canceln
                    event.setCancelled(true);
                }
                
                //DAMAGE Modifier
                int modDamage = PvP.modifyDamage(damager, damagee, event.getDamage());
                event.setDamage(modDamage);
            }
            else
            {
                event.setCancelled(true);
            }
        }
    }    
}
