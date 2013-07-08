package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.exception.IncorrectUsageException;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;

public class SpawnMob
{
    // TODO custom loot items & Equipment
    // TODO random color
    // TODO ocelot colors  Ocelot.Type. ...
    // TODO zombie villager
    // TODO skeleton wither
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
        List<String> entityData = new ArrayList<String>();
        if (entityName.isEmpty())
        {
            return null;
        }
        if (entityName.contains(":"))
        {
            entityData = Arrays.asList(StringUtils.explode(":", entityName
                .substring(entityName.indexOf(":") + 1, entityName.length())));
            entityName = entityName.substring(0, entityName.indexOf(":"));
            entityType = Match.entity().mob(entityName);
        }
        else
        {
            entityType = Match.entity().mob(entityName);
        }
        if (entityType == null)
        {
            context.sendTranslated("&cUnknown mob-type: &6%s &cnot found!", entityName);
            return null;
        }
        Entity[] spawnedMobs = new Entity[amount];
        for (int i = 0; i < amount; ++i)
        {
            spawnedMobs[i] = loc.getWorld().spawnEntity(loc, entityType);
            applyDataToMob(context, entityType, spawnedMobs[i], entityData);
            if (ridingOn != null)
            {
                ridingOn[i].setPassenger(spawnedMobs[i]);
            }
            if (spawnedMobs[i] instanceof Skeleton)
            {
                ((Skeleton)spawnedMobs[i]).getEquipment().setItemInHand(new ItemStack(Material.BOW));
            }
        }
        return spawnedMobs;
    }

    static void applyDataToMob(CommandContext context, EntityType entityType, Entity entity, List<String> datas)
    {
        for (String data : datas)
        {
            for (EntityDataChanger entityDataChanger : EntityDataChanger.entityDataChangers)
            {
                if (entityDataChanger.canApply(entity))
                {
                    entityDataChanger.applyTo(entity, data);
                }
            }
        }
    }
}
