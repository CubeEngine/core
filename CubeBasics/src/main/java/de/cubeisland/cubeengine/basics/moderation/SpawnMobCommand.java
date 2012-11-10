package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsConfiguration;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.args.IntArg;
import de.cubeisland.cubeengine.core.command.exception.InvalidUsageException;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.EntityMatcher;
import de.cubeisland.cubeengine.core.util.matcher.EntityType;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import de.cubeisland.cubeengine.core.util.matcher.ProfessionMatcher;
import java.util.Locale;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 * The /spawnmob command.
 */
public class SpawnMobCommand
{
    private BasicsConfiguration config;

    public SpawnMobCommand(Basics basics)
    {
        config = basics.getConfiguration();
    }

    @Command(
        desc = "Spawns the specified Mob",
        max = 3,
        usage = "<mob>[:data][,<ridingmob>[:data]] [amount] [player]")
    public void spawnMob(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        if (!context.hasIndexed(2) && sender == null)
        {
            invalidUsage(context, "basics", "&eSuccesfully spawned some &cbugs &einside your server!");
        }
        if (!context.hasIndexed(0))
        {
            invalidUsage(context, "basics", "&cYou need to define what mob to spawn!");
        }
        Location loc;
        if (context.hasIndexed(2))
        {
            User user = context.getUser(2);
            if (user == null)
            {
                illegalParameter(context, "core", "&cUser %s not found!",context.getString(2));
            }
            loc = user.getLocation();
        }
        else
        {
            loc = sender.getTargetBlock(null, 200).getLocation().add(new Vector(0, 1, 0)); // TODO do Util method for this in core 
        }
        Integer amount = 1;
        if (context.hasIndexed(1))
        {
            amount = context.getIndexed(1, IntArg.class, null);
            if (amount == null)
            {
                illegalParameter(context, "basics", "&e%s is not a number! Really!", context.getString(1));
            }
            if (amount <= 0)
            {
                illegalParameter(context, "basics", "&eAnd how am i supposed to know which mobs to despawn?");
            }
        }
        if (amount > config.spawnmobLimit)
        {
            denyAccess(context, "basics", "&cThe serverlimit is set to &e%d&c, you cannot spawn more mobs at once!", config.spawnmobLimit);
        }
        loc.add(0.5, 0, 0.5);
        Entity entitySpawned = this.spawnMobs(context, context.getString(0), loc, amount);
        if (entitySpawned.getPassenger() == null)
        {
            context.sendMessage("basics", "&aSpawned %d &e%s&a!", amount, EntityType.fromBukkitType(entitySpawned.getType()).toString());
        }
        else
        {
            String message = EntityType.fromBukkitType(entitySpawned.getType()).toString();
            while (entitySpawned.getPassenger() != null)
            {
                entitySpawned = entitySpawned.getPassenger();
                message = _(context.getSender(), "basics", "%s &ariding &e%s", EntityType.fromBukkitType(entitySpawned.getType()).toString(), message);
            }
            message = _(context.getSender(), "basics", "&aSpawned %d &e%s!", amount, message);
            context.sendMessage(message);
        }
    }

    private Entity spawnMobs(CommandContext context, String mobString, Location loc, int amount)
    {
        String[] mobStrings = StringUtils.explode(",", mobString);
        Entity[] mobs = this.spawnMob(context, mobStrings[0], loc, amount, null);
        Entity[] ridingMobs = mobs;
        try
        {
            for (int i = 1; i < mobStrings.length; ++i)
            {
                ridingMobs = this.spawnMob(context, mobStrings[i], loc, amount, ridingMobs);
            }
            return mobs[0];
        }
        catch (InvalidUsageException e)
        {
            context.sendMessage(e.getMessage());
            return mobs[0];
        }
    }

