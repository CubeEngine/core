package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Direction;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.core.util.math.MathHelper;
import de.cubeisland.cubeengine.core.util.time.Duration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.util.*;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

public class InformationCommands
{
    private Basics basics;

    public InformationCommands(Basics basics)
    {
        this.basics = basics;
    }

    @Command(desc = "Displays the Biome-Type you are standing in.", usage = "{world} {block-x} {block-z]", max = 3)
    public void biome(CommandContext context)
    {
        World world;
        Integer x;
        Integer z;
        if (context.hasArg(2))
        {
            world = context.getArg(0,World.class,null);
            if (world == null)
            {
                context.sendMessage("basics","&cUnkown world %s!",context.getString(0));
                return;
            }
            x = context.getArg(1,Integer.class,null);
            z = context.getArg(2,Integer.class,null);
            if (x == null || z == null)
            {
                context.sendMessage("basics","&cPlease provide valid integer x and/or z coordinates!");
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            User user = (User)context.getSender();
            Location loc = user.getLocation();
            world = loc.getWorld();
            x = loc.getBlockX();
            z = loc.getBlockZ();
        }
        else
        {
            context.sendMessage("basics","&cPlease provide a world and x and z coordinates!");
            return;
        }
        Biome biome = world.getBiome(x, z);
        context.sendMessage("basics", "&eBiome at x=&6%d &ez=&6%d&e: &9%s", x,z,biome.name());
    }

    @Command(desc = "Displays the seed of a world.", usage = "{world}", max = 1)
    public void seed(CommandContext context)
    {
        World world = null;
        if (context.hasArg(0))
        {
            world = context.getArg(0, World.class, null);
            if (world == null)
            {
                context.sendMessage("basics", "&cWorld %s not found!",context.getString(0));
                return;
            }
        }
        if (world == null)
        {
            if (context.getSender() instanceof User)
            {
                world = ((User)context.getSender()).getWorld();
            }
            else
            {
                context.sendMessage("basics", "&cNo world specified!");
                return;
            }
        }
        context.sendMessage("basics", "&eSeed of &6%s&e is &6%d", world.getName(), world.getSeed());
    }

    @Command(desc = "Displays the direction in which you are looking.")
    public void compass(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            int direction = Math.round(((User)sender).getLocation().getYaw() + 180f + 360f) % 360;
            String dir;
            dir = Direction.matchDirection(direction).name();
            sender.sendMessage("basics", "&eYou are looking to &6%s&e!", _(sender, "basics", dir));
        }
        else
        {
            context.sendMessage("basics", "&6ProTip: &eI assume you are looking right at your screen. Right?");
        }
    }

