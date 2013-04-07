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
package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import gnu.trove.map.hash.THashMap;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Iterator;
import java.util.Map;

public class DiscoCommand
{
    private final Fun module;
    private final Map<String, DiscoTask> activeTasks;

    public DiscoCommand(Fun module)
    {
        this.module = module;
        this.activeTasks = new THashMap<String, DiscoTask>();
    }

    @Command(desc = "Changes from day to night and vice verca", usage = "[world] [delay <value>]", max = 1, params = @Param(names = {
    "delay", "d"
    }, type = Integer.class))
    public void disco(ParameterizedContext context)
    {
        final CommandSender sender = context.getSender();

        World world = null;
        if (sender instanceof User)
        {
            world = ((User)sender).getWorld();
        }

        if (context.hasArg(0))
        {
            world = context.getArg(0, World.class);
            if (world == null)
            {
                context.sendTranslated("&cThe given world was not found!");
                return;
            }
        }

        if (world == null)
        {
            context.sendTranslated("&cNo world has been specified!");
            return;
        }

        final int delay = context.getParam("delay", 10);
        if (delay < 1 || delay > this.module.getConfig().maxDiscoDelay)
        {
            context.sendTranslated("&cThe delay has to be a number between 0 and %d", this.module.getConfig().maxDiscoDelay);
            return;
        }

        DiscoTask task = this.activeTasks.remove(world.getName());
        if (task != null)
        {
            task.stop();
            Iterator<Map.Entry<String, DiscoTask>> iter = this.activeTasks.entrySet().iterator();
            while (iter.hasNext())
            {
                if (iter.next().getValue() == task)
                {
                    iter.remove();
                }
            }
            context.sendTranslated("&aThe disco has been stopped!");
        }
        else
        {
            task = new DiscoTask(world, delay);
            if (task.start())
            {
                this.activeTasks.put(world.getName(), task);
                context.sendTranslated("&aThe disco started!");
            }
            else
            {
                context.sendTranslated("&cThe disco couldn not be started!");
            }
        }
    }

    private class DiscoTask implements Runnable
    {
        private final World world;
        private long originalTime;
        private int taskID;
        private final long interval;

        public DiscoTask(World world, final long delay)
        {
            this.world = world;
            this.originalTime = -1;
            this.taskID = -1;
            this.interval = delay;
        }

        public World getWorld()
        {
            return this.world;
        }

        public boolean start()
        {
            this.originalTime = this.world.getTime();
            this.taskID = module.getCore().getTaskManager().scheduleSyncRepeatingTask(module, this, 0, this.interval);
            return this.taskID != -1;
        }

        public void stop()
        {
            if (this.taskID != -1)
            {
                module.getCore().getTaskManager().cancelTask(module, this.taskID);
                this.taskID = -1;
                if (this.originalTime != -1)
                {
                    this.world.setTime(this.originalTime);
                }
            }
        }

        @Override
        public void run()
        {
            if (this.world.getTime() > 12000)
            {
                this.world.setTime(6000);
            }
            else
            {
                this.world.setTime(18000);
            }
        }
    }
}
