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
package de.cubeisland.cubeengine.log.action.logaction.interact;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ITEM;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * splashed potions
 * <p>Events: {@link PotionSplashEvent}</p>
 */
public class PotionSplash extends SimpleLogActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER, ENTITY,ITEM);
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }
    @Override
    public String getName()
    {
        return "potion-splash";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event)
    {
        if (this.isActive(event.getPotion().getWorld()))
        {
            LivingEntity livingEntity = event.getPotion().getShooter();
            String additionalData = this.serializePotionLog(event);
            this.logSimple(livingEntity,additionalData);
        }
    }

    public String serializePotionLog(PotionSplashEvent event)
    {
        ObjectNode json = this.om.createObjectNode();
        ArrayNode effects = json.putArray("effects");
        for (PotionEffect potionEffect : event.getPotion().getEffects())
        {
            ArrayNode effect = effects.addArray();
            effects.add(effect);
            effect.add(potionEffect.getType().getName());
            effect.add(potionEffect.getAmplifier());
            effect.add(potionEffect.getDuration());
        }
        if (!event.getAffectedEntities().isEmpty())
        {
            json.put("amount", event.getAffectedEntities().size());
            ArrayNode affected = json.putArray("affected");
            for (LivingEntity livingEntity : event.getAffectedEntities())
            {
                if (livingEntity instanceof Player)
                {
                    User user = um.getExactUser((Player)livingEntity);
                    affected.add(user.key);
                }
                else
                {
                    affected.add(-livingEntity.getType().getTypeId());
                }
            }
        }
        return json.toString();
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendMessage("Potion stuff happened!");//TODO
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        //TODO
        return false;
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).POTION_SPLASH_enable;
    }
}
