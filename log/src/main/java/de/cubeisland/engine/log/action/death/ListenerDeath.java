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
package de.cubeisland.engine.log.action.death;

import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.bigdata.Reference;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.LogListener;
import de.cubeisland.engine.log.action.block.entity.ActionEntityBlock.EntitySection;
import de.cubeisland.engine.log.action.block.player.ActionPlayerBlock.PlayerSection;

/**
 * A Listener for Death related Actions
 * <p>Events:
 * {@link EntityDeathEvent}
 * <p>Actions:
 * {@link DeathKill}
 * {@link DeathPlayer}
 * {@link PlayerDeathDrop}
 * {@link EntityDeathAction}
 * {@link DeathBoss}
 * {@link DeathPet}
 * {@link DeathAnimal}
 * {@link DeathNpc}
 * {@link DeathMonster}
 * {@link DeathOther}
 * {@link DeathDrop}
 */
public class ListenerDeath extends LogListener
{
    public ListenerDeath(Log module)
    {
        super(module, DeathKill.class, DeathPlayer.class, PlayerDeathDrop.class,
              DeathBoss.class, DeathPlayer.class, DeathAnimal.class, DeathNpc.class, DeathMonster.class,
              DeathOther.class, DeathDrop.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event)
    {
        DeathKill killAction = this.newAction(DeathKill.class, event.getEntity().getWorld());
        if (killAction != null)
        {
            // TODO check config for killer type
            killAction.setLocation(event.getEntity().getLocation());
            EntityDamageEvent cause = event.getEntity().getLastDamageCause();
            if (cause == null)
            {
                killAction.otherKiller = DamageCause.CUSTOM;
            }
            else if (cause instanceof EntityDamageByEntityEvent)
            {
                Entity damager = ((EntityDamageByEntityEvent)cause).getDamager();
                if (damager instanceof Player)
                {
                    killAction.playerKiller = new PlayerSection((Player)damager);
                }
                else if (damager instanceof Projectile)
                {
                    killAction.projectile = true;
                    if (((Projectile)damager).getShooter() instanceof Entity)
                    {
                        if (((Projectile)damager).getShooter() instanceof Player)
                        {
                            killAction.playerKiller = new PlayerSection((Player)((Projectile)damager).getShooter());
                        }
                        else
                        {
                            killAction.entityKiller = new EntitySection((Entity)((Projectile)damager).getShooter());
                        }
                    }
                    else
                    {
                        killAction.entityKiller = new EntitySection(damager);
                    }
                }
                else
                {
                    killAction.entityKiller = new EntitySection(damager);
                }
            }
            else
            {
                killAction.otherKiller = cause.getCause();
            }
            this.logAction(killAction);
        }

        // TODO do not forget ref to killAction
        Reference<DeathKill> reference = this.reference(killAction);
        LivingEntity killed = event.getEntity();
        if (killed instanceof Player)
        {
            DeathPlayer action = this.newAction(DeathPlayer.class, killed.getWorld());
            if (action != null)
            {
                action.killer = reference;
                action.setPlayer((Player)killed);
                this.logAction(action);
            }
            if (this.isActive(DeathDrop.class, event.getEntity().getWorld()))
            {
                Reference<DeathPlayer> deathRef = this.reference(action);
                for (ItemStack itemStack : event.getDrops())
                {
                    PlayerDeathDrop dropAction = newAction(PlayerDeathDrop.class);
                    dropAction.item = itemStack;
                    dropAction.death = deathRef;
                    this.logAction(dropAction);
                }
            }
            return;
        }

        Class<? extends EntityDeathAction> actionType;
        if (killed instanceof Wither || killed instanceof EnderDragon)
        {
            actionType = DeathBoss.class;
        }
        else if (killed instanceof Animals)
        {
            if (killed instanceof Tameable && ((Tameable)killed).isTamed())
            {
                actionType = DeathPet.class;
            }
            else
            {
                actionType = DeathAnimal.class;
            }
        }
        else if (killed instanceof Villager)
        {
            actionType = DeathNpc.class;
        }
        else if (killed instanceof Monster)
        {
            actionType = DeathMonster.class;
        }
        else
        {
            actionType = DeathOther.class;
        }
        EntityDeathAction action = this.newAction(actionType, killed.getWorld());
        if (action != null)
        {
            action.setKilled(killed);
            action.setLocation(killed.getLocation());
            action.killer = reference;
            this.logAction(action);
        }
        Reference<EntityDeathAction> deathRef = this.reference(action);
        if (this.isActive(DeathDrop.class, event.getEntity().getWorld()))
        {
            for (ItemStack itemStack : event.getDrops())
            {
                DeathDrop dropAction = newAction(DeathDrop.class);
                dropAction.item = itemStack;
                dropAction.death = deathRef;
                this.logAction(dropAction);
            }
        }
    }

    /*

    public static boolean rollbackDeath(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        // TODO warning if rollback multiple times
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
    */
}
