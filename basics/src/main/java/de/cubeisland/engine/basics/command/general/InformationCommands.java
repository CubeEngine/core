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
package de.cubeisland.engine.basics.command.general;

import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.Direction;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.core.util.math.BlockVector2;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.util.math.MathHelper;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class InformationCommands
{
    private final PeriodFormatter formatter;
    private final Basics module;

    public InformationCommands(Basics module)
    {
        this.module = module;
        this.formatter = new PeriodFormatterBuilder().appendWeeks().appendSuffix(" week"," weeks").appendSeparator(" ")
                                                     .appendDays().appendSuffix(" day", " days").appendSeparator(" ")
                                                     .appendHours().appendSuffix(" hour"," hours").appendSeparator(" ")
                                                     .appendMinutes().appendSuffix(" minute", " minutes").appendSeparator(" ")
                                                     .appendSeconds().appendSuffix(" second", " seconds").toFormatter();
    }

    @Command(desc = "Displays the biome type you are standing in.",
             usage = "(world) (block-x) (block-z)", max = 3)
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
                context.sendTranslated(MessageType.NEGATIVE, "Unknown world {input#world}!", context.getString(0));
                return;
            }
            x = context.getArg(1,Integer.class,null);
            z = context.getArg(2,Integer.class,null);
            if (x == null || z == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Please provide valid whole number x and/or z coordinates!");
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
            context.sendTranslated(MessageType.NEGATIVE, "Please provide a world and x and z coordinates!");
            return;
        }
        Biome biome = world.getBiome(x, z);
        context.sendTranslated(MessageType.NEUTRAL, "Biome at {vector:x\\=:z\\=}: {biome}", new BlockVector2(x, z), biome);
    }

    @Command(desc = "Displays the seed of a world.", usage = "(world)", max = 1)
    public void seed(CommandContext context)
    {
        World world = null;
        if (context.hasArg(0))
        {
            world = context.getArg(0, World.class, null);
            if (world == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "World {input#world} not found!", context.getString(0));
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
                context.sendTranslated(MessageType.NEGATIVE, "No world specified!");
                return;
            }
        }
        context.sendTranslated(MessageType.NEUTRAL, "Seed of {world} is {long#seed}", world, world.getSeed());
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
            sender.sendTranslated(MessageType.NEUTRAL, "You are looking to {input#direction}!", dir); // TODO translate direction
        }
        else
        {
            context.sendTranslated(MessageType.NEUTRAL, "{text:ProTip}: I assume you are looking right at your screen, right?");
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
                context.sendTranslated(MessageType.POSITIVE, "You are on heightlevel {integer#blocks} ({amount#blocks} above sealevel)", height, height - 62);
            }
            else
            {
                context.sendTranslated(MessageType.POSITIVE, "You are on heightlevel {integer#blocks} ({amount#blocks} below sealevel)", height, 62 - height);
            }
        }
        else
        {
            context.sendTranslated(MessageType.NEGATIVE, "You dug too deep!");
        }
    }

    @Command(desc = "Displays your current location.")
    public void getPos(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            final Location loc = ((User)context.getSender()).getLocation();
            context.sendTranslated(MessageType.NEUTRAL, "Your position is {vector:x\\=:y\\=:z\\=}", new BlockVector3(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }
        else
        {
            context.sendTranslated(MessageType.NEUTRAL, "Your position: {text:Right in front of your screen!:color=RED}");
        }
    }

    @Command(desc = "Displays near players(entities/mobs) to you.",
             max = 2, usage = "[radius] [player] [-entity]|[-mob]",
             flags = {@Flag(longName = "entity", name = "e"),
                      @Flag(longName = "mob", name = "m")})
    public void near(ParameterizedContext context)
    {
        User user;
        if (context.hasArg(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", context.getString(1));
                return;
            }
        }
        else if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        else
        {
            context.sendTranslated(MessageType.NEUTRAL, "I am right {text:behind:color=RED} you!");
            return;
        }
        int radius = this.module.getConfiguration().commands.nearDefaultRadius;
        if (context.hasArg(0))
        {
            radius = context.getArg(0, Integer.class, radius);
        }
        int squareRadius = radius * radius;
        Location userLocation = user.getLocation();
        List<Entity> list = userLocation.getWorld().getEntities();
        LinkedList<String> outputlist = new LinkedList<>();
        TreeMap<Double, List<Entity>> sortedMap = new TreeMap<>();
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
                            sublist = new ArrayList<>();
                        }
                        sublist.add(entity);
                        sortedMap.put(distance, sublist);
                    }
                }
            }
        }
        int i = 0;
        LinkedHashMap<String, Pair<Double, Integer>> groupedEntities = new LinkedHashMap<>();
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
                        key = ChatFormat.DARK_GREEN + "player";
                    }
                    else if (entity instanceof LivingEntity)
                    {
                        key = ChatFormat.DARK_AQUA + Match.entity().getNameFor(entity.getType());
                    }
                    else if (entity instanceof Item)
                    {
                        key = ChatFormat.GREY + Match.material().getNameFor(((Item)entity).getItemStack());
                    }
                    else
                    {
                        key = ChatFormat.GREY + Match.entity().getNameFor(entity.getType());
                    }
                    Pair<Double, Integer> pair = groupedEntities.get(key);
                    if (pair == null)
                    {
                        pair = new Pair<>(Math.sqrt(dist), 1);
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
            groupedOutput.append("\n").append(ChatFormat.GOLD).append(groupedEntities.get(key).getRight()).append("x ")
                         .append(key).append(ChatFormat.WHITE).append(" (").append(ChatFormat.GOLD)
                         .append(MathHelper.round(groupedEntities.get(key).getLeft())).append("m")
                         .append(ChatFormat.WHITE).append(")");
        }
        if (outputlist.isEmpty())
        {
            context.sendTranslated(MessageType.NEGATIVE, "Nothing detected nearby!");
        }
        else
        {
            String result;
            result = StringUtils.implode(ChatFormat.WHITE + ", ", outputlist);
            result += groupedOutput.toString();
            if (context.getSender().getName().equals(user.getName()))
            {
                context.sendTranslated(MessageType.NEUTRAL, "Found those nearby you:\n{}", result);
            }
            else
            {
                context.sendTranslated(MessageType.NEUTRAL, "Found those nearby {user}:\n{}", user, result);
            }
        }
    }

    private void addNearInformation(List<String> list, Entity entity, double distance)
    {
        String s;
        if (entity instanceof Player)
        {
            s = ChatFormat.DARK_GREEN + ((Player)entity).getName();
        }
        else if (entity instanceof LivingEntity)
        {
            s = ChatFormat.DARK_AQUA + Match.entity().getNameFor(entity.getType());
        }
        else
        {
            if (entity instanceof Item)
            {
                s = ChatFormat.GREY + Match.material().getNameFor(((Item)entity).getItemStack());
            }
            else
            {
                s = ChatFormat.GREY + Match.entity().getNameFor(entity.getType());
            }
        }
        s += ChatFormat.WHITE + " (" + ChatFormat.GOLD + distance + "m" + ChatFormat.WHITE + ")";
        list.add(s);
    }

    @Command(names = {"ping", "pong"}, desc = "Pong!", max = 0)
    public void ping(CommandContext context)
    {
        final String label = context.getLabel().toLowerCase(Locale.ENGLISH);
        if (context.getSender() instanceof ConsoleCommandSender)
        {
            context.sendTranslated(MessageType.NEUTRAL, label + " in the console?");
        }
        else
        {
            context.sendTranslated(MessageType.NONE, ("ping".equals(label) ? "pong" : "ping") + "! Your latency: {integer#ping}", ((User)context.getSender()).getPing());
        }
    }

    @Command(desc = "Displays chunk, memory and world information.",
             max = 0, flags = @Flag(longName = "reset" , name = "r"))
    public void lag(ParameterizedContext context)
    {
        if (context.hasFlag("r"))
        {
            if (module.perms().COMMAND_LAG_RESET.isAuthorized(context.getSender()))
            {
                this.module.getLagTimer().resetLowestTPS();
                context.sendTranslated(MessageType.POSITIVE, "Reset lowest TPS!");
            }
            else
            {
                context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to do this!");
            }
            return;
        }
        //Uptime:
        context.sendTranslated(MessageType.POSITIVE, "[{text:CubeEngine-Basics:color=RED}]");
        DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT,
                     context.getSender().getLocale());
        Date start = new Date(ManagementFactory.getRuntimeMXBean().getStartTime());
        Duration dura = new Duration(start.getTime(), System.currentTimeMillis());
        context.sendTranslated(MessageType.POSITIVE, "Server has been running since {input#uptime}", df.format(start));
        context.sendTranslated(MessageType.POSITIVE, "Uptime: {input#uptime}", formatter.print(dura.toPeriod()));
        //TPS:
        float tps = this.module.getLagTimer().getAverageTPS();
        String color = tps == 20 ? ChatFormat.DARK_GREEN.toString() : tps > 17 ? ChatFormat.YELLOW.toString() : tps > 10 ? ChatFormat.RED.toString() : tps == 0 ? (ChatFormat.YELLOW.toString() + "NaN") : ChatFormat.DARK_RED.toString();
        context.sendTranslated(MessageType.POSITIVE, "Current TPS: {input#color}{decimal#tps:1}", color, tps);
        Pair<Long, Float> lowestTPS = this.module.getLagTimer().getLowestTPS();
        if (lowestTPS.getRight() != 20)
        {
            color = ChatFormat.parseFormats(tps > 17 ? ChatFormat.YELLOW.toString() : tps > 10 ? ChatFormat.RED.toString() : ChatFormat.DARK_RED.toString());
            Date date = new Date(lowestTPS.getLeft());
            context.sendTranslated(MessageType.POSITIVE, "Lowest TPS was {}{decimal#tps:1} ({input#date})", color, lowestTPS.getRight(), df.format(date));
            long timeSinceLastLowTPS = System.currentTimeMillis() - this.module.getLagTimer().getLastLowTPS();
            if (tps == 20 && TimeUnit.MINUTES.convert(timeSinceLastLowTPS,TimeUnit.MILLISECONDS) < 1)
            {
                context.sendTranslated(MessageType.NEGATIVE, "TPS was low in the last minute!");
            }
        }
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
                memused = ChatFormat.DARK_RED.toString();
            }
            else
            {
                memused = ChatFormat.RED.toString();
            }
        }
        else if (memUsePercent > 60)
        {
            memused = ChatFormat.YELLOW.toString();
        }
        else
        {
            memused = ChatFormat.DARK_GREEN.toString();
        }
        memused += memUse;
        context.sendTranslated(MessageType.POSITIVE, "Memory Usage: {input#memused}/{integer#memcom}/{integer#memMax} MB", memused, memCom, memMax);
        //Worlds with loaded Chunks / Entities
        for (World world : Bukkit.getServer().getWorlds())
        {
            String type = world.getEnvironment().name();
            int loadedChunks = world.getLoadedChunks().length;
            int entities = world.getEntities().size();
            context.sendTranslated(MessageType.POSITIVE, "{world} ({input#environment}): {amount} chunks {amount} entities", world, type, loadedChunks, entities);
        }
    }


    @Command(desc = "Displays all loaded worlds", names = {"listWorlds","worldlist","worlds"})
    public void listWorlds(CommandContext context)
    {
        context.sendTranslated(MessageType.POSITIVE, "Loaded worlds:");
        String format = " " + ChatFormat.WHITE + "- " + ChatFormat.GOLD + "%s" + ChatFormat.WHITE + ":" + ChatFormat.INDIGO + "%s";
        for (World world : Bukkit.getServer().getWorlds())
        {
            context.sendMessage(String.format(format, world.getName(), world.getEnvironment().name()));
        }
    }
}
