package de.cubeisland.cubeengine.war;

import static de.cubeisland.cubeengine.war.CubeWar.t;
import de.cubeisland.cubeengine.war.area.AreaControl;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import de.cubeisland.cubeengine.war.user.PvP;
import de.cubeisland.cubeengine.war.user.UserControl;
import org.bukkit.Chunk;
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

    private AreaControl areas = AreaControl.get();
    private GroupControl groups = GroupControl.get();
    private PvP pvp = CubeWar.getInstance().getPvp();
    private UserControl users = UserControl.get();

    public CubeWarListener()
    {
    }

    @EventHandler
    public void respawn(final PlayerRespawnEvent event)
    {
        CubeWar plugin = CubeWar.getInstance();
        int respawntime = groups.getGroupAtLocation(event.getRespawnLocation()).getRespawnProtect() * 20;
        if (respawntime > 0)
        {
            users.getUser(event.getPlayer()).setRespawning(true);
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
                    new Runnable()
                    {

                        public void run()
                        {
                            users.getUser(event.getPlayer()).setRespawning(false);
                        }
                    }, respawntime);
        }
    }

    @EventHandler
    public void move(final PlayerMoveEvent event)
    {
        Chunk chunkFrom = event.getFrom().getChunk();
        Chunk chunkTo = event.getTo().getChunk();
        if (!chunkFrom.equals(chunkTo))
        {
            Group groupFrom = areas.getGroup(chunkFrom);
            Group groupTo = areas.getGroup(chunkTo);
            if (!groupFrom.equals(groupTo))
            {
                event.getPlayer().sendMessage("X: " + event.getTo().getChunk().getX() + " Z: " + event.getTo().getChunk().getZ()
                        + " " + groupTo.getTag());
            }
        }
    }

    @EventHandler
    public void death(final EntityDeathEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            if (event.getEntity().getKiller() != null)
            {
                users.kill(event.getEntity().getKiller(), (Player) event.getEntity());
                pvp.loot(event.getEntity().getKiller().getPlayer(), (Player) event.getEntity(), event.getDrops(), event.getEntity().getLocation());
                event.getDrops().clear();
            }

        }
        if (event.getEntity() instanceof Monster)
        {
            if (event.getEntity().getKiller() != null)
            {
                users.kill(event.getEntity().getKiller(), (Monster) event.getEntity());
            }
        }
    }

    @EventHandler
    public void damage(final EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof Player))
        {
            return;
        }
        Player damagee = (Player) event.getEntity();
        Entity damagerEntity = event.getDamager();
        Player damager;
        Boolean ranged = false;
        if ((damagerEntity instanceof Player))
        {
            damager = (Player) damagerEntity; //MELEE
        }
        else
        {
            if ((damagerEntity instanceof Projectile))
            {
                if (((Projectile) damagerEntity).getShooter() instanceof Player)
                {
                    ranged = true; //RANGED
                    damager = (Player) ((Projectile) damagerEntity).getShooter();
                }
                else
                {
                    return;
                }
            }
            else
            {
                return;
            }
        }
        if (!ranged)
        {
            ItemStack potionItem = damager.getItemInHand().clone();
            potionItem.setAmount(1);
            if (potionItem.getType().equals(Material.POTION))
            {
                if (users.isAllied(damager, damagee))
                {
                    int h;
                    switch (potionItem.getDurability())
                    {
                        case 8193:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 15 * 20, 1));
                            break;//0:45 46
                        case 8257:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30 * 20, 1));
                            break;//2:00 108
                        case 8225:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 7 * 20, 2));
                            break;//0:22 48
                        case 8194:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1));
                            break;//3:00
                        case 8258:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 1));
                            break;//8:00
                        case 8226:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20, 5));
                            break;//1:30
                        case 8195:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30 * 20, 0));
                            break;//3:00
                        case 8259:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60 * 20, 0));
                            break;//8:00
                        case 8227:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 30 * 20, 0));
                            break;//3:00
                        case 8197:
                            h = damagee.getHealth() + 3;
                            if (h > 20)
                            {
                                h = 20;
                            }
                            damagee.setHealth(h);
                            break;//6
                        case 8261:
                            h = damagee.getHealth() + 3;
                            if (h > 20)
                            {
                                h = 20;
                            }
                            damagee.setHealth(h);
                            break;//6
                        case 8229:
                            h = damagee.getHealth() + 6;
                            if (h > 20)
                            {
                                h = 20;
                            }
                            damagee.setHealth(h);
                            break;//12
                        case 8201:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 10 * 20, 5));
                            break;//3:00 3
                        case 8265:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 20, 5));
                            break;//8:00 3       
                        case 8233:
                            damagee.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 20, 10));
                            break;//1:30 6
                        default:
                            damager.sendMessage(t("potion_not"));
                            return;
                    }
                    damagee.getInventory().remove(potionItem);
                    damager.sendMessage(t("potion_use", damagee.getName()));
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (pvp.isPvPallowed(damager, damagee))
        {//PVP is ON Kill em all!
            //FLYMODE Disabler
            if (damagee.isFlying() || damagee.getAllowFlight())
            {
                if (ranged)
                {
                    pvp.stopFlyArrow(damagee);//Arrow to the knee :)
                }
                else
                {
                    pvp.stopFlyAndFall(damagee);//Falling flies
                }
            }
            if (damager.isFlying())
            {
                pvp.stopFly(damager); //Falling attacker
            }            //DAMAGE Disabler
            if (pvp.isDamageOn(damager, damagee))
            {
                //FRIENDLY Beschuss Disabler (Denglisch FTGewinn)
                if (!pvp.isFriendlyFireOn(damager, damagee))
                {
                    event.setCancelled(true);
                }
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
