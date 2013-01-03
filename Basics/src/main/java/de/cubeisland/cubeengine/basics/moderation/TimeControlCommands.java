package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

/**
 * Commands changing time.
 * /time
 * /ptime
 */
public class TimeControlCommands
{

    private final Basics basics;
    private final TaskManager taskmgr;
    private final LockTask lockTask;

    public TimeControlCommands(Basics basics)
    {
        this.basics = basics;
        this.taskmgr = basics.getTaskManger();
        this.lockTask = new LockTask();
    }

    private enum Time
    {
        // TODO what about a matcher + alias file?
        DAY(
            6000,
            "day",
            "noon"),
        NIGHT(
            18000,
            "night",
            "midnight"),
        DAWN(
            22500,
            "dawn"),
        SUNRISE(
            0,
            "sunrise",
            "morning"),
        DUSK(
            13000,
            "dusk",
            "moonrise"),
        EVEN(
            15000,
            "even",
            "evening",
            "sunset"),
        FORENOON(
            3000,
            "forenoon"),
        AFTERNOON(
            9000,
            "afternoon");

        public static final int TICKS_PER_HOUR = 1000;
        public static final int TICKS_PER_DAY = 24 * TICKS_PER_HOUR;
        public static final int HALF_DAY = TICKS_PER_DAY / 2;
        public static final int LIGHT_SHIFT = HALF_DAY / 2;
        public static final double TICKS_TO_MINUTES = (double)TICKS_PER_DAY / 1440D;

        private static final Pattern PARSE_TIME_PATTERN = Pattern.compile("^([012]?\\d)(?::(\\d{2}))?(pm|am)?$", Pattern.CASE_INSENSITIVE);
        private static final THashMap<String, Time> times = new THashMap<String, Time>(values().length);
        private static final TLongObjectHashMap<String> timeNames = new TLongObjectHashMap<String>();
        private final String[] names;
        private final long time;

        static
        {
            for (Time time : values())
            {
                for (String name : time.names)
                {
                    times.put(name, time);
                }
                timeNames.put(time.time, time.names[0]);
            }
        }

        private Time(long time, String... names)
        {
            this.names = names;
            this.time = time;
        }

        public static String getTimeName(long time)
        {
            return timeNames.get(time);
        }

        public static long matchTime(String string)
        {
            if (string == null)
            {
                return -1;
            }

            // is it a named time?
            Time time = times.get(string);
            if (time != null)
            {
                return time.time;
            }
            try
            {
                Matcher matcher = PARSE_TIME_PATTERN.matcher(string);
                // is it a formatted time?
                if (matcher.find())
                {
                    // no null-check: group 1 is always available if matched
                    String part = matcher.group(1);
                    // remove leading zeros to prevent the number from being interpreted as an octal number
                    if (part.charAt(0) == '0')
                    {
                        part = part.substring(1);
                    }
                    int hours = Integer.parseInt(part);
                    int minutes = 0;
                    // group 2: minutes; option
                    part = matcher.group(2);
                    if (part != null)
                    {
                        if (part.charAt(0) == '0')
                        {
                            part = part.substring(1);
                        }
                        minutes = Integer.parseInt(part);
                    }
                    // if more than 60 minutes, hours will be added instead to keep the minutes below 60
                    hours += minutes / 60;
                    minutes %= 60;
                    // keep the hours within 24 hours
                    hours = (hours * TICKS_PER_HOUR) % TICKS_PER_DAY;

                    // group 3: am/pm; optional
                    part = matcher.group(3);
                    if (part != null)
                    {
                        // keep the ours within 12
                        if (hours > HALF_DAY)
                        {
                            hours %= HALF_DAY;
                        }
                        // if pm had half a day
                        if (part.equalsIgnoreCase("pm"))
                        {
                            hours += HALF_DAY;
                        }
                    }
                    // shift the time for 6 hours to match MC's sun light
                    // subtracts 6 to shift the time, add 24 and modulo24 to keep it between 0 and 24
                    hours = (hours - LIGHT_SHIFT + TICKS_PER_DAY) % TICKS_PER_DAY;

                    // calculate the ticks
                    return hours + Math.round(TICKS_TO_MINUTES * (double)minutes);
                }

                long daytime = Long.parseLong(string);
                if (daytime == 0)
                {
                    return 0;
                }
                // if the time is below 24 is probably meant to be interpreted as hours
                if (daytime < 24)
                {
                    daytime *= 1000;
                }
                return daytime;
            }
            catch (NumberFormatException ignored)
            {}
            return -1;
        }

