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
package de.cubeisland.engine.basics.command.moderation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsConfiguration;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.parameterized.completer.WorldCompleter;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

/**
 * Commands controlling / affecting worlds. /weather /remove /butcher
 */
public class WorldControlCommands
{
    private final BasicsConfiguration config;
    private final Basics module;
    private final EntityRemovals entityRemovals;

    public WorldControlCommands(Basics module)
    {
        this.module = module;
        this.config = module.getConfiguration();
        this.entityRemovals = new EntityRemovals(module);
    }

    @Command(desc = "Changes the weather", min = 1, max = 3,
             usage = "<sun|rain|storm> [duration] [in <world>]",
             params = @Param(names = "in", type = World.class))
    public void weather(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        boolean sunny = true;
        boolean noThunder = true;
        int duration = 10000000;
        String weather = Match.string().matchString(context.getString(0), "sun", "rain", "storm");
        if (weather == null)
        {
            context.sendTranslated(NEGATIVE, "Invalid weather! {input}", context.getString(0));
            context.sendTranslated(NEUTRAL, "Use {name#sun}, {name#rain} or {name#storm}!", "sun", "rain", "storm");
            return;
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
        if (context.hasArg(1))
        {
            duration = context.getArg(1, Integer.class, 0);
            if (duration == 0)
            {
                context.sendTranslated(NEGATIVE, "The given duration is invalid!");
                return;
            }
            duration *= 20;
        }
        World world;
        if (context.hasParam("in"))
        {
            world = context.getParam("in", null);
            if (world == null)
            {
                context.sendTranslated(NEGATIVE, "World {input#world} not found!", context.getString(1));
                return;
            }
        }
        else
        {
            if (sender == null)
            {
                throw new IncorrectUsageException(context.getSender().getTranslation(NEGATIVE, "If not used ingame you have to specify a world!"));
            }
            world = sender.getWorld();
        }
        if (world.isThundering() != noThunder && world.hasStorm() != sunny) // weather is not changing
        {
            context.sendTranslated(POSITIVE, "Weather in {world} is already set to {input#weather}!", world, weather);
        }
        else
        {
            context.sendTranslated(POSITIVE, "Changed weather in {world} to {input#weather}!", world, weather);
        }
        world.setStorm(!sunny);
        world.setThundering(!noThunder);
        world.setWeatherDuration(duration);
    }

    @Command(desc = "Removes entity", usage = "<entityType[:itemMaterial]> [radius]|[-all] [in <world>]", flags = @Flag(longName = "all", name = "a"), params = @Param(names = {
        "in"
    }, type = World.class), min = 1, max = NO_MAX)
    public void remove(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        World world;
        if (context.hasParam("in"))
        {
            world = context.getParam("in");
        }
        else
        {
            if (sender == null)
            {
                context.sendTranslated(NEGATIVE, "The butcher will come for YOU tonight!");
                return;
            }
            world = sender.getWorld();
        }
        int radius = this.config.commands.removeDefaultRadius;
        if (context.hasFlag("a")) // remove all selected entities in world
        {
            radius = -1;
        }
        else if (sender == null)
        {
            context.sendTranslated(NEGATIVE, "If not used ingame you can only remove all!");
            return;
        }
        if (context.hasArg(1))
        {
            radius = context.getArg(1, Integer.class, 0);
            if (radius <= 0)
            {
                context.sendTranslated(NEGATIVE, "The radius has to be a whole number greater than 0!");
                return;
            }
        }
        Location loc = null;
        if (sender != null)
        {
            loc = sender.getLocation();
        }
        int entitiesRemoved;
        if (context.getString(0).equalsIgnoreCase("*"))
        {
            List<Entity> list = new ArrayList<>();
            for (Entity entity : world.getEntities())
            {
                if (!(entity instanceof LivingEntity))
                {
                    list.add(entity);
                }
            }
            entitiesRemoved = this.removeEntities(list, loc, radius, false);
        }
        else
        {
            List<Entity> list = world.getEntities(); // All entites remaining in that list will not get deleted!
            String[] s_entityTypes = StringUtils.explode(",", context.getString(0));
            List<org.bukkit.entity.EntityType> types = new ArrayList<>();
            for (String s_entityType : s_entityTypes)
            {
                if (s_entityType.contains(":"))
                {
                    EntityType type = Match.entity().any(s_entityType.substring(0, s_entityType.indexOf(":")));
                    if (!EntityType.DROPPED_ITEM.equals(type))
                    {
                        context.sendTranslated(NEGATIVE, "You can only specify data for removing items!");
                        return;
                    }
                    Material itemtype = Match.material().material(s_entityType.substring(s_entityType.indexOf(":") + 1));
                    List<Entity> remList = new ArrayList<>();
                    for (Entity entity : list)
                    {
                        if (entity.getType().equals(EntityType.DROPPED_ITEM) && ((Item)entity).getItemStack().getType().equals(itemtype))
                        {
                            remList.add(entity);
                        }
                    }
                    list.removeAll(remList);
                }
                else
                {
                    EntityType type = Match.entity().any(s_entityType);
                    if (type == null)
                    {
                        context.sendTranslated(NEGATIVE, "Invalid entity-type!");
                        context.sendTranslated(NEUTRAL, "Use one of those instead:");
                        context.sendMessage(EntityType.DROPPED_ITEM.toString() + ChatFormat.YELLOW + ", " +
                                                ChatFormat.GOLD + EntityType.ARROW + ChatFormat.YELLOW + ", " +
                                                ChatFormat.GOLD + EntityType.BOAT + ChatFormat.YELLOW + ", " +
                                                ChatFormat.GOLD + EntityType.MINECART + ChatFormat.YELLOW + ", " +
                                                ChatFormat.GOLD + EntityType.PAINTING + ChatFormat.YELLOW + ", " +
                                                ChatFormat.GOLD + EntityType.ITEM_FRAME + ChatFormat.YELLOW + " or " +
                                                ChatFormat.GOLD + EntityType.EXPERIENCE_ORB);
                        return;
                    }
                    if (type.isAlive())
                    {
                        context.sendTranslated(NEGATIVE, "To kill living entities use the {text:/butcher} command!");
                        return;
                    }
                    types.add(type);
                }
            }
            List<Entity> remList = new ArrayList<>();
            for (Entity entity : list)
            {
                if (types.contains(entity.getType()))
                {
                    remList.add(entity);
                }
            }
            list.removeAll(remList);
            remList = world.getEntities();
            remList.removeAll(list);
            entitiesRemoved = this.removeEntities(remList, loc, radius, false);
        }
        if (entitiesRemoved == 0)
        {
            context.sendTranslated(NEUTRAL, "No entities to remove!");
        }
        else if (context.getString(0).equalsIgnoreCase("*"))
        {
            if (radius == -1)
            {
                context.sendTranslated(POSITIVE, "Removed all entities in {world}! ({amount})", world, entitiesRemoved);
                return;
            }
            context.sendTranslated(POSITIVE, "Removed all entities around you! ({amount})", entitiesRemoved);
        }
        else
        {
            if (radius == -1)
            {
                context.sendTranslated(POSITIVE, "Removed {amount} entities in {world}!", entitiesRemoved, world);
                return;
            }
            context.sendTranslated(POSITIVE, "Removed {amount} entities nearby!", entitiesRemoved); // TODO a non-plural version if there is only 1 entity
        }
    }

    @Command(desc = "Gets rid of mobs close to you. Valid types are:\n" +
        "monster, animal, pet, golem, boss, other, creeper, skeleton, spider etc.", flags = {
        @Flag(longName = "lightning", name = "l"), // die with style
        @Flag(longName = "all", name = "a")// infinite radius
    }, params = @Param(names = "in", type = World.class, completer = WorldCompleter.class),
             usage = "[types...] [radius] [in <world>] [-l] [-all]", max = 2)
    public void butcher(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        Location loc;
        int radius = this.config.commands.butcherDefaultRadius;
        int removed;
        if (sender == null)
        {
            radius = -1;
            loc = this.config.mainWorld.getSpawnLocation();
        }
        else
        {
            loc = sender.getLocation();
        }
        if (context.hasArg(1))
        {
            radius = context.getArg(1, Integer.class, 0);
            if (radius < 0 && !(radius == -1 && module.perms().COMMAND_BUTCHER_FLAG_ALL.isAuthorized(context
                                                                                                         .getSender())))
            {
                context.sendTranslated(NEGATIVE, "The radius has to be a number greater than 0!");
                return;
            }
        }
        if (context.hasFlag("a") && module.perms().COMMAND_BUTCHER_FLAG_ALL.isAuthorized(context.getSender()))
        {
            radius = -1;
        }
        boolean lightning = false;
        if (context.hasFlag("l") && module.perms().COMMAND_BUTCHER_FLAG_LIGHTNING.isAuthorized(context.getSender()))
        {
            lightning = true;
        }
        List<Entity> list;
        if (context.getSender() instanceof User && !(radius == -1))
        {
            list = ((User)context.getSender()).getNearbyEntities(radius, radius, radius);
        }
        else
        {
            list = loc.getWorld().getEntities();
        }
        String[] s_types = { "monster" };
        boolean allTypes = false;
        if (context.hasArg(0))
        {
            if (context.getString(0).equals("*"))
            {
                allTypes = true;
                if (!module.perms().COMMAND_BUTCHER_FLAG_ALLTYPE.isAuthorized(context.getSender()))
                {
                    context.sendTranslated(NEGATIVE, "You are not allowed to butcher all types of living entities at once!");
                    return;
                }
            }
            else
            {
                s_types = StringUtils.explode(",", context.getString(0));
            }
        }
        List<Entity> remList = new ArrayList<>();
        if (!allTypes)
        {
            for (String s_type : s_types)
            {
                String match = Match.string().matchString(s_type, this.entityRemovals.GROUPED_ENTITY_REMOVAL.keySet());
                EntityType directEntityMatch = null;
                if (match == null)
                {
                    directEntityMatch = Match.entity().living(s_type);
                    if (directEntityMatch == null)
                    {
                        context.sendTranslated(NEGATIVE, "Unknown entity {input#entity}", s_type);
                        return;
                    }
                    if (this.entityRemovals.DIRECT_ENTITY_REMOVAL.get(directEntityMatch) == null) throw new IllegalStateException("Missing Entity? " + directEntityMatch);
                }
                EntityRemoval entityRemoval;
                if (directEntityMatch != null)
                {
                    entityRemoval = this.entityRemovals.DIRECT_ENTITY_REMOVAL.get(directEntityMatch);
                }
                else
                {
                    entityRemoval = this.entityRemovals.GROUPED_ENTITY_REMOVAL.get(match);
                }
                for (Entity entity : list)
                {
                    if (entityRemoval.doesMatch(entity) && entityRemoval.isAllowed(context.getSender()))
                    {
                        remList.add(entity);
                    }
                }
            }
        }
        else
        {
            remList.addAll(list);
        }
        list = new ArrayList<>();
        for (Entity entity : remList)
        {
            if (entity.getType().isAlive())
            {
                list.add(entity);
            }
        }
        removed = this.removeEntities(list, loc, radius, lightning);
        if (removed == 0)
        {
            context.sendTranslated(NEUTRAL, "Nothing to butcher!");
        }
        else
        {
            context.sendTranslated(POSITIVE, "You just slaughtered {amount} living entities!", removed);
        }

    }

    private int removeEntities(List<Entity> remList, Location loc, int radius, boolean lightning)
    {
        int removed = 0;

        if (radius != -1 && loc == null)
        {
            throw new IllegalStateException("Unknown Location with radius");
        }
        boolean all = radius == -1;
        int radiusSquared = radius * radius;
        final Location entityLocation = new Location(null, 0, 0, 0);
        for (Entity entity : remList)
        {
            if (entity instanceof Player)
            {
                continue;
            }
            entity.getLocation(entityLocation);
            if (!all)
            {

                int distance = (int)(entityLocation.subtract(loc)).lengthSquared();
                if (radiusSquared < distance)
                {
                    continue;
                }
            }
            if (lightning)
            {
                entityLocation.getWorld().strikeLightningEffect(entityLocation);
            }
            entity.remove();
            removed++;
        }
        return removed;
    }
}
