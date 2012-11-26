package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.fun.Fun;
import org.bukkit.World;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;

public class DiscoCommand 
{
    private final Fun module;
    
    private boolean running;
    
    public DiscoCommand(Fun module)
    {
        this.module = module;
        this.running = false;
    }
    
    @Command(
        desc = "Changes from day to night and vice verca",
        min = 1,
        max = 1,
        params = 
        { @Param(names = {"delay", "d"}, type = Integer.class) },
        usage = "<changes> [delay <value>]"
    )
    public void disco(CommandContext context)
    {
        if(this.running)
        {
            context.sendMessage("&eThe disco command is currently running");
            return;
        }
        try
        {
            final int changes = Integer.valueOf(context.getString(0));
            final World world = context.getSenderAsUser("core", "&cThis command can only be used by a player!").getWorld();
            int delay = context.getNamed("delay", Integer.class, Integer.valueOf(10));
            
            if(delay < 1 || delay > this.module.getConfig().maxDiscoDelay)
            {
                illegalParameter(context, "fun", "&cThe ticks has to be a number between 0 and %d", this.module.getConfig().maxDiscoDelay);
            }
            if(changes > this.module.getConfig().maxDiscoChanges || changes < 1)
            {
                illegalParameter(context, "fun", "&cThe number of changes of day and night shouldn't be over %d or less than 1.", this.module.getConfig().maxDiscoChanges);
            }
            
            if(world != null)
            {
                final long defaultTime = world.getTime();
                for(int i = 0; i <= changes * 2; i++)
                {
                    this.running = true;
                    final int j = i;
                    this.module.getTaskManger().scheduleSyncDelayedTask(module, new Runnable() 
                    {
                        @Override
                        public void run()
                        {
                            if(world.getTime() > 12000)
                            {
                                world.setTime(6000);
                            }
                            else 
                            {
                                world.setTime(18000);
                            }
                            
                            if(j == changes * 2)
                            {
                                world.setTime(defaultTime);
                                running = false;
                            }
                        }
                    }, i * delay);
                } 
            }
              
        }
        catch(NumberFormatException e)
        {
            illegalParameter(context, "fun", "&c\"%s\" is not a number", context.getString(0));
        }
    }
}
