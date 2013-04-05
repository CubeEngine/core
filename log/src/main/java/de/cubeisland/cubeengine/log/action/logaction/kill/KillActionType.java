package de.cubeisland.cubeengine.log.action.logaction.kill;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.ItemDrop;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.ActionType;
import de.cubeisland.cubeengine.log.storage.ItemData;

import static de.cubeisland.cubeengine.log.storage.ActionType.*;
import static de.cubeisland.cubeengine.log.storage.ActionType.ENVIRONMENT_KILL;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.PROJECTILE;

public class KillActionType extends LogActionType
{
    public KillActionType(Log module, int id, String name)
    {
        super(module, -1 ,"KILL");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity killed = event.getEntity();
        if (!event.getDrops().isEmpty())
        {
            ItemDrop itemDrop = this.manager.getActionType(ItemDrop.class);
            if (itemDrop.isActive(event.getEntity().getWorld()))
            {
                for (ItemStack itemStack : event.getDrops())
                {
                    String itemData = new ItemData(itemStack).serialize(this.om);
                    itemDrop.logSimple(killed,itemData);
                }
            }
        }
        Location location = event.getEntity().getLocation();

        SimpleLogActionType actionType;
        if (killed instanceof Player)
        {
            actionType = this.manager.getActionType(PlayerDeath.class);
        }
        else if (killed instanceof Wither || killed instanceof EnderDragon)
        {
            actionType = this.manager.getActionType(BossDeath.class);
        }
        else if (killed instanceof Animals)
        {
            if (killed instanceof Tameable && ((Tameable) killed).isTamed())
            {
                actionType = this.manager.getActionType(PetDeath.class);
            }
            else
            {
                actionType = this.manager.getActionType(AnimalDeath.class);
            }
        }
        else if (killed instanceof Villager)
        {
            actionType = this.manager.getActionType(AnimalDeath.class);
        }
        else if (killed instanceof Monster)
        {
            actionType = this.manager.getActionType(MonsterDeath.class);
        }
        else
        {
            actionType = this.manager.getActionType(OtherDeath.class);
        }
        EntityDamageEvent dmgEvent = killed.getLastDamageCause();
        if (dmgEvent == null)
        {
            return; // squids dying in air, lazy bukkit :S -> https://bukkit.atlassian.net/browse/BUKKIT-3684
        }
        String additionalData = actionType.serializeData(dmgEvent.getCause(), killed,null);
        Entity causer;
        if (dmgEvent instanceof EntityDamageByEntityEvent)
        {
            Entity damager = ((EntityDamageByEntityEvent)dmgEvent).getDamager();
            if (dmgEvent.getCause().equals(PROJECTILE) && damager instanceof Projectile)
            {
                causer = ((Projectile) damager).getShooter();
                if (causer instanceof Player)
                {
                    if (false) //TODO player is Killer
                    {
                        return;
                    }
                }
                else if (causer instanceof Skeleton || causer instanceof Ghast)
                {
                    if (false) //TODO monster is Killer
                    {
                        return;
                    }
                }
                else if (causer instanceof Wither)
                {
                    if (false) //TODO boss is Killer
                    {
                        return;
                    }
                }
                else // Projectile shot by Dispenser
                {
                    System.out.print("Unknown Shooter: "+ ((Projectile) damager).getShooter());
                    return;
                }
            }
            else
            {
                if (damager instanceof Player)
                {
                    if (false) //TODO player is Killer
                    {
                        return;
                    }
                }
                else if (damager instanceof Wither || damager instanceof EnderDragon)
                {
                    if (false) //TODO boss is Killer
                    {
                        return;
                    }
                }
                else
                {
                    if (false) //TODO monster is Killer
                    {
                        return;
                    }
                }
                causer = damager;
            }
        }
        else
        {
            if (false) //TODO environement is Killer
            {
                return;
            }
            causer = null;
        }
        actionType.logSimple(location,causer,killed,additionalData);
    }
}
