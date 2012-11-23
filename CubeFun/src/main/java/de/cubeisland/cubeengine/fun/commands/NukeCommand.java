package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.FunConfiguration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class NukeCommand
{
    private final NukeListener nukeListener;
    private final FunConfiguration config;
    
    public NukeCommand(Fun module)
    {
        config = module.getConfig();
        nukeListener = new NukeListener();
        module.registerListener(nukeListener);
    }
    
    @Command(
        desc = "an tnt carpet is falling at a player or the place the player is looking at",
        max = 1,
        flags = {@Flag(longName = "unsafe", name = "u")},
        usage = "[radius] [height <value>] [player <name>] [-unsafe]",
        params = {
            @Param(names = {"player", "p"}, type = User.class),
            @Param(names = {"height", "h"}, type = Integer.class),
            @Param(names = {"concentration", "c"}, type = String.class)
        }
    )
    public void nuke(CommandContext context)
    {
        int noBlock = 0;
        
        int numberOfBlocks = 0;
        
        int radius = context.getIndexed(0, Integer.class, 0);
        int height = context.getNamed("height", Integer.class, Integer.valueOf(5));
        int concentration = 1;
        int concentrationOfBlocksPerCircle = 1;
        
        Location centerOfTheCircle;
        User user;
        
        if(context.hasNamed("concentration"))
        {
            String concNamed = context.getNamed("concentration", String.class, null);
            Matcher matcher = Pattern.compile("(\\d*)(\\.(\\d+))?").matcher(concNamed);
            if(concNamed != null && matcher.matches())
            {
                try
                {
                    if(matcher.group(1) != null && matcher.group(1).length() > 0)
                    {
                        concentration = Integer.valueOf(matcher.group(1));
                    }
                    if(matcher.group(3) != null && matcher.group(3).length() > 0)
                    {
                        concentrationOfBlocksPerCircle = Integer.valueOf(matcher.group(3));
                    }
                }
                catch(NumberFormatException e)
                {
                    invalidUsage(context, "fun", "The named Paramter concentration has a wrong usage. 1.1 is the right. You used %s", concNamed);
                }
            }
            if(concentration > this.config.nukeConcentrationLimit || concentrationOfBlocksPerCircle > this.config.nukeConcentrationLimit)
            {
                illegalParameter(context, "fun", "The concentration should not be greater than %d", this.config.nukeConcentrationLimit);
            }
        }
        if(radius > this.config.nukeRadiusLimit)
        {
            illegalParameter(context, "fun", "&cThe radius should not be greater than %d", this.config.nukeRadiusLimit);
        }
        if(concentration < 1)
        {
            illegalParameter(context, "fun", "&cThe concentration should not be smaller than 1");
        }
        if(concentrationOfBlocksPerCircle < 1)
        {
            illegalParameter(context, "fun", "&cThe concentration of Blocks per Circle should not be smaller than 1");
        }
        if(height < 1)
        {
            illegalParameter(context, "fun", "&cThe height can't be less than 1");
        }
        
        if(context.hasNamed("player"))
        {
            user = context.getNamed("player", User.class);
            if(user == null)
            {
                invalidUsage(context, "fun", "User not found");
            }
            centerOfTheCircle = user.getLocation();
        }
        else
        {
            user = context.getSenderAsUser("core", "&cThis command can only be used by a player!");
            centerOfTheCircle = user.getTargetBlock(null, 40).getLocation();
        }
         
        while(noBlock != height)
        {
            centerOfTheCircle.add(0,1,0);
            if(centerOfTheCircle.getBlock().getType() == Material.AIR)
            {
                noBlock++;
            }
            else
            {
                noBlock = 0;
            }
        }
        
        for(int i = radius; i > 0; i -= concentration)
        {
            double blocksPerCircle = i * 4 / concentrationOfBlocksPerCircle;
            double angle = 2 * Math.PI / blocksPerCircle;
            for(int j = 0; j < blocksPerCircle; j++)
            {
                TNTPrimed tnt = user.getWorld().spawn(
                    new Location(centerOfTheCircle.getWorld(), 
                    Math.cos(j * angle) * i + centerOfTheCircle.getX() + 0.5, 
                    centerOfTheCircle.getY(), 
                    Math.sin(j * angle) * i + centerOfTheCircle.getZ() + 0.5
                ), TNTPrimed.class);
                tnt.setVelocity(new Vector(0,0,0));
                numberOfBlocks++;

                if(!context.hasFlag("u"))
                {
                    nukeListener.add(tnt);
                }
            }
        }
        if(radius == 0)
        {
            TNTPrimed tnt = user.getWorld().spawn(centerOfTheCircle, TNTPrimed.class);
            if(!context.hasFlag("u"))
            {
                nukeListener.add(tnt);
                numberOfBlocks++;
            }
        }
        
        context.sendMessage("fun", "You spawnt %d blocks of TNT", numberOfBlocks);
    }

    private class NukeListener implements Listener
    {
        private final Set<TNTPrimed> noBlockDamageSet;

        public NukeListener()
        {
            this.noBlockDamageSet = new HashSet<TNTPrimed>();
        }

        public void add(TNTPrimed tnt)
        {
            noBlockDamageSet.add(tnt);
        }

        public void remove(TNTPrimed tnt)
        {
            noBlockDamageSet.remove(tnt);
        }

        public boolean contains(TNTPrimed tnt)
        {
            return noBlockDamageSet.contains(tnt);
        }

        @EventHandler
        public void onBlockDamage(EntityExplodeEvent event)
        {
            try
            {
                if (event.getEntityType() == EntityType.PRIMED_TNT && this.contains((TNTPrimed)event.getEntity()))
                {
                    event.blockList().clear();
                    remove((TNTPrimed)event.getEntity());
                }
            }
            catch (NullPointerException ignored)
            {}
        }
    }

}