    @Command(desc = "Displays your current depth.")
    public void depth(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            final int height = ((User)context.getSender()).getLocation().getBlockY();
            if (height > 62)
            {
                context.sendMessage("basics", "You are on heightlevel %d (%d above sealevel)", height, height - 62);
            }
            else
            {
                context.sendMessage("basics", "You are on heightlevel %d (%d below sealevel)", height, 62 - height);
            }
        }
        else
        {
            context.sendMessage("basics", "&cYou dug too deep!");
        }
    }

    @Command(desc = "Displays your current location.")
    public void getPos(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            final Location loc = ((User)context.getSender()).getLocation();
            context.sendMessage("basics", "&eYour position is &6X:&f%d &6Y:&f%d &6Z:&f%d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        else
        {
            context.sendMessage("basics", "&eYour position: &cRight in front of your screen!");
        }
    }

    @Command(desc = "Displays near players(entities/mobs) to you.", max = 2, usage = "[radius] [player] [-entity]|[-mob]", flags = {
        @Flag(longName = "entity", name = "e"),
        @Flag(longName = "mob", name = "m")
    })
    public void near(ParameterizedContext context)
    {
        User user;
        if (context.hasArg(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                context.sendMessage("basics", "&cUser &2%s &cnot found!",context.getString(1));
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        else
        {
            context.sendMessage("basics", "&eI am right &cbehind &eyou!");
            return;
        }
        int radius = this.basics.getConfiguration().nearDefaultRadius;
        if (context.hasArg(0))
        {
            radius = context.getArg(0, int.class, radius);
        }
        int squareRadius = radius * radius;
        Location userLocation = user.getLocation();
        List<Entity> list = userLocation.getWorld().getEntities();
        LinkedList<String> outputlist = new LinkedList<String>();
        TreeMap<Double, List<Entity>> sortedMap = new TreeMap<Double, List<Entity>>();
        final Location entityLocation = new Location(null, 0, 0, 0);
        for (Entity entity : list)
        {
            entity.getLocation(entityLocation);
            double distance = entityLocation.distanceSquared(userLocation);
            if (!entityLocation.equals(userLocation))
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
                        key = "&3" + Match.entity().getNameFor(entity.getType());
                    }
                    else if (entity instanceof Item)
                    {
                        key = "&7" + Match.material().getNameFor(((Item)entity).getItemStack());
                    }
                    else
                    {
                        key = "&7" + Match.entity().getNameFor(entity.getType());
                    }
                    Pair<Double, Integer> pair = groupedEntities.get(key);
                    if (pair == null)
                    {
                        pair = new Pair<Double, Integer>(Math.sqrt(dist), 1);
                        groupedEntities.put(key, pair);
                    }
                    else
                    {
                        pair.setRight(pair.getRight() + 1);
                    }
                }
            }
        }
        StringBuilder groupedOutput = new StringBuilder();
        for (String key : groupedEntities.keySet())
        {
            groupedOutput.append(String.format("\n&6%dx %s &f(&e%dm+&f)", groupedEntities.get(key).getRight(), key, MathHelper.round(groupedEntities.get(key).getLeft())));
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
            list.add(String.format("&3%s&f (&e%dm&f)", Match.entity().getNameFor(entity.getType())));
        }
        else
        {
            if (entity instanceof Item)
            {
                list.add(String.format("&7%s&f (&e%dm&f)", Match.material().getNameFor(((Item)entity).getItemStack()), (int)distance));
            }
            else
            {
                list.add(String.format("&7%s&f (&e%dm&f)", Match.entity().getNameFor(entity.getType())));
            }
        }
    }

    @Command(names = {
        "ping", "pong"
    }, desc = "Pong!", max = 0)
    public void ping(CommandContext context)
    {
        final String label = context.getLabel().toLowerCase(Locale.ENGLISH);
        if (context.getSender() instanceof ConsoleCommandSender)
        {
            context.sendMessage("basics", "&e" + label + " in the console?");
        }
        else
        {
            context.sendMessage("basics", ("ping".equals(label) ? "pong" : "ping") + "! Your latency: %s", ((User)context.getSender()).getPing());
        }
    }

    @Command(desc = "Displays chunk, memory, and world information.", max = 0)
    public void lag(CommandContext context)
    {
        //Uptime:
        Duration dura = new Duration(new Date(ManagementFactory.getRuntimeMXBean().getStartTime()).getTime(), System.currentTimeMillis());
        context.sendMessage("basics", "&6Uptime: &a%s", dura.format("%www %ddd %hhh %mmm %sss"));
        //TPS:
        float tps = this.basics.getLagTimer().getAverageTPS();
        String color = tps == 20 ? "&a" : tps > 17 ? "&e" : tps > 10 ? "&c" : "&4";
        color = ChatFormat.parseFormats(color);
        context.sendMessage("basics", "&6Current TPS: %s%.1f", color, tps);
        //Memory
        long memUse = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1048576;
        long memCom = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted() / 1048576;
        long memMax = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() / 1048576;
        String memused;
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
        memused = ChatFormat.parseFormats(memused);
        context.sendMessage("basics", "&6Memory Usage: %s&f/&e%d&f/&e%d MB", memused, memCom, memMax);
        //Worlds with loaded Chunks / Entities
        for (World world : Bukkit.getServer().getWorlds())
        {
            String type = world.getEnvironment().name();
            int loadedChunks = world.getLoadedChunks().length;
            int entities = world.getEntities().size();
            context.sendMessage("basics", "&6%s &e(&2%s&e)&6: &e%d &6chunks &e%d &6entities", world.getName(), type, loadedChunks, entities);
        }
    }


    @Command(desc = "Displays all loaded worlds", names = {"listWorlds","worldlist"})
    public void listWorlds(CommandContext context)
    {
        context.sendMessage("basics","&aLoaded worlds:");
        for (World world : Bukkit.getServer().getWorlds())
        {
            context.sendMessage(String.format(ChatFormat.parseFormats(" &f- &6%s&f: &9%s"),world.getName(),world.getEnvironment().name()));
        }
    }
}