        public static String format(long time)
        {
            // lookup the name, I use a integer-division here to round
            String formatted = Time.getTimeName((time / TICKS_PER_HOUR) * TICKS_PER_HOUR);
            if (formatted == null)
            {
                // shift the time back to show a matching to the light
                time = (time + LIGHT_SHIFT) % TICKS_PER_DAY;
                int hours = (int)(time / TICKS_PER_HOUR);
                int minutes = (int)Math.round((double)(time % TICKS_PER_HOUR) / TICKS_TO_MINUTES);

                formatted = StringUtils.padLeft("" + hours, '0', 2) + ":" + StringUtils.padRight("" + minutes, '0', 2);
            }
            return formatted;
        }
    }

    @Command(desc = "Changes the time of a world", max = -1, usage = "<time> [world] [world] ...")
    public void time(CommandContext context)
    {
        User sender = context.getSenderAsUser();

        if (sender != null)
        {
            if (!context.hasIndexed(0))
            {
                context.sendMessage("basics", "&aIt's currently &e%s&a in this world.", Time.format(sender.getWorld().getTime()));
            }
            else
            {
                String timeString = context.getString(0);
                final long time;
                if ("lock".equalsIgnoreCase(timeString))
                {
                    time = -2;
                }
                else if ("unlock".equalsIgnoreCase(timeString))
                {
                    time = -3;
                }
                else
                {
                    time = Time.matchTime(timeString);
                }
                if (time == -1)
                {
                    World world = Bukkit.getWorld(context.getString(0));
                    if (world != null)
                    {
                        context.sendMessage("basics", "&aIt's currently &e%s&a in this world.", Time.format(world.getTime()));
                    }
                    else
                    {
                        context.sendMessage("basics", "&cThe time you entered is not valid!");
                    }
                    return;
                }
                if (context.hasIndexed(1))
                {
                    List<World> worlds;
                    String worldName = context.getString(1);
                    if ("*".equals(worldName))
                    {
                        worlds = Bukkit.getWorlds();
                    }
                    else
                    {
                        worlds = new ArrayList<World>();
                        int i = 1;
                        World world;
                        do
                        {
                            world = Bukkit.getWorld(worldName);
                            if (world != null)
                            {
                                worlds.add(world);
                            }
                        }
                        while ((worldName = context.getString(++i)) != null);
                    }

                    if (worlds.isEmpty())
                    {
                        context.sendMessage("basics", "&cNone of the specified worlds were found!");
                        return;
                    }
                    for (World world : worlds)
                    {
                        this.setTime(world, time);
                    }

                    if (worlds.size() == 1)
                    {
                        context.sendMessage("basics", "&aThe time of &e%s&a has been set to &e%s&a!", worlds.get(0).getName(), Time.format(time));
                    }
                    else
                    {
                        context.sendMessage("basics", "&aThe time of &e%s world(s)&a has been set to &e%s&a!", worlds.size(), Time.format(time));
                    }
                }
                else
                {
                    this.setTime(sender.getWorld(), time);
                }
            }
        }
        else
        {
            if (context.indexedCount() == 0)
            {
                context.sendMessage("basics", "&cYou have to specify a world when using this command from the console!");
            }
            else if (context.indexedCount() == 1)
            {
                World world = Bukkit.getWorld(context.getString(0));
                if (world == null)
                {
                    context.sendMessage("basics", "&cThe specified world wasn't found!");
                    return;
                }
                context.sendMessage("basics", "&aIt's currently &e%s&a in this world.", Time.format(world.getTime()));
            }
            else
            {
                final long time = Time.matchTime(context.getString(0));
                if (time == -1)
                {
                    context.sendMessage("basics", "&cThe time you entered is not valid!");
                    return;
                }

                List<World> worlds;
                String worldName = context.getString(1);
                if ("*".equals(worldName))
                {
                    worlds = Bukkit.getWorlds();
                }
                else
                {
                    worlds = new ArrayList<World>();
                    int i = 1;
                    World world;
                    do
                    {
                        world = Bukkit.getWorld(worldName);
                        if (world != null)
                        {
                            worlds.add(world);
                        }
                    }
                    while ((worldName = context.getString(++i)) != null);
                }

                if (worlds.isEmpty())
                {
                    context.sendMessage("basics", "&cNone of the specified worlds were found!");
                    return;
                }
                for (World world : worlds)
                {
                    this.setTime(world, time);
                }

                if (worlds.size() == 1)
                {
                    context.sendMessage("basics", "&aThe time of &e%s&a has been set to &e%s&a!", worlds.get(0).getName(), Time.format(time));
                }
                else
                {
                    context.sendMessage("basics", "&aThe time of &e%s world(s)&a has been set to &e%s&a!", worlds.size(), Time.format(time));
                }
            }
        }
    }

