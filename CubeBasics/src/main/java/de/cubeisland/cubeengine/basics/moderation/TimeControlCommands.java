
package de.cubeisland.cubeengine.basics.moderation;

import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import java.util.HashMap;
import java.util.List;
import org.bukkit.World;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
/**
 * Commands changing time.
 * /time
 * /ptime
 */
public class TimeControlCommands
{
    private enum Time
    {
        DAY(6000, "day", "noon"),
        NIGHT(18000, "night", "midnight"),
        DAWN(0, "dawn", "morning"),
        DUSK(12000, "dusk", "even");
        private static final HashMap<String, Time> times = new HashMap<String, Time>();
        private static final HashMap<Long, String> timeNames = new HashMap<Long, String>();
        protected String[] names;
        protected long longTime;

        static
        {
            for (Time time : values())
            {
                for (String name : time.names)
                {
                    times.put(name, time);
                }
                timeNames.put(time.longTime, time.names[0]);
            }
        }

        private Time(long longTime, String... names)
        {
            this.names = names;
            this.longTime = longTime;
        }

        public static String getTimeName(Long time)
        {
            return timeNames.get(time);
        }

        public static Long matchTime(String s)
        {
            if (s == null)
            {
                return null;
            }
            Time time = times.get(s);
            if (time != null)
            {
                return time.longTime;
            }
            try //TODO time as 12:00 4pm/am etc.
            {
                return Long.parseLong(s); // this is time in ticks
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }

    @Command(
        desc = "Changes the time of a world",
        min = 1, max = 2,
        flags = { @Flag(name = "a", longName = "all") },
        usage = "<day|night|dawn|even|<time>> [world] [-all]")
    public void time(CommandContext context)
    {
        //TODO change output time set to %d to day|night etc..
        String timeString = context.getIndexed(0, String.class, null);
        Long time = Time.matchTime(timeString);
        if (time == null)
        {
            illegalParameter(context, "basics", "Invalid Time format!");
        }
        if (context.hasFlag("a"))
        {
            for (World world : context.getSender().getServer().getWorlds())
            {
                world.setTime(time);
            }
            String timeName = Time.getTimeName(time);
            if (timeName == null)
            {
                context.sendMessage("basics", "Time set to %d in all worlds", time);
            }
            else
            {
                context.sendMessage("basics", "Time set to %s in all worlds", timeName);
            }
        }
        else
        {
            User sender = context.getSenderAsUser();
            World world = null;
            if (context.hasIndexed(1))
            {
                String worldname = context.getIndexed(1, String.class, "");
                world = context.getSender().getServer().getWorld(worldname);
                if (world == null)
                {
                    List<World> worlds = context.getSender().getServer().getWorlds();
                    StringBuilder sb = new StringBuilder();
                    for (World w : worlds)
                    {
                        sb.append(" ").append(w.getName());
                    }
                    paramNotFound(context, "basics", "&cThe World %s does not exist!"
                        + "\nUse one of those:%s", context. getString(1), sb.toString());
                }
            }
            else if (sender == null)
            {
                invalidUsage(context, "basics", "If not used by a player you have to specify a world!");
            }
            if (world == null)
            {
                world = sender.getWorld();
            }
            world.setTime(time);
            String timeName = Time.getTimeName(time);
            if (timeName == null)
            {
                context.
                    sendMessage("basics", "Time set to %d in world %s", time, world.
                    getName());
            }
            else
            {
                context.sendMessage("basics", "Time set to %s in world %s", timeName, world.getName());
            }
        }
    }
    
    @Command(
        desc = "Changes the time for a player",
        min = 1,
        max = 2,
        flags = { @Flag(longName = "relative", name = "rel") },
        usage = "<day|night|dawn|even> [player]")
    public void ptime(CommandContext context)
    {
        Long time = 0L;
        boolean other = false;
        boolean reset = false;
        String timeString = context.getIndexed(0, String.class, null);
        if (timeString.equalsIgnoreCase("reset"))
        {
            reset = true;
        }
        else
        {
            time = Time.matchTime(timeString);
            if (time == null)
            {
                invalidUsage(context, "basics", "Invalid Time format!");
            }
        }
        User sender = context.getSenderAsUser();
        User user = sender;
        if (context.hasIndexed(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                invalidUsage(context, "core", "User not found!");
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
            context.sendMessage("basics", "Resetted the time for %s!", user.getName());
            if (other)
            {
                user.sendMessage("basics", "Your time was resetted!");
            }
        }
        else
        {
            if (context.hasFlag("rel"))
            {
                user.setPlayerTime(time, false);
            }
            else
            {
                user.setPlayerTime(user.getWorld().getTime() - time, true);
            }
            String timeName = Time.getTimeName(time);
            if (timeName == null)
            {
                context.sendMessage("basics", "Time set to %d for %s", time, user.getName());
            }
            else
            {
                context.sendMessage("basics", "Time set to %s for %s", timeName, user.getName());
            }
            if (other)
            {
                if (timeName == null)
                {
                    user.sendMessage("basics", "Your time was set to %d!", time);
                }
                else
                {
                    user.
                        sendMessage("basics", "Your time was set to %s!", timeName);
                }
            }
        }
    }
}