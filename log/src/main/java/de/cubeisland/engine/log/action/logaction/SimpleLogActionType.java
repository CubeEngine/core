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
package de.cubeisland.engine.log.action.logaction;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageEvent;

import de.cubeisland.engine.log.action.LogActionType;
import de.cubeisland.engine.log.action.logaction.container.ContainerType;

import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class SimpleLogActionType extends LogActionType
{
    public void logSimple(Entity player, String additional)
    {
        this.queueLog(player.getLocation(),player,null,null,null,null,additional);
    }

    public void logSimple(Location location, Entity causer, Entity data, String additional)
    {
        this.queueLog(location, causer, null, -1L * data.getType().getTypeId(), null, null, additional);
    }

    public String serializeData(EntityDamageEvent.DamageCause cause, Entity entity, DyeColor newColor)
    {
        ObjectNode json = this.om.createObjectNode();
        if (cause != null)
        {
            json.put("dmgC", cause.name());
        }
        if (entity instanceof Player)
        {
            if (cause == null)
            {
                return null;
            }
            return json.toString(); // only save cause
        }
        if (entity instanceof Ageable)
        {
            json.put("isAdult", ((Ageable)entity).isAdult() ? 1 : 0);
        }
        if (entity instanceof Ocelot)
        {
            json.put("isSit", ((Ocelot)entity).isSitting() ? 1 : 0);
        }
        if (entity instanceof Wolf)
        {
            json.put("isSit", ((Wolf)entity).isSitting() ? 1 : 0);
            json.put("color", ((Wolf)entity).getCollarColor().name());
        }
        if (entity instanceof Sheep)
        {
            json.put("color", ((Sheep)entity).getColor().name());
        }
        if (entity instanceof Villager)
        {
            json.put("prof", ((Villager)entity).getProfession().name());
        }
        if (entity instanceof Tameable && ((Tameable) entity).isTamed())
        {
            if (((Tameable)entity).getOwner() != null)
            {
                json.put("owner", ((Tameable)entity).getOwner().getName());
            }
        }
        if (newColor != null)
        {
            json.put("nColor", newColor.name());
        }
        json.put("UUID", entity.getUniqueId().toString()); // TODO this makes rollback for dying etc possible
        return json.toString();
    }

    public void logSimple(Location location, Entity player, ContainerType type, String additional)
    {
        this.queueLog(location,player,type.name,null,null,null,additional);
    }

    public void logSimple(Location location, Entity player, String additional)
    {
        this.queueLog(location,player,null,null,null,null,additional);
    }
}
