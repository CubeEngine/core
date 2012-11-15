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
        private static final HashMap<String, Time> times     = new HashMap<String, Time>();
        private static final HashMap<Long, String> timeNames = new HashMap<Long, String>();
        protected String[]                         names;
        protected long                             longTime;

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
            try
            //TODO time as 12:00 4pm/am etc.
            {
                return Long.parseLong(s); // this is time in ticks
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }

    @Command(desc = "Changes the time of a world", min = 1, max = 2, flags = {
        @Flag(name = "a", longName = "all")
    }, usage = "<day|night|dawn|even|<time>> [world] [-all]")
    public void time(CommandContext context)
    {
        //TODO change output time set to %d to day|night etc..
        String timeString = context.getString(0);
        Long time = Time.matchTime(timeString);
        if (time == null)
        {
            illegalParameter(context, "basics", "&cInvalid time-format!");
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
                context.sendMessage("basics", "&aTime set to &e%d &ain all worlds!", time);
            }
            else
            {
                context.sendMessage("basics", "&aTime set to &e%s &ain all worlds!", timeName);
            }
        }
        else
        {
            User sender = context.getSenderAsUser();
            World world = null;
            if (context.hasIndexed(1))
            {
                String worldname = context.getString(1, "");
                world = context.getSender().getServer().getWorld(worldname);
                if (world == null)
                {
                    List<World> worlds = context.getSender().getServer().getWorlds();
                    StringBuilder sb = new StringBuilder();
                    for (World w : worlds)
                    {
                        sb.append("\n").append(w.getName());
                    }
                    paramNotFound(context, "basics", "&cThe World &6%s &cdoes not exist!"
                        + "\n&eUse one of those:&6%s", context.getString(1), sb.toString());
                }
            }
            else if (sender == null)
            {
                invalidUsage(context, "basics", "&cIf not used by a player you have to specify a world!"); //TODO funny message ?
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
                    sendMessage("basics", "&aTime set to &e%d &ain world &6%s&a!", time, world.
                        getName());
            }
            else
            {
                context.sendMessage("basics", "&aTime set to &e%s &ain world &6%s&a!", timeName, world.getName());
            }
        }
    }

    @Command(desc = "Changes the time for a player", min = 1, max = 2, flags = {
        @Flag(longName = "relative", name = "rel")
    }, usage = "<day|night|dawn|even|reset> [player]")
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
        User sender = context.getSenderAsUser();
        User user = sender;
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
}
