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
package de.cubeisland.engine.basics.command.moderation.spawnmob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.matcher.Match;

public class SpawnMob
{
    static Entity[] spawnMobs(CommandContext context, String mobString, Location loc, int amount)
    {
        String[] mobStrings = StringUtils.explode(",", mobString);
        Entity[] mobs = spawnMob(context, mobStrings[0], loc, amount, null); // base mobs
        Entity[] ridingMobs = mobs;
        try
        {
            for (int i = 1; i < mobStrings.length; ++i)
            {
                ridingMobs = spawnMob(context, mobStrings[i], loc, amount, ridingMobs);
            }
            return mobs;
        }
        catch (IncorrectUsageException e)
        {
            context.sendMessage(e.getMessage());
            return mobs;
        }
    }

    static Entity[] spawnMob(CommandContext context, String mobString, Location loc, int amount, Entity[] ridingOn)
    {
        String entityName = mobString;
        EntityType entityType;
        List<String> entityData = new ArrayList<>();
        if (entityName.isEmpty())
        {
            return null;
        }
        if (entityName.contains(":"))
        {
            entityData = Arrays.asList(StringUtils.explode(":", entityName.substring(entityName
                                                                                         .indexOf(":") + 1, entityName
                                                                                         .length())));
            entityName = entityName.substring(0, entityName.indexOf(":"));
            entityType = Match.entity().mob(entityName);
        }
        else
        {
            entityType = Match.entity().mob(entityName);
        }
        if (entityType == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Unknown mob-type: {input#entityname} not found!", entityName);
            return null;
        }
        Entity[] spawnedMobs = new Entity[amount];
        for (int i = 0; i < amount; ++i)
        {
            //CreatureSpawnEvent
            spawnedMobs[i] = loc.getWorld().spawnEntity(loc, entityType);
            if (ridingOn != null)
            {
                ridingOn[i].setPassenger(spawnedMobs[i]);
            }
            if (spawnedMobs[i] instanceof Skeleton)
            {
                ((Skeleton)spawnedMobs[i]).getEquipment().setItemInHand(new ItemStack(Material.BOW));
            }
        }
        applyDataToMob(entityData, spawnedMobs);
        return spawnedMobs;
    }

    /**
     * Applies a list of data in Strings onto given entities
     *
     * @param datas the data to apply
     * @param entities one or multiple entities of the same type
     */
    @SuppressWarnings("unchecked")
    static void applyDataToMob(List<String> datas, Entity... entities)
    {
        if (entities.length == 0) throw new IllegalArgumentException("You need to provide at least one entity to apply the data to!");
        for (Entity entity : entities)
        {
            if (!entities[0].getType().equals(entity.getType())) throw new IllegalArgumentException("All the entities need to be of the same type");
        }
        Map<EntityDataChanger, Object> changers = new HashMap<>();
        for (String data : datas)
        {
            for (EntityDataChanger entityDataChanger : EntityDataChanger.entityDataChangers)
            {
                if (entityDataChanger.canApply(entities[0]))
                {
                    Object typeValue = entityDataChanger.changer.getTypeValue(data);
                    if (typeValue != null) // valid typeValue for given data?
                    {
                        changers.put(entityDataChanger, typeValue); // save to apply later to all entities
                        break;
                    }
                }
            }
        }
        for (Entity entity : entities)
        {
            for (Entry<EntityDataChanger, Object> entry : changers.entrySet())
            {
                entry.getKey().changer.applyEntity(entity, entry.getValue());
            }
        }
    }
}
