package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsConfiguration;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.EntityMatcher;
import de.cubeisland.cubeengine.core.util.matcher.EntityType;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.WaterMob;

import java.util.ArrayList;
import java.util.List;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;

/**
 * Commands controling / affecting worlds. /weather /remove /butcher
 */
public class WorldControlCommands
{
    private BasicsConfiguration config;

    public WorldControlCommands(Basics basics)
    {
        this.config = basics.getConfiguration();
    }

    @Command(desc = "Changes the weather", min = 1, max = 3, usage = "<sun|rain|storm> [duration] [in <world>]", params = @Param(names = "in", type = World.class))
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

    @Command(desc = "Removes entity", usage = "<entityType[:itemMaterial]> [radius] [in <world>] [-a]", flags = {
        @Flag(longName = "all", name = "a")
    }, params = @Param(names = {
        "in"
    }, type = World.class), min = 1)
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
                invalidUsage(context, "basics", "&cThe butcher will come to YOU tonight!");
            }
            world = sender.getWorld();
        }
        int radius = this.config.removeCmdDefaultRadius;
        if (context.hasFlag("a")) // remove all selected entities in world
        {
            radius = -1;
        }
        else if (sender == null)
        {
            invalidUsage(context, "basics", "&cIf not used ingame you can only remove all!");
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
        int entitiesRemoved = this.removeEntityType(world.getEntities(), loc, radius, type, itemtype, false);
        if (entitiesRemoved == 0)
        {
            context.sendMessage("basics", "&eNo entities to remove!");
        }
        else
        {
            context.sendMessage("basics", "&aRemoved &e%d &aentities!", entitiesRemoved);
        }
    }

    @Command(desc = "Gets rid of living animals nearby you", flags = {
        @Flag(longName = "pets", name = "p"),
        @Flag(longName = "golems", name = "g"),
        @Flag(longName = "animals", name = "a"),
        @Flag(longName = "npc", name = "n"),
        @Flag(longName = "other", name = "o"), //squids & bats
        @Flag(longName = "force", name = "f"), // all living entities (but not players)
        @Flag(longName = "lightning", name = "l"),
        @Flag(longName = "all", name = "all")
    }, params = @Param(names = {
        "choose", "c"
    }, type = String.class), usage = "[radius] [world] [choose|c <entityType>] [flags]")
    public void butcher(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        Location loc;
        int radius = this.config.butcherCmdDefaultRadius;
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
        if (context.hasIndexed(0))
        {
            radius = context.getIndexed(0, Integer.class, 0);
            if (radius < 0)
            {
                if (!(radius == -1 && BasicsPerm.COMMAND_BUTCHER_FLAG_ALL.isAuthorized(context.getSender())))
                {
                    illegalParameter(context, "basics", "&cThe radius has to be a number greater than 0!");
                }
            }
        }
        if (context.hasFlag("all") && BasicsPerm.COMMAND_BUTCHER_FLAG_ALL.isAuthorized(context.getSender()))
        {
            radius = -1;
        }
        boolean lightning = false;
        if (context.hasFlag("l") && BasicsPerm.COMMAND_BUTCHER_FLAG_LIGHTNING.isAuthorized(context.getSender()))
        {
            lightning = true;
        }
        List<Entity> list = new ArrayList<Entity>();
        for (Entity entity : loc.getWorld().getEntities())
        {
            if (entity instanceof LivingEntity && !(entity instanceof Player))
            {
                list.add(entity); // only living entities that are not players
            }
        }
        if (context.hasNamed("c"))
        {
            EntityType type = EntityMatcher.get().matchMob(context.getNamed("c", String.class));
            if (type == null)
            {
                blockCommand(context, "basics", "%cUnkown Mob-Type!");
            }
            removed = this.removeEntityType(list, loc, radius, type, null, lightning);
        }
        else if (context.hasFlag("f") && BasicsPerm.COMMAND_BUTCHER_FLAG_ALLTYPE.isAuthorized(context.getSender())) //remove all living
        {
            removed = this.removeEntityType(list, loc, radius, null, null, lightning);
        }
        else
        {
            List<Entity> filteredList = new ArrayList<Entity>();
            for (Entity entity : list)
            {
                if (entity instanceof Monster || entity instanceof Slime || entity instanceof Ghast || entity instanceof EnderDragon
                    || (context.hasFlag("p") && entity instanceof Tameable && ((Tameable)entity).isTamed() && BasicsPerm.COMMAND_BUTCHER_FLAG_PET.isAuthorized(context.getSender()))
                    || (context.hasFlag("g") && entity instanceof Golem && BasicsPerm.COMMAND_BUTCHER_FLAG_GOLEM.isAuthorized(context.getSender()))
                    || (context.hasFlag("n") && entity instanceof NPC && BasicsPerm.COMMAND_BUTCHER_FLAG_NPC.isAuthorized(context.getSender()))
                    || (context.hasFlag("a") && entity instanceof Animals && !(entity instanceof Tameable) && BasicsPerm.COMMAND_BUTCHER_FLAG_ANIMAL.isAuthorized(context.getSender()))
                    || (context.hasFlag("o") && BasicsPerm.COMMAND_BUTCHER_FLAG_OTHER.isAuthorized(context.getSender()) && (entity instanceof Ambient || entity instanceof WaterMob)))

                {
                    filteredList.add(entity);
                }
            }
            removed = this.removeEntityType(filteredList, loc, radius, null, null, lightning);
        }

        context.sendMessage("basics", removed == 0 ? "&eNothing to butcher!" : "&aYou just slaughtered &e%d &aliving entities!", removed);
    }

    private int removeEntityType(List<Entity> list, Location loc, int radius, EntityType type, Material itemtype, boolean lightning)
    {
        if (loc == null && radius != -1)
        {
            throw new IllegalStateException("Unknown Location with radius");
        }
        int removed = 0;

        for (Entity entity : list)
        {
            if (type != null && !entity.getType().equals(type.getBukkitType()))
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
            if (lightning)
            {
                entity.getWorld().strikeLightningEffect(entity.getLocation());
            }
            entity.remove();
            removed++;
        }
        return removed;
    }
}