    @Command(desc = "Changes the time for a player", min = 1, max = 2, flags = {
        @Flag(longName = "relative", name = "rel")
    }, usage = "<<time>|reset> [player]")
    public void ptime(CommandContext context)
    {
        Long time = 0L;
        boolean other = false;
        boolean reset = false;
        String timeString = context.getString(0);
        if (timeString.equalsIgnoreCase("reset"))
        {
            reset = true;
        }
        else
        {
            time = Time.matchTime(timeString);
            if (time == null)
            {
                invalidUsage(context, "basics", "&cInvalid time-format!");
            }
        }

        User user = context.getSenderAsUser();
        if (context.hasIndexed(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                invalidUsage(context, "core", "&cUser %s not found!", context.getUser(0));
            }
            if (!BasicsPerm.COMMAND_PTIME_OTHER.
                isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "&cYou are not allowed to change the time of other players!");
            }
            other = true;
        }
        if (reset)
        {
            user.resetPlayerTime();
            context.sendMessage("basics", "&aReseted the time for &2%s&a!", user.getName());
            if (other)
            {
                user.sendMessage("basics", "&eYour time was reseted!");
            }
        }
        else
        {
            if (context.hasFlag("rel"))
            {
                user.resetPlayerTime();
                user.setPlayerTime(user.getWorld().getTime() - time, true);
            }
            else
            {
                user.resetPlayerTime();
                user.setPlayerTime(time, false);
            }
            String timeName = Time.getTimeName(time);
            if (timeName == null)
            {
                context.sendMessage("basics", "&aTime set to &e%d &afor &2%s&a!", time, user.getName());
            }
            else
            {
                context.sendMessage("basics", "&aTime set to &e%s &afor &2%s&a!", timeName, user.getName());
            }
            if (other)
            {
                if (timeName == null)
                {
                    user.sendMessage("basics", "&aYour time was set to &e%d!", time);
                }
                else
                {
                    user.sendMessage("basics", "&aYour time was set to &e%s!", timeName);
                }
            }
        }
    }

    private void setTime(World world, long time)
    {
        if (time == -2)
        {
            this.lockTask.add(world);
        }
        else if (time == -3)
        {
            this.lockTask.remove(world);
        }
        else
        {
            world.setTime(time);
        }
    }

    private final class LockTask implements Runnable
    {
        private final Map<String, Long> worlds = new HashMap<String, Long>();
        private int taskid = -1;

        public void add(World world)
        {
            this.worlds.put(world.getName(), world.getTime());
            if (this.taskid == -1)
            {
                this.taskid = taskmgr.scheduleSyncRepeatingTask(basics, this, 10, 10);
            }
        }

        public void remove(World world)
        {
            this.worlds.remove(world.getName());
            if (this.taskid != -1 && this.worlds.isEmpty())
            {
                taskmgr.cancelTask(basics, this.taskid);
                this.taskid = -1;
            }
        }

        @Override
        public void run()
        {
            Iterator<Map.Entry<String, Long>> iter = this.worlds.entrySet().iterator();

            Map.Entry<String, Long> entry;
            World world;
            while (iter.hasNext())
            {
                entry = iter.next();
                world = Bukkit.getWorld(entry.getKey());
                if (world != null)
                {
                    world.setTime(entry.getValue());
                }
                else
                {
                    iter.remove();
                }
            }
            if (this.taskid != -1 && this.worlds.isEmpty())
            {
                taskmgr.cancelTask(basics, this.taskid);
                this.taskid = -1;
            }
        }
    }
}
