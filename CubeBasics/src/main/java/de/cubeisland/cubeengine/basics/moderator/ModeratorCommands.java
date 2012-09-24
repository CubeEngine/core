package de.cubeisland.cubeengine.basics.moderator;

import de.cubeisland.cubeengine.basics.Basics;
import static de.cubeisland.cubeengine.core.CubeEngine._;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.EntityMatcher;
import de.cubeisland.cubeengine.core.util.EntityType;
import de.cubeisland.cubeengine.core.util.MaterialMatcher;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 *
 * @author Anselm Brehme
 */
public class ModeratorCommands
{
    UserManager cuManager;

    public ModeratorCommands(Basics module)
    {
        cuManager = module.getUserManager();
    }

    @Command(
    desc = "Spawns the specified Mob",
    min = 1,
    max = 3)
    public void spawnMob(CommandContext context)
    {//TODO later more ridingmobs riding on the riding mob etc...
        // /spawnmob <mob>[:data][,<ridingmob>[:data]] [amount] [player]
        User sender = cuManager.getUser(context.getSender());

        if (sender == null)
        {
            context.getSender().sendMessage(_("core", "&cThis command can only be used by a player!"));
            return;
        }
        EntityType entityType;
        EntityType ridingEntityType;

        String entityName;
        String entityData = null;
        String ridingEntityName = null;
        String ridingEntityData = null;
        String mobString = context.getString(0);
        if (mobString.contains(","))
        {
            entityName = mobString.substring(0, mobString.indexOf(","));
            ridingEntityName = mobString.substring(mobString.indexOf(",") + 1, mobString.length());
        }
        else
        {
            entityName = mobString;
        }
        if (entityName.contains(":"))
        {
            entityData = entityName.substring(entityName.indexOf(":") + 1, entityName.length());
            entityName = entityName.substring(0, entityName.indexOf(":"));
            entityType = EntityMatcher.get().matchMob(entityName);
            if (entityType == null)
            {
                return; //TODO msg invalid mob
            }
        }
        else
        {
            entityType = EntityMatcher.get().matchMob(entityName);
        }
        if (ridingEntityName != null && ridingEntityName.contains(":"))
        {
            ridingEntityData = ridingEntityName.substring(ridingEntityName.indexOf(":") + 1, ridingEntityName.length());
            ridingEntityName = ridingEntityName.substring(0, ridingEntityName.indexOf(":"));
            ridingEntityType = EntityMatcher.get().matchMob(ridingEntityName);
            if (ridingEntityType == null)
            {
                return; //TODO msg invalid ridingmob
            }
        }
        else
        {
            ridingEntityType = EntityMatcher.get().matchMob(ridingEntityName);
        }
        Location loc;
        if (context.hasIndexed(2))
        {
            User user = context.getUser(2);
            if (user == null)
            {
                return;//TODO msg user not found
            }
            loc = user.getLocation();
        }
        else
        {
            loc = sender.getTargetBlock(null, 200).getLocation().add(new Vector(0, 1, 0)); // TODO do Util method for this in core 
        }
        int amount = 1;
        if (context.hasIndexed(1))
        {
            amount = context.getIndexed(1, int.class, 0);
            if (amount == 0)
            {
                return; //TODO msg invalid amount
            }
        }

        for (int i = 1; i <= amount; ++i)
        { //TODO msg succes spawn
            Entity entity = loc.getWorld().spawnEntity(loc, entityType.getBukkitType());
            this.applyDataToMob(entityType, entity, entityData);
            if (ridingEntityType != null)
            {
                Entity ridingentity = loc.getWorld().spawnEntity(loc, ridingEntityType.getBukkitType());
                this.applyDataToMob(ridingEntityType, ridingentity, ridingEntityData);
                entity.setPassenger(ridingentity);
            }
        }
    }

    private void applyDataToMob(EntityType entityType, Entity entity, String data)
    {
        if (data != null)
        {
            if (data.equalsIgnoreCase("baby"))
            {
                if (entityType.isAnimal())
                {
                    ((Animals) entity).setBaby();
                }
                else
                {
                    //could not apply data
                }
            }
            else if (data.equalsIgnoreCase("angry"))
            {
                if (entityType.equals(EntityType.WOLF))
                {
                    ((Wolf) entity).setAngry(true);
                }
                else if (entityType.equals(EntityType.PIG_ZOMBIE))
                {
                    ((PigZombie) entity).setAngry(true);
                }
            }
            else if (data.equalsIgnoreCase("tamed"))
            {
                if (entityType.equals(EntityType.WOLF))
                {
                    ((Wolf) entity).setTamed(true);
                }
                else if (entityType.equals(EntityType.OCELOT))
                {
                    ((Ocelot) entity).setTamed(true);
                }
            }
            else if (data.equalsIgnoreCase("powered") || data.equalsIgnoreCase("power"))
            {
                if (entityType.equals(EntityType.CREEPER))
                {
                    ((Creeper) entity).setPowered(true);
                }
            }
            else
            {
                if (entityType.equals(EntityType.SHEEP))
                {
                    DyeColor color = MaterialMatcher.get().matchColorData(data);
                    if (color == null)
                    {
                        //TODO msg color not found
                        return;
                    }
                    ((Sheep) entity).setColor(color);
                }
                else if (entityType.equals(EntityType.SLIME) || entityType.equals(EntityType.MAGMA_CUBE))
                {
                    int size;
                    try
                    {
                        size = Integer.parseInt(data);
                    }
                    catch (NumberFormatException e)
                    {
                        //TODO msg invalid size
                        return;
                    }
                    if (size > 0 && size <= 250)
                    {
                        ((Slime) entity).setSize(size);
                    }
                }
                else if (entityType.equals(EntityType.VILLAGER))
                {
                    //TODO professions better
                    Profession profession = Profession.valueOf(data);
                    if (profession == null)
                    {
                        return; //TODO msg
                    }
                    ((Villager) entity).setProfession(profession);
                }
                else if (entityType.equals(EntityType.ENDERMAN))
                {
                    //TODO professions better
                    ItemStack item = MaterialMatcher.get().matchItemStack(data);
                    if (item == null)
                    {
                        return; //TODO msg
                    }
                    ((Enderman) entity).setCarriedMaterial(item.getData());
                }
            }
        }
    }

    @Command(
    desc = "Changes the weather",
    min = 1,
    max = 3,
    usage = "/weather <sun|rain|storm> [world] [duration]")
    public void weather(CommandContext context)
    {
        User sender = cuManager.getUser(context.getSender());
        boolean sunny = true;
        boolean noThunder = true;
        int duration = 10000000;
        String weather = context.getString(0);
        if (weather.equalsIgnoreCase("sun"))
        {
            sunny = true;
            noThunder = true;
        }
        else if (weather.equalsIgnoreCase("rain"))
        {
            sunny = false;
            noThunder = true;
        }
        else if (weather.equalsIgnoreCase("storm"))
        {
            sunny = false;
            noThunder = false;
        }
        if (context.hasIndexed(2))
        {
            duration = context.getIndexed(2, int.class, 0);
            if (duration == 0)
            {
                return; //msg invalid time
            }
        }
        World world;
        if (context.hasIndexed(1))
        {
            world = context.getSender().getServer().getWorld(context.getString(1));
            if (world == null)
            {
                return; //TODO msg no world
            }
        }
        else
        {
            if (sender == null)
            {
                return;//IF not a player need world
            }
            world = sender.getWorld();

        }
        world.setStorm(!sunny);
        world.setThundering(!noThunder);
        world.setWeatherDuration(duration);
    }
}