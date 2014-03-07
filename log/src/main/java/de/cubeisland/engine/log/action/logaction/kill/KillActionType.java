/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.action.logaction.kill;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import com.fasterxml.jackson.databind.JsonNode;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.logaction.ActionTypeContainer;
import de.cubeisland.engine.log.action.logaction.ItemDrop;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.ItemData;
import de.cubeisland.engine.log.storage.LogEntry;

/**
 * Container-ActionType for kills
 * <p>Events: {@link EntityDeathEvent}</p>
 * <p>External Actions:
 * {@link PlayerDeath},
 * {@link BossDeath},
 * {@link PetDeath},
 * {@link AnimalDeath},
 * {@link NpcDeath},
 * {@link MonsterDeath},
 * {@link OtherDeath},
 */
public class KillActionType extends ActionTypeContainer
{
    public KillActionType()
    {
        super("KILL");
    }

    private void logDeathDrops(EntityDeathEvent event)
    {
        if (!event.getDrops().isEmpty()) // TODO log drops later
        {
            ItemDrop itemDrop = this.manager.getActionType(ItemDrop.class);
            if (itemDrop.isActive(event.getEntity().getWorld()))
            {
                for (ItemStack itemStack : event.getDrops())
                {
                    String itemData = new ItemData(itemStack).serialize(this.om);
                    itemDrop.logSimple(event.getEntity(),itemData);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity killed = event.getEntity();
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
            actionType = this.manager.getActionType(NpcDeath.class);
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
            this.logDeathDrops(event);
            return; // should not happen anymore (but i'll leave it in to prevent NPE)
        }
        String additionalData = actionType.serializeData(dmgEvent.getCause(), killed,null);
        if (dmgEvent instanceof EntityDamageByEntityEvent)
        {
            Entity damager = ((EntityDamageByEntityEvent)dmgEvent).getDamager();
            if (damager instanceof Projectile)
            {
                ProjectileSource causer = ((Projectile) damager).getShooter();
                if (causer instanceof Player)
                {
                    if (false) //TODO player is Killer
                    {
                        this.logDeathDrops(event);
                        return;
                    }
                }
                else if (causer instanceof Skeleton || causer instanceof Ghast)
                {
                    if (false) //TODO monster is Killer
                    {
                        this.logDeathDrops(event);
                        return;
                    }
                }
                else if (causer instanceof Wither)
                {
                    if (false) //TODO boss is Killer
                    {
                        this.logDeathDrops(event);
                        return;
                    }
                }
                else // Projectile shot by Dispenser // TODO better
                {
                    if (false)
                    {
                        this.module.getLog().debug("Unknown shooter: {}", ((Projectile) damager).getShooter());
                        this.logDeathDrops(event);
                        return;
                    }
                }
            }
            else
            {
                if (damager instanceof Player)
                {
                    if (false) //TODO player is Killer
                    {
                        this.logDeathDrops(event);
                        return;
                    }
                }
                else if (damager instanceof Wither || damager instanceof EnderDragon)
                {
                    if (false) //TODO boss is Killer
                    {
                        this.logDeathDrops(event);
                        return;
                    }
                }
                else
                {
                    if (false) //TODO monster is Killer
                    {
                        this.logDeathDrops(event);
                        return;
                    }
                }
                actionType.logSimple(location,damager,killed,additionalData);
                this.logDeathDrops(event);
            }
        }
        else
        {
            if (false) //TODO environement is Killer
            {
                this.logDeathDrops(event);
                return;
            }
            actionType.logSimple(location,null,killed,additionalData);
            this.logDeathDrops(event);
        }
    }

    static void showSubActionLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        int amount = 1;
        if (logEntry.hasAttached())
        {
            amount += logEntry.getAttached().size();
        }
        if (logEntry.hasCauserUser())
        {
            if (amount == 1)
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{name#entity} got slaughtered by {user}{}", time, logEntry.getEntityFromData(), logEntry.getCauserUser().getDisplayName(), loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{amount}x {name#entity} got slaughtered by {user}{}", time, amount, logEntry.getEntityFromData(), logEntry.getCauserUser().getDisplayName(), loc);
            }
        }
        else if (logEntry.hasCauserEntity())
        {
            if (amount == 1)
            {
                user.sendTranslated(MessageType.POSITIVE,  "{}{name#entity} could not escape {name#entity}{}", time, logEntry.getEntityFromData(), logEntry.getCauserEntity(), loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{amount}x {name#entity} could not escape {name#entity}{}", time, amount, logEntry.getEntityFromData(), logEntry.getCauserEntity(), loc);
            }

        }
        else // something else
        {
            if (amount == 1)
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{name#entity} died ({input#cause}){}", time, logEntry.getEntityFromData(), logEntry.getAdditional().get("dmgC").toString(), loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{amount}x {name#entity} died ({input#cause}){}", time, amount, logEntry.getEntityFromData(), logEntry.getAdditional().get("dmgC").toString(), loc);
            }
        }
    }

    public static boolean isSimilarSubAction(LogEntry logEntry, LogEntry other)
    {
        if (logEntry.getActionType() != other.getActionType()) return false;
        return logEntry.getCauser().equals(other.getCauser())
            && logEntry.getData() == other.getData()
            && logEntry.getWorld() == other.getWorld();
    }

    public static boolean rollbackDeath(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        if (!preview)
        {
            Location location = logEntry.getLocation();
            int id = (int)-logEntry.getData();
            EntityType entityType = EntityType.fromId(id);
            Entity entity = location.getWorld().spawnEntity(location, entityType);
            applySerializedData(entity, logEntry.getAdditional());
        }
        return true;
    }

    private static void applySerializedData(Entity entity, JsonNode json)
    {
        if (entity instanceof Ageable)
        {
            if (json.get("isAdult").asBoolean())
            {
                ((Ageable)entity).setAdult();
            }
            else
            {
                ((Ageable)entity).setBaby();
            }
        }
        if (entity instanceof Ocelot)
        {
            ((Ocelot)entity).setSitting(json.get("isSit").asBoolean());
        }
        if (entity instanceof Wolf)
        {
            ((Wolf)entity).setSitting(json.get("isSit").asBoolean());
            DyeColor color = DyeColor.valueOf(json.get("color").asText());
            ((Wolf)entity).setCollarColor(color);
        }
        if (entity instanceof Sheep)
        {
            DyeColor color = DyeColor.valueOf(json.get("color").asText());
            ((Sheep)entity).setColor(color);
        }
        if (entity instanceof Villager)
        {
            Profession profession = Profession.valueOf(json.get("prof").asText());
            ((Villager)entity).setProfession(profession);
        }
        if (entity instanceof Tameable && ((Tameable) entity).isTamed())
        {
            JsonNode owner = json.get("owner");
            if (owner != null)
            {
                User user = CubeEngine.getUserManager().getUser(owner.asText(), false);
                if (user != null)
                {
                    ((Tameable)entity).setOwner(user);
                }
            }
        }
    }
}
