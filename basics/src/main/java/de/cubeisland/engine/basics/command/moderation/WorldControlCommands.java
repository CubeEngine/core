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
import de.cubeisland.engine.basics.BasicsPerm;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.parameterized.completer.WorldCompleter;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;

import static de.cubeisland.engine.basics.command.moderation.EntityRemoval.DIRECT_ENTITY_REMOVAL;
import static de.cubeisland.engine.basics.command.moderation.EntityRemoval.GROUPED_ENTITY_REMOVAL;
import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;

/**
 * Commands controlling / affecting worlds. /weather /remove /butcher
 */
public class WorldControlCommands
{

    private BasicsConfiguration config;

    public WorldControlCommands(Basics module)
    {
        this.config = module.getConfiguration();
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
            context.sendTranslated("&cInvalid weather!\n&eUse &6sun&e, &6rain&e or &6storm&e!");
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
                context.sendTranslated("&cThe given duration is invalid!");
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
                context.sendTranslated("&cWorld &6%s &cnot found!", context.getString(1));
                return;
            }
        }
        else
        {
            if (sender == null)
            {
                throw new IncorrectUsageException(context.getSender().translate("&cIf not used ingame you have to specify a world!"));
            }
            world = sender.getWorld();
        }
        if (world.isThundering() != noThunder && world.hasStorm() != sunny) // weather is not changing
        {
            context.sendTranslated("&aWeather in &6%s &awas already set to &e%s&a!", world.getName(), weather);
        }
        else
        {
            context.sendTranslated("&aChanged weather in &6%s &ato &e%s&a!", world.getName(), weather);
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
                context.sendTranslated("&cThe butcher will come to YOU tonight!");
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
            context.sendTranslated("&cIf not used ingame you can only remove all!");
            return;
        }
        if (context.hasArg(1))
        {
            radius = context.getArg(1, Integer.class, 0);
            if (radius <= 0)
            {
                context.sendTranslated("&cThe radius has to be a number greater than 0!");
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
                        context.sendTranslated("&cYou can only specify data for removing items!");
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
                        context.sendTranslated("&cInvalid entity-type!\n&eUse &6"
                                                   + EntityType.DROPPED_ITEM + "&e, &6" + EntityType.ARROW + "&e, &6"
                                                   + EntityType.BOAT + "&e, &6" + EntityType.MINECART + "&e, &6"
                                                   + EntityType.PAINTING + "&e, &6" + EntityType.ITEM_FRAME + " &eor &6"
                                                   + EntityType.EXPERIENCE_ORB);
                        return;
                    }
                    if (type.isAlive())
                    {
                        context.sendTranslated("&cTo kill living entities use the &e/butcher &ccommand!");
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
            context.sendTranslated("&eNo entities to remove!");
        }
        else if (context.getString(0).equalsIgnoreCase("*"))
        {
            if (radius == -1)
            {
                context.sendTranslated("&aRemoved all entities in &6%s&a! &f(&6%d&f)", world.getName(), entitiesRemoved);
                return;
            }
            context.sendTranslated("&aRemoved all entities around you! &f(&6%d&f)", entitiesRemoved);
        }
        else
        {
            if (radius == -1)
            {
                context.sendTranslated("&aRemoved &e%d &aentities in '&6%s&a!", entitiesRemoved, world.getName());
                return;
            }
            context.sendTranslated("&aRemoved &e%d &aentities around you!", entitiesRemoved);
        }
    }

    @Command(desc = "Gets rid of mobs nearby you. Valid types are:\n" +
        "monster, animal, pet, golem, boss, other, creeper, skeleton, spider etc.", flags = {
        @Flag(longName = "lightning", name = "l"), // die with style
        @Flag(longName = "all", name = "a")// infinite radius
    }, params = @Param(names = "in", type = World.class, completer = WorldCompleter.class),
             usage = "[types...] [radius] [in world] [-l] [-all]", max = 2)
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
            if (radius < 0 && !(radius == -1 && BasicsPerm.COMMAND_BUTCHER_FLAG_ALL.isAuthorized(context.getSender())))
            {
                context.sendTranslated("&cThe radius has to be a number greater than 0!");
                return;
            }
        }
        if (context.hasFlag("a") && BasicsPerm.COMMAND_BUTCHER_FLAG_ALL.isAuthorized(context.getSender()))
        {
            radius = -1;
        }
        boolean lightning = false;
        if (context.hasFlag("l") && BasicsPerm.COMMAND_BUTCHER_FLAG_LIGHTNING.isAuthorized(context.getSender()))
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
                if (!BasicsPerm.COMMAND_BUTCHER_FLAG_ALLTYPE.isAuthorized(context.getSender()))
                {
                    context.sendTranslated("&cYou are not allowed to butcher all types of living entities at once!");
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
                String match = Match.string().matchString(s_type, GROUPED_ENTITY_REMOVAL.keySet());
                EntityType directEntityMatch = null;
                if (match == null)
                {
                    directEntityMatch = Match.entity().living(s_type);
                    if (directEntityMatch == null)
                    {
                        context.sendTranslated("&cUnkown entity &6%s", s_type);
                        return;
                    }
                    if (DIRECT_ENTITY_REMOVAL.get(directEntityMatch) == null) throw new IllegalStateException("Missing Entity? " + directEntityMatch);
                }
                EntityRemoval entityRemoval;
                if (directEntityMatch != null)
                {
                    entityRemoval = DIRECT_ENTITY_REMOVAL.get(directEntityMatch);
                }
                else
                {
                    entityRemoval = GROUPED_ENTITY_REMOVAL.get(match);
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
        context.sendTranslated(removed == 0 ? "&eNothing to butcher!" : "&aYou just slaughtered &e%d &aliving entities!", removed);
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
