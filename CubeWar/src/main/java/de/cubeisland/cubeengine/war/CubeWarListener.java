package de.cubeisland.cubeengine.war;

import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.area.AreaControl;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import de.cubeisland.cubeengine.war.user.PvP;
import de.cubeisland.cubeengine.war.user.Users;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Faithcaio
 */
public class CubeWarListener implements Listener
{
    private AreaControl areas = CubeWar.getInstance().getAreas();
    private GroupControl groups = GroupControl.get();
    private PvP pvp = CubeWar.getInstance().getPvp();
    
    public CubeWarListener() 
    {
        
    }
    
    @EventHandler
    public void respawn(final PlayerRespawnEvent event)
    {
        CubeWar plugin = CubeWar.getInstance();
        int respawntime = groups.getGroup(event.getRespawnLocation()).getPvp_respawnprotect()*20;
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
    public void move(final PlayerMoveEvent event)
    {
        if (!event.getFrom().getChunk().equals(event.getTo().getChunk()))
        {
            if (!areas.getGroup(event.getFrom().getChunk()).equals(areas.getGroup(event.getTo().getChunk())))
                event.getPlayer().sendMessage("X: "+event.getTo().getChunk().getX()+" Z: "+event.getTo().getChunk().getZ()+
                    " "+groups.getGroup(event.getPlayer()).getTag());
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
                pvp.loot(event.getEntity().getKiller().getPlayer(), (Player)event.getEntity(), event.getDrops(), event.getEntity().getLocation());
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
        if (!ranged)
        {
            ItemStack potionItem = damager.getItemInHand().clone();
            potionItem.setAmount(1);
            if (potionItem.getType().equals(Material.POTION))
            if (Users.isAllied(damager, damagee))
            {int h;
                switch (potionItem.getDurability())
                {
                    case 8193: damagee.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,15*20,1)); break;//0:45 46
                    case 8257: damagee.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,30*20,1)); break;//2:00 108
                    case 8225: damagee.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,7*20,2)); break;//0:22 48
                    case 8194: damagee.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,10*20,1)); break;//3:00
                    case 8258: damagee.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,30*20,1)); break;//8:00
                    case 8226: damagee.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,5*20,5)); break;//1:30
                    case 8195: damagee.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,30*20,0)); break;//3:00
                    case 8259: damagee.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,60*20,0)); break;//8:00
                    case 8227: damagee.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,30*20,0)); break;//3:00
                    case 8197: h = damagee.getHealth()+3; if(h>20) h=20; damagee.setHealth(h); break;//6
                    case 8261: h = damagee.getHealth()+3; if(h>20) h=20; damagee.setHealth(h); break;//6
                    case 8229: h = damagee.getHealth()+6; if(h>20) h=20; damagee.setHealth(h); break;//12
                    case 8201: damagee.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,10*20,5)); break;//3:00 3
                    case 8265: damagee.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,20*20,5)); break;//8:00 3       
                    case 8233: damagee.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,5*20,10)); break;//1:30 6
                    default: damager.sendMessage(t("potion_not")); return;
                }
                damagee.getInventory().remove(potionItem);
                damager.sendMessage(t("potion_use",damagee.getName()));
                event.setCancelled(true);
                return;
            }
        }
        
        if (pvp.isPvPallowed(damager, damagee))
        {//PVP is ON Kill em all!
            //FLYMODE Disabler
            if (damagee.isFlying()||damagee.getAllowFlight())
            {
                if (ranged)
                    pvp.stopFlyArrow(damagee);//Arrow to the knee :)
                else
                    pvp.stopFlyAndFall(damagee);//Falling flies
            } 
            if (damager.isFlying())
                pvp.stopFly(damager); //Falling attacker
            //DAMAGE Disabler
            if (pvp.isDamageOn(damager, damagee))
            {
                //FRIENDLY Beschuss Disabler (Denglisch FTGewinn)
                if (!pvp.isFriendlyFireOn(damager, damagee))
                    event.setCancelled(true);
                //DAMAGE Modifier
                int modDamage = pvp.modifyDamage(damager, damagee, event.getDamage());
                event.setDamage(modDamage);
            }
            else
            {
                event.setCancelled(true);
            }
        }
    }    
}
