package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.EntityType;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import de.cubeisland.cubeengine.core.util.math.MathHelper;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.i18n.I18n._;

public class InformationCommands
{
    private Basics basics;

    public InformationCommands(Basics basics)
    {
        this.basics = basics;
    }

    @Command(
        desc = "Displays the direction in which you are looking.")
    public void compass(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&6ProTip: &eI assume you are looking right at your screen. Right?");
        int direction = (int)(sender.getLocation().getYaw() + 180 + 360) % 360;
        String dir;
        if (direction < 23)
        {
            dir = "N";
        }
        else if (direction < 68)
        {
            dir = "NE";
        }
        else if (direction < 113)
        {
            dir = "E";
        }
        else if (direction < 158)
        {
            dir = "SE";
        }
        else if (direction < 203)
        {
            dir = "S";
        }
        else if (direction < 248)
        {
            dir = "SW";
        }
        else if (direction < 293)
        {
            dir = "W";
        }
        else if (direction < 338)
        {
            dir = "NW";
        }
        else
        {
            dir = "N";
        }
        sender.sendMessage("basics", "&eYou are looking to &6%s&e!", _(sender, "basics", dir));
    }

    @Command(
        desc = "Displays your current depth.")
    public void depth(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&cYou dug too deep!");
        int height = sender.getLocation().getBlockY();
        if (height > 62)
        {
            sender.sendMessage("basics", "You are on heightlevel %d (%d above sealevel)", height, height - 62);
        }
        else
        {
            sender.sendMessage("basics", "You are on heightlevel %d (%d below sealevel)", height, 62 - height);
        }
    }

    @Command(
        desc = "Displays your current location.")
    public void getPos(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eYour position: &cRight in front of your screen!");
        sender.sendMessage("basics", "&eYour position is &6X:&f%d &6Y:&f%d &6Z:&f%d", sender.getLocation().getBlockX(), sender.getLocation().getBlockY(), sender.getLocation().getBlockZ());
    }

    @Command(
        desc = "Displays the message of the day!")
    public void motd(CommandContext context)
    {
        context.sendMessage(basics.getConfiguration().motd);//TODO translatable other lang in config else default
        /*
         * default: 'Welcome on our Server! Have fun!'
         * de_DE: 'Willkommen auf unserem Server! Viel Spaß'
         */
    }

    @Command(
        desc = "Displays near players(entities/mobs) to you.",
        max = 2,
        usage = "[radius] [player] [-entity]|[-mob]",
        flags =
        {
            @Flag(longName = "entity", name = "e"),
            @Flag(longName = "mob", name = "m")
        })
    public void near(CommandContext context)
    {
        User user;
        if (context.hasIndexed(1))
        {
            user = context.getUser(1);
        }
        else
        {
            user = context.getSenderAsUser("basics", "&eI'am right &cbehind &eyou!");
        }
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        int radius = this.basics.getConfiguration().nearDefaultRadius;
        if (context.hasIndexed(0))
        {
            radius = context.getIndexed(0, int.class, radius);
        }
        int squareRadius = radius * radius;
        List<Entity> list = user.getWorld().getEntities();
        LinkedList<String> outputlist = new LinkedList<String>();
        TreeMap<Double, List<Entity>> sortedMap = new TreeMap<Double, List<Entity>>();
        //TODO if list contains too many objects show nearest normally then
        //e.g.:  100x Zombie (250m+)
        //TODO only show the flag is there for
        for (Entity entity : list)
        {
            double distance = entity.getLocation().distanceSquared(user.getLocation());
            if (!entity.getLocation().equals(user.getLocation()))
            {
                if (distance < squareRadius)
                {
                    if (context.hasFlag("e") || (context.hasFlag("m") && entity instanceof LivingEntity) || entity instanceof Player)
                    {
                        List<Entity> sublist = sortedMap.get(distance);
                        if (sublist == null)
                        {
                            sublist = new ArrayList<Entity>();
                        }
                        sublist.add(entity);
                        sortedMap.put(distance, sublist);
                    }
                }
            }
        }
        int i = 0;
        LinkedHashMap<String, Pair<Double, Integer>> groupedEntities = new LinkedHashMap<String, Pair<Double, Integer>>();
        for (double dist : sortedMap.keySet())
        {
            i++;
            for (Entity entity : sortedMap.get(dist))
            {
                if (i <= 10)
                {
                    this.addNearInformation(outputlist, entity, Math.sqrt(dist));
                }
                else
                {
                    String key;
                    if (entity instanceof Player)
                    {
                        key = "&2player";
                    }
                    else if (entity instanceof LivingEntity)
                    {
                        key = "&3" + EntityType.fromBukkitType(entity.getType()).toString();
                    }
                    else if (entity instanceof Item)
                    {
                        key = "&7" + MaterialMatcher.get().getNameFor(((Item)entity).getItemStack());
                    }
                    else
                    {
                        key = "&7" + EntityType.fromBukkitType(entity.getType()).toString();
                    }
                    Pair<Double, Integer> pair = groupedEntities.get(key);
                    if (pair == null)
                    {
                        pair = new Pair<Double, Integer>(Math.sqrt(dist), 1);
                        groupedEntities.put(key, pair);
                    }
                    else
                    {
                        pair.y++;
                    }
                }
            }
        }
        StringBuilder groupedOutput = new StringBuilder();
        for (String key : groupedEntities.keySet())
        {
            groupedOutput.append(String.format("\n&6%dx %s &f(&e%dm+&f)", groupedEntities.get(key).y, key, MathHelper.round(groupedEntities.get(key).x)));
        }
        if (outputlist.isEmpty())
        {
            context.sendMessage("Nothing detected nearby!");
        }
        else
        {
            String result;
            result = StringUtils.implode("&f, ", outputlist);
            result += groupedOutput.toString();
            if (context.getSender().getName().equals(user.getName()))
            {
                context.sendMessage("basics", "&eFound those nearby you:\n%s", result);
            }
            else
            {
                context.sendMessage("basics", "&eFound those nearby %s:\n%s", user.getName(), StringUtils.implode("&f, ", outputlist));
            }
        }
    }

