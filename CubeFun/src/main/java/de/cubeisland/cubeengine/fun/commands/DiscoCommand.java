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
    
    public DiscoCommand(Fun module)
    {
        this.module = module;
    }
    
    @Command(
        desc = "changes from day to night and vice verca (default every 10 ticks)",
        min = 1,
        max = 1,
        params = { @Param(names = {"ticks", "t"}, type = Integer.class) },
        usage = "<changes>"
    )
    public void disco(CommandContext context)
    {
        try
        {
            final int changes = Integer.valueOf(context.getString(0));
            final World world = context.getSenderAsUser("core", "&cThis command can only be used by a player!").getWorld();
            int ticks = context.getNamed("ticks", Integer.class, Integer.valueOf(10));
            
            if(ticks < 1)
            {
                illegalParameter(context, "fun", "the ticks has to be a number greater than 0");
            }
            if(changes > this.module.getConfig().maxDiscoChanges)
            {
                illegalParameter(context, "fun", "The number of changes of day and night shouldn't be over %d", this.module.getConfig().maxDiscoChanges);
            }
            
            if(world != null)
            {
                final long defaultTime = world.getTime();
                for(int i = 0; i <= changes * 2; i++)
                {
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
                            }
                        }
                    }, i * ticks);
                } 
            }
              
        }
        catch(NumberFormatException e)
        {
            illegalParameter(context, "fun", "\"%s\" is not a number", context.getString(0));
        }
    }
}
