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
package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.exception.IncorrectUsageException;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsConfiguration;

/**
 * The /spawnmob command.
 */
public class SpawnMobCommand
{
    private BasicsConfiguration config;
    private AdvancedSpawnMob advancedSpawnMob;

    public SpawnMobCommand(Basics basics)
    {
        config = basics.getConfiguration();
        this.advancedSpawnMob = new AdvancedSpawnMob(basics);
    }

    @Command(desc = "Spawns the specified Mob", max = 3, usage = "<mob>[:data][,<ridingmob>[:data]] [amount] [player]")
    public void spawnMob(CommandContext context)
    {
        // TODO fix skeleton not having a bow
        // TODO custom loot items
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (!context.hasArg(2) && sender == null)
        {
            context.sendTranslated("&eSuccesfully spawned some &cbugs &einside your server!");
            return;
        }
        if (!context.hasArg(0))
        {
            context.sendTranslated("&cYou need to define what mob to spawn!");
            return;
        }
        if (context.getString(0).equalsIgnoreCase("advanced"))
        {
            advancedSpawnMob.addUser(sender); //TODO
            return;
        }
        Location loc;
        if (context.hasArg(2))
        {
            User user = context.getUser(2);
            if (user == null)
            {
                context.sendTranslated("&cUser &2%s &cnot found!", context.getString(2));
                return;
            }
            loc = user.getLocation();
        }
        else
        {
            loc = sender.getTargetBlock(null, 200).getLocation().add(new Vector(0, 1, 0));
        }
        Integer amount = 1;
        if (context.hasArg(1))
        {
            amount = context.getArg(1, Integer.class, null);
            if (amount == null)
            {
                context.sendTranslated("&e%s is not a number! Really!", context.getString(1));
                return;
            }
            if (amount <= 0)
            {
                context.sendTranslated("&eAnd how am i supposed to know which mobs to despawn?");
                return;
            }
        }
        if (amount > config.spawnmobLimit)
        {
            context.sendTranslated("&cThe serverlimit is set to &e%d&c, you cannot spawn more mobs at once!", config.spawnmobLimit);
            return;
        }
        loc.add(0.5, 0, 0.5);
        Entity[] entitiesSpawned = spawnMobs(context, context.getString(0), loc, amount);
        if (entitiesSpawned == null)
        {
            return;
        }
        Entity entitySpawned = entitiesSpawned[0];
        if (entitySpawned.getPassenger() == null)
        {
            context.sendTranslated("&aSpawned %d &e%s&a!", amount, Match.entity().getNameFor(entitySpawned.getType()));
        }
        else
        {
            String message = Match.entity().getNameFor(entitySpawned.getType());
            while (entitySpawned.getPassenger() != null)
            {
                entitySpawned = entitySpawned.getPassenger();
                message = context.getSender().translate("%s &ariding &e%s", Match.entity().getNameFor(entitySpawned.getType()), message);
            }
            message = context.getSender().translate("&aSpawned %d &e%s!", amount, message);
            context.sendMessage(message);
        }
    }

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
            entityData = Arrays.asList(StringUtils.explode(":", entityName.substring(entityName.indexOf(":") + 1, entityName.length())));
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
            if (!applyDataToMob(context, entityType, spawnedMobs[i], entityData))
                return spawnedMobs;
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

    static boolean applyDataToMob(CommandContext context, EntityType entityType, Entity entity, List<String> datas)
    {
        for (String data : datas)
        {
            String match = Match.string().matchString(data.toLowerCase(Locale.ENGLISH), "saddled", "baby", "angry", "tamed", "power", "charged", "sitting");
            //TODO this list configurable something like datavalues.txt
            if (match == null)
            {
                if (data.toLowerCase(Locale.ENGLISH).endsWith("hp"))
                {
                    try
                    {
                        int hp = Integer.parseInt(data.substring(0, data.length() - 2));
                        EntityDataChanger.HP.applyTo(entity,hp);
                    }
                    catch (NumberFormatException e)
                    {
                        context.sendTranslated("&cInvalid HP amount!");
                    }
                }
            }
            else if ("baby".equals(match))
            {
                if (!EntityDataChanger.BABYAGEABLE.applyTo(entity,true))
                {
                    context.sendTranslated("&eThis entity can not be a baby! Can you?");
                }
            }
            else if ("saddled".equals(match))
            {
                if (!EntityDataChanger.PIGSADDLE.applyTo(entity,true))
                {
                    context.sendTranslated("&eOnly Pigs can be saddled!");
                }
            }
            else if ("angry".equals(match))
            {
                if (!EntityDataChanger.ANGRYWOLF.applyTo(entity,true) || !EntityDataChanger.ANGRYPIGZOMBIE.applyTo(entity,true))
                {
                    context.sendTranslated("&eOnly Wolfs or PigZombies can be aggro!");
                }
            }
            else if ("tamed".equals(match))
            {
                AnimalTamer tamer =null;
                if (context.getSender() instanceof AnimalTamer)
                {
                    tamer = (AnimalTamer)context.getSender();
                }
                if (!EntityDataChanger.TAME.applyTo(entity,tamer))
                {
                    context.sendTranslated("&eOnly Wolfs or Ocelots can be tamed!");
                }
            }
            else if ("sitting".equals(match))
            {
                if (!EntityDataChanger.SITTINGOCELOT.applyTo(entity,true) || !EntityDataChanger.SITTINGWOLF.applyTo(entity,true))
                {
                    context.sendTranslated("&eOnly a wolfs and ocelots can sit!");
                }
            }
            else if ("charged".equals(match) || data.equalsIgnoreCase("power"))
            {
                if (!EntityDataChanger.POWERED.applyTo(entity,true))
                {
                    context.sendTranslated("&eYou can only charge creepers!");
                }
            }
            if (entityType.equals(EntityType.SHEEP))
            {
                DyeColor color = Match.materialData().colorData(data);
                if (color == null)
                {
                    context.sendTranslated("&cInvalid SheepColor: " + data);
                }
                else
                {
                    EntityDataChanger.SHEEP_COLOR.applyTo(entity,color);
                }
            }
            else if (entityType.equals(EntityType.SLIME) || entityType.equals(EntityType.MAGMA_CUBE))
            {
                match = Match.string().matchString(data, "tiny", "small", "big");
                try
                {
                    int size = "tiny".equals(match) ? 0
                        : "small".equals(match) ? 2
                        : "big".equals(match) ? 4
                        : Integer.parseInt(data);
                    EntityDataChanger.SLIME_SIZE.applyTo(entity,size);
                }
                catch (NumberFormatException e)
                {
                    context.sendTranslated("&eThe slime-size has to be a number or tiny, small or big!");
                    return false;
                }
            }
            else if (entityType.equals(EntityType.VILLAGER))
            {
                Villager.Profession profession = Match.profession().profession(data);
                if (profession == null)
                {
                    context.sendTranslated("Unknown villager-profession!");
                    return false;
                }
                EntityDataChanger.VILLAGER_PROFESSION.applyTo(entity,profession);
            }
            else if (entityType.equals(EntityType.ENDERMAN))
            {
                ItemStack item = Match.material().itemStack(data);
                if (item == null)
                {
                    context.sendTranslated("Material not found!");
                    return false;
                }
                EntityDataChanger.ENDERMAN_ITEM.applyTo(entity,item);
            }
        }
        return true;
    }
}