    private void addNearInformation(List<String> list, Entity entity, double distance)
    {
        if (entity instanceof Player)
        {
            list.add(String.format("&2%s&f (&e%dm&f)", ((Player)entity).getName(), (int)distance));
        }
        else if (entity instanceof LivingEntity)
        {
            list.add(String.format("&3%s&f (&e%dm&f)", EntityType.fromBukkitType(entity.getType()), (int)distance));
        }
        else
        {
            if (entity instanceof Item)
            {
                list.add(String.format("&7%s&f (&e%dm&f)", MaterialMatcher.get().getNameFor(((Item)entity).getItemStack()), (int)distance));
            }
            else
            {
                list.add(String.format("&7%s&f (&e%dm&f)", EntityType.fromBukkitType(entity.getType()), (int)distance));
            }
        }
    }

    @Command(
        names =
    {
        "ping", "pong"
    },
    desc = "Pong!",
    max = 0)
    public void ping(CommandContext context)
    {
        if (context.getLabel().equalsIgnoreCase("ping"))
        {
            context.sendMessage("basics", "&6Pong!");
        }
        else
        {
            if (context.getLabel().equalsIgnoreCase("pong"))
            {
                context.sendMessage("basics", "&6Ping!");
            }
        }
    }

    @Command(
        desc = "Displays chunk, memory, and world information.",
    max = 0)
    public void lag(CommandContext context)
    {
        //uptime
        //tps
        //nutz/reserviert/max Memory

        //alle worlds mit gel. chunks und entity
        String uptime = new Date(ManagementFactory.getRuntimeMXBean().getStartTime()).toString();
        long memUse = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1048576;
        long memCom = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted() / 1048576;
        long memMax = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() / 1048576;
        String memused;
        String memcommited = "&e" + memCom;
        String memmax = "&e" + memMax;
        long memUsePercent = 100 * memUse / memMax;
        if (memUsePercent > 90)
        {
            if (memUsePercent > 95)
            {
                memused = "&4";
            }
            else
            {
                memused = "&c";
            }
        }
        else if (memUsePercent > 60)
        {
            memused = "&e";
        }
        else
        {
            memused = "&a";
        }
        memused += memUse;
        context.sendMessage("basics", "&6Uptime: &a%s\n&6Memory Usage: %s&f/%s&f/%s MB", uptime, memused, memcommited, memmax);
    }
}