package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import org.bukkit.World;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;

public class DiscoCommand implements Runnable
{
    private final Fun module;
    
    private World world;
    private int taskId;
    private long startTime;
    private User player;
    
    public DiscoCommand(Fun module)
    {
        this.module = module;
    }
    
    @Command(
        desc = "Changes from day to night and vice verca",
        max = 0,
        params = 
        { @Param(names = {"delay", "d"}, type = Integer.class) },
        usage = "[delay <value>]"
    )
    public void disco(CommandContext context)
    {
        if(this.world == null)
        {
            this.player = context.getSenderAsUser("core", "&cThis command can only be used by a player!");
            this.world = player.getWorld();
            int delay = context.getNamed("delay", Integer.class, 10);
            
            if(delay < 1 || delay > this.module.getConfig().maxDiscoDelay)
            {
                illegalParameter(context, "fun", "&cThe delay has to be a number between 0 and %d", this.module.getConfig().maxDiscoDelay);
            }
            
            this.startTime = world.getTime();
            
            this.taskId = this.module.getTaskManger().scheduleSyncRepeatingTask(this.module, this, 0, delay);
        }
        else
        {
            this.remove();
        }
    }
    
    private void remove()
    {
        this.world.setTime(startTime);
        this.module.getTaskManger().cancelTask(module, taskId);
        this.taskId = -1;
        this.world = null;
        this.startTime = -1;
    }

    @Override
    public void run() 
    {
        if(this.world != null)
        {
            if(this.world.getTime() > 12000)
            {
                this.world.setTime(6000);
            }
            else 
            {
                this.world.setTime(18000);
            }
        }
        if(!player.isOnline())
        {
            this.remove();
        }
    }
}
