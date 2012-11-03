package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsConfiguration;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.EntityMatcher;
import de.cubeisland.cubeengine.core.util.matcher.EntityType;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;

/**
 * Commands controling / affecting worlds.
 * /weather
 * /remove
 */
public class WorldControlCommands
{
    private BasicsConfiguration config;

    public WorldControlCommands(Basics basics)
    {
        config = basics.getConfiguration();
    }

    @Command(
        desc = "Changes the weather",
        min = 1,
        max = 3,
        usage = "<sun|rain|storm> [world] [duration]")
    public void weather(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        boolean sunny = true;
        boolean noThunder = true;
        int duration = 10000000;
        String weather = StringUtils.matchString(context.getString(0), "sun", "rain", "storm");
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
                illegalParameter(context, "basics", "The given time is invalid!");
            }
        }
        World world;
        if (context.hasIndexed(1))
        {
            world = context.getSender().getServer().getWorld(context.getString(1));
            if (world == null)
            {
                illegalParameter(context, "basics", "World %s not found!", context.getString(1));
            }
        }
        else if (sender == null)
        {
            invalidUsage(context, "basics", "If not used ingame you have to specify a world!");
        }
        world = sender.getWorld();
        world.setStorm(!sunny);
        world.setThundering(!noThunder);
        world.setWeatherDuration(duration);
    }

    @Command(
        desc = "Removes entity",
        usage = "<entityType> [radius] [in <world>] [-a]",
        flags = { @Flag(longName = "all", name = "a") },
        params = { @Param(names = { "in" }, types = World.class) },
        min = 1)
    public void remove(CommandContext context)
    {
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
                invalidUsage(context, "basics", "If not used ingame you have to specify a world!");
            }
            world = sender.getWorld();
        }
        int radius = config.removeCmdDefaultRadius;
        if (context.hasFlag("a")) // remove all selected entities in world
        {
            radius = -1;
        }
        else if (sender == null)
        {
            invalidUsage(context, "basics", "If not used ingame you can only remove all!");
        }
        if (context.hasIndexed(1))
        {
            radius = context.getIndexed(1, int.class, 0);
            if (radius <= 0)
            {
                illegalParameter(context, "basics", "The radius has to be a number greater than 0!");
            }
        }
        EntityType type = EntityMatcher.get().matchEntity(context.getString(0));
        if (type == null)
        {
            illegalParameter(context, "basics", "Invalid entity-type!\nUse "
                + EntityType.DROPPED_ITEM + ", " + EntityType.ARROW + ", "
                + EntityType.BOAT + ", " + EntityType.MINECART + ", "
                + EntityType.EXPERIENCE_ORB + " or " + EntityType.ARROW);
        }
        if (type.isAlive())
        {
            invalidUsage(context, "basics", "To kill living entities use the butcher command!");
        }
        Location loc = null;
        if (sender != null)
        {
            loc = sender.getLocation();
        }
        int entitiesRemoved = this.removeEntityType(world.getEntities(), loc, radius, type);
        context.sendMessage("basics", "Removed %d entities!", entitiesRemoved);
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
            if (!entity.getType().equals(type.getBukkitType()))
            {
                continue;
            }
            if (radius != -1)
            {
                int distance = (int)(entity.getLocation().subtract(loc)).lengthSquared();
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
}