    private Entity[] spawnMob(CommandContext context, String mobString, Location loc, int amount, Entity[] ridingOn)
    {
        String entityName = mobString;
        EntityType entityType;
        String entityData = null;
        if (entityName.contains(":"))
        {
            entityData = entityName.substring(entityName.indexOf(":") + 1, entityName.length());
            entityName = entityName.substring(0, entityName.indexOf(":"));
            entityType = EntityMatcher.get().matchMob(entityName);
        }
        else
        {
            entityType = EntityMatcher.get().matchMob(entityName);
        }
        if (entityName.isEmpty())
        {
            blockCommand();
        }
        if (entityType == null)
        {
            paramNotFound(context, "basics", "&cEntity-type &6%s &cnot found!", entityName);
        }
        Entity[] spawnedMobs = new Entity[amount];
        for (int i = 0; i < amount; ++i)
        {
            spawnedMobs[i] = loc.getWorld().spawnEntity(loc, entityType.getBukkitType());
            this.applyDataToMob(context.getSender(), entityType, spawnedMobs[i], entityData);
            if (ridingOn != null)
            {
                ridingOn[i].setPassenger(spawnedMobs[i]);
            }
        }
        return spawnedMobs;
    }

    private void applyDataToMob(CommandSender sender, EntityType entityType, Entity entity, String data)
    {
        if (data != null)
        {
            String match = StringUtils.matchString(data.toLowerCase(Locale.ENGLISH), "baby", "angry", "tamed", "power", "charged"); //TODO this list configurable something like datavalues.txt

            if (match.equals("baby"))
            {
                if (entityType.isAnimal())
                {
                    ((Animals)entity).setBaby();
                }
                else
                {
                    illegalParameter(sender, "basics", "&eThis entity can not be a baby! Can you?");
                }
            }
            else if (match.equals("angry"))
            {
                if (entityType.equals(EntityType.WOLF))
                {
                    ((Wolf)entity).setAngry(true);
                }
                else if (entityType.equals(EntityType.PIG_ZOMBIE))
                {
                    ((PigZombie)entity).setAngry(true);
                }
            }
            else if (match.equals("tamed"))
            {
                if (entity instanceof Tameable) // Wolf or Ocelot
                {
                    ((Tameable)entity).setTamed(true);
                    if (sender instanceof AnimalTamer)
                    {
                        ((Tameable)entity).setOwner((AnimalTamer)sender);
                    }
                    else
                    {
                        invalidUsage(sender, "basics", "&eYou can not own any Animals!");
                    }
                }
            }
            else if (match.equals("charged") || data.equalsIgnoreCase("power"))
            {
                if (entityType.equals(EntityType.CREEPER))
                {
                    ((Creeper)entity).setPowered(true);
                }
            }
            else if (entityType.equals(EntityType.SHEEP))
            {
                DyeColor color = MaterialMatcher.get().
                    matchColorData(data);
                if (color == null)
                {
                    illegalParameter(sender, "basics", "Color not found!");
                }
                ((Sheep)entity).setColor(color);
            }
            else if (entityType.equals(EntityType.SLIME) || entityType.equals(EntityType.MAGMA_CUBE))
            {
                int size = 4;
                match = StringUtils.matchString(data, "tiny", "small", "big");
                if (match.equals("tiny"))
                {
                    size = 0;
                }
                else if (match.equals("small"))
                {
                    size = 2;
                }
                else if (match.equals("big"))
                {
                    size = 4;
                }
                else
                {
                    try
                    {
                        size = Integer.parseInt(data);
                    }
                    catch (NumberFormatException e)
                    {
                        illegalParameter(sender, "basics", "The slime-size has to be a number or tiny, small or big!");
                    }
                }
                if (size >= 0 && size <= 250)
                {
                    ((Slime)entity).setSize(size);
                }
                else
                {
                    illegalParameter(sender, "basics", "The slime-size can not be smaller than 0 or bigger than 250!");
                }
            }
            else if (entityType.equals(EntityType.VILLAGER))
            {
                Villager.Profession profession = ProfessionMatcher.get().matchProfession(data);
                if (profession == null)
                {
                    illegalParameter(sender, "basics", "Unknown villager-profession!");
                }
                ((Villager)entity).setProfession(profession);
            }
            else if (entityType.equals(EntityType.ENDERMAN))
            {
                ItemStack item = MaterialMatcher.get().matchItemStack(data);
                if (item == null)
                {
                    illegalParameter(sender, "basics", "Material not found!");
                }
                ((Enderman)entity).setCarriedMaterial(item.getData());
            }
        }
    }
}