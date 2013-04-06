package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryType;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;

import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class SimpleLogActionType extends LogActionType
{
    public SimpleLogActionType(Log module, String name, Type... types)
    {
        super(module, name, types);
    }

    public void logSimple(Entity player, String additional)
    {
        this.queueLog(player.getLocation(),player,null,null,null,null,additional);
    }

    public void logSimple(Location location, Entity causer, Entity data, String additional)
    {
        this.queueLog(location,causer,null,-1L * data.getType().getTypeId(), null,null,additional);
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
            json.put("owner", ((Tameable)entity).getOwner().getName());
        }
        if (newColor != null)
        {
            json.put("nColor", newColor.name());
        }
        return json.toString();
    }

    public void logSimple(Location location, Entity player, InventoryType type, String additional)
    {
        this.queueLog(location,player,type.name(),null,null,null,additional);
    }

    public void logSimple(Location location, Player player, String additional)
    {
        this.queueLog(location,player,null,null,null,null,additional);
    }
}
