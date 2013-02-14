package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import org.bukkit.World;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import gnu.trove.map.hash.THashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.command.CommandSender;

public class DiscoCommand
{
    private final Fun module;
    private final Map<String, DiscoTask> activeTasks;

    public DiscoCommand(Fun module)
    {
        this.module = module;
        this.activeTasks = new THashMap<String, DiscoTask>();
    }

    @Command(desc = "Changes from day to night and vice verca", usage = "[world] [delay <value>]", max = 0, params = @Param(names = {
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
                context.sendMessage("fun", "&cThe given world was not found!");
                return;
            }
        }

        if (world == null)
        {
            context.sendMessage("fun", "&cNo world has been specified!");
            return;
        }

        final int delay = context.getParam("delay", 10);
        if (delay < 1 || delay > this.module.getConfig().maxDiscoDelay)
        {
            illegalParameter(context, "fun", "&cThe delay has to be a number between 0 and %d", this.module.getConfig().maxDiscoDelay);
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
            context.sendMessage("fun", "&aThe disco has been stopped!");
        }
        else
        {
            task = new DiscoTask(world, delay);
            if (task.start())
            {
                this.activeTasks.put(world.getName(), task);
                context.sendMessage("fun", "&aThe disco started!");
            }
            else
            {
                context.sendMessage("fun", "&cThe disco couldn not be started!");
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
