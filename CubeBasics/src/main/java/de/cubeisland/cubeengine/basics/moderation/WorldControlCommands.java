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
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;

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
        usage = "<sun|rain|storm> [duration] [in <world>]",
        params = @Param(names = "in", type = World.class)
    )
    public void weather(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        boolean sunny = true;
        boolean noThunder = true;
        int duration = 10000000;
        String weather = StringUtils.matchString(context.getString(0), "sun", "rain", "storm");
        if (weather == null)
        {
            paramNotFound(context, "basics", "&cInvalid weather!\n&eUse &6sun&e, &6rain &eor &6storm&e!");
        }
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
        if (context.hasIndexed(1))
        {
            duration = context.getIndexed(1, Integer.class, 0);
            if (duration == 0)
            {
                illegalParameter(context, "basics", "&cThe given duration is invalid!");
            }
            duration *= 20;
        }
        World world = null;
        if (context.hasNamed("in"))
        {
            world = context.getNamed("in", World.class, null);
            if (world == null)
            {
                illegalParameter(context, "basics", "&cWorld &6%s &cnot found!", context.getString(1));
            }
        }
        else
        {
            if (sender == null)
            {
                invalidUsage(context, "basics", "&cIf not used ingame you have to specify a world!");
            }
            else
            {
                world = sender.getWorld();
            }
        }
        world.setStorm(!sunny);
        world.setThundering(!noThunder);
        world.setWeatherDuration(duration);
        context.sendMessage("basics", "&aChanged wheather in &6%s &ato &e%s&a!", world.getName(), weather);
    }

    @Command(
        desc = "Removes entity",
        usage = "<entityType[:itemMaterial]> [radius] [in <world>] [-a]",
        flags = { @Flag(longName = "all", name = "a") },
        params = @Param(names = { "in" }, type = World.class),
        min = 1
    )
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
                invalidUsage(context, "basics", "&cIf not used ingame you have to specify a world!");//TODO funny msg
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
            invalidUsage(context, "basics", "&cIf not used ingame you can only remove all!");//TODO funny msg
        }
        if (context.hasIndexed(1))
        {
            radius = context.getIndexed(1, Integer.class, 0);
            if (radius <= 0)
            {
                illegalParameter(context, "basics", "&cThe radius has to be a number greater than 0!");
            }
        }
        String s_type = context.getString(0);
        EntityType type = EntityMatcher.get().matchEntity(s_type);
        Material itemtype = null;
        if (type == null)
        {
            if (s_type.contains(":"))
            {
                type = EntityMatcher.get().matchEntity(s_type.substring(0, s_type.indexOf(":")));
                itemtype = MaterialMatcher.get().matchMaterial(s_type.substring(s_type.indexOf(":") + 1));
            }
            if (type == null)
            {
                paramNotFound(context, "basics", "&cInvalid entity-type!\n&eUse &6"
                    + EntityType.DROPPED_ITEM + "&e, &6" + EntityType.ARROW + "&e, &6"
                    + EntityType.BOAT + "&e, &6" + EntityType.MINECART + "&e, &6"
                    + EntityType.PAINTING + "&e, &6" + EntityType.ITEM_FRAME + " &eor &6"
                    + EntityType.EXPERIENCE_ORB);
            }
        }
        if (type.isAlive())
        {
            blockCommand(context, "basics", "&cTo kill living entities use the &e/butcher &ccommand!");
        }
        Location loc = null;
        if (sender != null)
        {
            loc = sender.getLocation();
        }
        int entitiesRemoved = this.removeEntityType(world.getEntities(), loc, radius, type, itemtype);
        context.sendMessage("basics", "&aRemoved &e%d &aentities!", entitiesRemoved);
    }

    private int removeEntityType(List<Entity> list, Location loc, int radius, EntityType type, Material itemtype)
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
            if (entity instanceof Item && itemtype != null)
            {
                if (!((Item)entity).getItemStack().getType().equals(itemtype))
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