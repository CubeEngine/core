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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsPerm;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.task.TaskManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.matcher.Match;

/**
 * Commands changing time. /time /ptime
 */
public class TimeControlCommands
{

    private final Basics basics;
    private final TaskManager taskmgr;
    private final LockTask lockTask;

    public TimeControlCommands(Basics basics)
    {
        this.basics = basics;
        this.taskmgr = basics.getCore().getTaskManager();
        this.lockTask = new LockTask();
    }

    @Command(desc = "Changes the time of a world",
             flags = @Flag(longName = "lock", name = "l"),
             params = @Param(names = { "w", "worlds", "in"}),
             max = -1, usage = "<time> [w <worlds>]")
    public void time(ParameterizedContext context)
    {
        List<World> worlds;
        if (context.hasParam("w"))
        {
            if (context.getString("w").equals("*"))
            {
                worlds = Bukkit.getWorlds();
            }
            else
            {
                worlds = Match.worlds().matchWorlds(context.getString("w"));
                for (World world : worlds)
                {
                    if (world == null)
                    {
                        context.sendTranslated("&cCould not match all worlds! %s", context.getString("w"));
                        return;
                    }
                }
            }
        }
        else
        {
            if (context.getSender() instanceof User)
            {
                worlds = Arrays.asList(((User)context.getSender()).getWorld());
            }
            else
            {
                throw new IncorrectUsageException(context.getSender().translate("&cYou have to specify a world when using this command from the console!"));
            }
        }
        if (context.hasArg(0))
        {
            final Long time = Match.time().matchTimeValue(context.getString(0));
            if (time == null)
            {
                context.sendTranslated("&cThe time you entered is not valid!");
                return;
            }
            if (worlds.size() == 1)
            {
                context.sendTranslated("&aThe time of &e%s&a has been set to &6%s (%s)&a!",
                                       worlds.get(0).getName(),
                                       Match.time().format(time),
                                       Match.time().getNearTimeName(time));
            }
            else if ("*".equals(context.getString("w")))
            {
                context.sendTranslated("&aThe time of all worlds has been set to &6%s (%s)&a!",
                                       Match.time().format(time),
                                       Match.time().getNearTimeName(time));
            }
            else
            {
                context.sendTranslated("&aThe time of &6%s &aworlds have been set to &6%s (%s)&a!",
                                       worlds.size(),Match.time().format(time),
                                       Match.time().getNearTimeName(time));
            }
            for (World world : worlds)
            {
                this.setTime(world, time);
                if (context.hasFlag("l"))
                {
                    if (this.lockTask.worlds.containsKey(world.getName()))
                    {
                        this.lockTask.remove(world);
                        context.sendTranslated("&aTime unlocked for &6%s&a!", world.getName());
                    }
                    else
                    {
                        this.lockTask.add(world);
                        context.sendTranslated("&aTime locked for &6%s&a!", world.getName());
                    }
                }
            }
        }
        else
        {
            context.sendTranslated("&aThe current time is:");
            for (World world : worlds)
            {
                context.sendTranslated("&e%s (%s)&a in &6%s.",
                                       Match.time().format(world.getTime()),
                                       Match.time().getNearTimeName(world.getTime()),
                                       world.getName());
            }
        }
    }

    @Command(desc = "Changes the time for a player", min = 1, max = 2, flags = {
        @Flag(longName = "lock", name = "l")
    }, usage = "<<time>|reset> [player]")
    public void ptime(ParameterizedContext context)
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
            time = Match.time().matchTimeValue(timeString);
            if (time == null)
            {
                context.sendTranslated("&cInvalid time-format!");
                return;
            }
        }

        User user = null;
        if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        if (context.hasArg(1))
        {
            user = context.getUser(1);
            if (user == null)
            {
                context.sendTranslated("&cUser &2%s &cnot found!", context.getString(1));
                return;
            }
            if (!BasicsPerm.COMMAND_PTIME_OTHER.
                    isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to change the time of other players!");
                return;
            }
            other = true;
        }
        else if (user == null)
        {
            context.sendTranslated("&cYou need to define a player!");
            return;
        }
        if (reset)
        {
            user.resetPlayerTime();
            context.sendTranslated("&aReseted the time for &2%s&a!", user.getName());
            if (other)
            {
                user.sendTranslated("&eYour time was reseted!");
            }
        }
        else
        {
            if (context.hasFlag("l"))
            {
                user.resetPlayerTime();
                user.setPlayerTime(time, false);
                context.sendTranslated("&aTime locked to &6%s (%s)&a for &2%s&a!",
                                       Match.time().format(time),
                                       Match.time().getNearTimeName(time),
                                       user.getName());
            }
            else
            {
                user.resetPlayerTime();
                user.setPlayerTime(time, true);
                context.sendTranslated("&aTime set to &6%s (%s)&a for &2%s&a!",
                                       Match.time().format(time),
                                       Match.time().getNearTimeName(time),
                                       user.getName());
            }
            if (other)
            {
                context.sendTranslated("&aYour time was set to &6%s (%s)&a!",
                                       Match.time().format(time),
                                       Match.time().getNearTimeName(time));
            }
        }
    }

    private void setTime(World world, long time)
    {
        world.setTime(time);
    }

    private final class LockTask implements Runnable
    {

        private final Map<String, Long> worlds = new HashMap<>();
        private int taskid = -1;

        public void add(World world)
        {
            this.worlds.put(world.getName(), world.getTime());
            if (this.taskid == -1)
            {
                this.taskid = taskmgr.runTimer(basics, this, 10, 10);
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
