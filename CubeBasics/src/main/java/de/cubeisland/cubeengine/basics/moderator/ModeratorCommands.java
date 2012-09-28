package de.cubeisland.cubeengine.basics.moderator;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.EntityMatcher;
import de.cubeisland.cubeengine.core.util.EntityType;
import de.cubeisland.cubeengine.core.util.MaterialMatcher;
import java.util.List;
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
    private UserManager cuManager;

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
        User sender = context.getSenderAsUser(true);
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
        }
        else
        {
            entityType = EntityMatcher.get().matchMob(entityName);

        }
        if (entityType == null)
        {
            return; //TODO msg invalid mob
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
            else if (data.equalsIgnoreCase("tamed")) //TODO set owner
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
                    //TODO professions better now have to write in capslock :(
                    Profession profession = Profession.valueOf(data);
                    if (profession == null)
                    {
                        return; //TODO msg
                    }
                    ((Villager) entity).setProfession(profession);
                }
                else if (entityType.equals(EntityType.ENDERMAN))
                {
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
        User sender = context.getSenderAsUser();
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

    @Command(
    desc = "Changes the global respawnpoint",
    usage = "/setspawn [world] [<x> <y> <z>]",
    max = 4)
    public void setSpawn(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        Integer x;
        Integer y;
        Integer z;
        World world;
        if (context.hasIndexed(0))
        {
            world = context.getSender().getServer().getWorld(context.getString(0));
            if (world == null)
            {
                return; //TODO msg no such world
            }
        }
        else
        {
            if (sender == null)
            {
                return; //TODO msg if not a player give world
            }
            world = sender.getWorld();
        }
        
        if (context.hasIndexed(3))
        {
            x = context.getIndexed(1, Integer.class, null);
            y = context.getIndexed(2, Integer.class, null);
            z = context.getIndexed(3, Integer.class, null);
            if (x==null || y == null || z == null)
            {
                return; //TODO msg invalid coords
            }
        }
        else
        {
            if (sender == null)
            {
                return; //TODO msg if not a player give coords
            }
            x = sender.getLocation().getBlockX();
            y = sender.getLocation().getBlockY();
            z = sender.getLocation().getBlockZ();
        }
        world.setSpawnLocation(x,y,z);
        //TODO msg spawn set.
    }
    
    @Command(
    desc = "Kills a player",
    usage = "/kill <player>",
    min = 1,
    max = 1)
    public void kill(CommandContext context)
    {//TODO kill a player looking at
        //TODO kill a player with cool effects :) e.g. lightnin
        //TODO perm checks if user can be killed
        User sender = context.getSenderAsUser();
        User user = context.getUser(0, true);
        user.setHealth(0);
        //TODO broadcast Deathmsg etc
        //TODO msg you killed ...
    }
    
    @Command(
    names={"ping","pong"},
    desc = "Pong!",
    usage = "/ping",
    min = 1,
    max = 1)
    public void ping(CommandContext context)
    {
        if (context.getLabel().equalsIgnoreCase("ping"))
        {
            context.sendMessage("basics", "Pong!");
        }
        else if (context.getLabel().equalsIgnoreCase("pong"))
        {
            context.sendMessage("basics", "Ping!");
        }
    }
    
    @Command(
    desc = "Removes entity",
    usage = "/remove <entityType> [radius] [in <world>] [-a]",
    flags = {@Flag(longName="all",name="a")},
    params= {@Param(names={"in"},types=World.class)},
    min = 1,
    max = 1)
    public void remove(CommandContext context)
    {/*
     Drops
     Arrows
     Boats
     Minecarts
     xp
     paintings
     
     other non living possible too
     TODO 
     */
        User sender = context.getSenderAsUser();
        World world;
        if (context.hasNamed("in"))
        {
            world = context.getNamed("in", World.class);
        }
        else
        {
            if (sender == null)
            {
                return; //TODO msg no player or world :(
            }
            world = sender.getWorld();
        }

        int radius = 20; //TODO default radius in config!
        if (context.hasFlag("a")) // remove all selected entities in world
        {
            radius = -1;
        }
        else
        {
            if (sender == null)
            {
                return; // no player -> no location TODO msg
            }
            if (context.hasIndexed(1))
            {
                radius = context.getIndexed(1, int.class, 0);
                if (radius == 0)
                {
                    return; //TODO msg invalid radius
                }
            }
        }
        EntityType type = EntityMatcher.get().matchEntity(context.getString(0));
        if (type.isAlive())
        {
            // TODO msg to remove livingentities use butcher
            return;
        }
        Location loc = null;
        if (sender != null)
        {
            loc = sender.getLocation();
        }
        this.removeEntityType(world.getEntities(), loc, radius, type);
    }
    
    private int removeEntityType(List<Entity> list, Location loc, int radius, EntityType type)
    {
        if (loc == null && radius != -1)
        {
            throw new IllegalStateException("Unkown Location with Radius");
        }
        int removed = 0;
        
        for (Entity entity : list)
        {
            if (entity.getType().equals(type.getBukkitType()))
            {
                continue;
            }
            if (radius != -1)
            {
                int distance = (int) (entity.getLocation().subtract(loc)).lengthSquared();
                if (radius * radius < distance)
                {
                    continue;
                }
            }
            entity.remove();
            removed++;
        }
        return removed;
    }
    
    @Command(
    desc = "Clears the inventory",
    usage = "/ci [player]",
    max = 1)
    public void clearinventory(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        User user = sender;
        boolean other = false;
        if (context.hasIndexed(0))
        {
            user = context.getUser(0, true);
            other = true;
        }
        user.getInventory().clear();
        //TODO later save inventory to restore later ??
        user.updateInventory();
        user.sendMessage("basics","Cleared Inventory!");
        if (other)
        {
            sender.sendMessage("basics", "Cleared Inventory of %s!", user.getName());
        }
    }
    
    @Command(
    desc = "Broadcasts a message",
    usage = "/broadcast <message>"
    )
    public void broadcast(CommandContext context)
    {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (context.hasIndexed(i))
        {
            sb.append(context.getString(i++));
        }
        context.getSender().getServer().broadcastMessage(sb.toString());
    }
    
    @Command(
    desc = "Makes a player execute a command",
    usage = "/sudo <player> <command>"
    )
    public void sudo(CommandContext context)
    {
        
        User sender = context.getSenderAsUser();
        User user = context.getUser(0, true);
        StringBuilder sb = new StringBuilder();
        int i = 1;
        while (context.hasIndexed(i))
        {
            sb.append(context.getString(i++));
        }
        user.chat("/" + sb.toString()); //TODO add flag for chat not cmd
        //TODO msg to sender if cmd worked??
    }
}