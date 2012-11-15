package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.FunConfiguration;
import de.cubeisland.cubeengine.fun.listeners.NukeListener;
import de.cubeisland.cubeengine.fun.listeners.RocketListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;

public class FunCommands
{
    private final FunConfiguration config;
    private final Fun module;
    private final UserManager userManager;
    private final TaskManager taskManager;
    
    public FunCommands(Fun module)
    {
        this.module = module;
        this.userManager = module.getUserManager();
        this.taskManager = module.getCore().getTaskManager();
        this.config = module.getConfig();
    }

    @Command(
        names = {"lightning", "strike"},
        desc = "strucks a player or the location you are looking at by lightning.",
        max = 0,
        params = {
            @Param(names = {"player", "p"}, type = User.class),
            @Param(names = {"damage", "d"}, type = Integer.class),
            @Param(names = {"fireticks", "f"}, type = Integer.class)
        },
        usage = "[player <name>]"
    )
    public void lightning(CommandContext context)
    {
        User user;
        Location location;
        int damage = context.getNamed("damage", Integer.class, Integer.valueOf(-1));

        if(context.hasNamed("player"))
        {
            user = context.getNamed("player", User.class);
            if (user == null)
            {
                invalidUsage(context, "core", "User not found!");
            }
            location = user.getLocation();
            if(damage != -1 && damage < 0 && damage > 20)
            {
                invalidUsage(context, "fun", "The damage value has to be a number from 1 to 20");
            }
            user.setFireTicks(20 * context.getNamed("fireticks", Integer.class, Integer.valueOf(0)));
        }
        else
        {
            user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");
            location = user.getTargetBlock(null, config.lightningDistance).getLocation();
        }

        user.getWorld().strikeLightningEffect(location);
        if(damage != -1)
        {
            user.damage(damage);
        }
    }

    @Command(
        names = {"throw"},
        desc = "The CommandSender throws a certain amount of snowballs or eggs. Default is one.",
        min = 1,
        max = 2,
        usage = "<egg|snowball> [amount]"
    )
    public void throwItem(CommandContext context)
    {
        User user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");

        String material = context.getString(0);
        int amount = context.getIndexed(1, Integer.class, 1);
        Class materialClass = null;

        if(amount > this.config.maxThrowNumber || amount < 1)
        {
            invalidUsage(context, "fun", "The amount has to be a number from 1 to %d", this.config.maxThrowNumber);
        }
        
        if (material.equalsIgnoreCase("snowball"))
        {
            materialClass = Snowball.class;
        }
        else if(material.equalsIgnoreCase("egg"))
        {
            materialClass = Egg.class;
        }
        else
        {
            invalidUsage(context, "fun", "The Item %s is not supported!", material);
        }

        ThrowItem throwItem = new ThrowItem(this.userManager, user.getName(), materialClass);
        for (int i = 0; i < amount; i++)
        {
            this.taskManager.scheduleSyncDelayedTask(module, throwItem, i * 10);
        }
    }

    @Command(
        desc = "The CommandSender throws a certain amount of fireballs. Default is one.",
        max = 1,
        usage = "[amount]"
    )
    public void fireball(CommandContext context)
    {
        User user = context.getSenderAsUser("core", "&cThis command can only be used by a player!");

        int amount = context.getIndexed(0, Integer.class, 1);
        if(amount < 1 || amount > this.config.maxFireballNumber)
        {
            invalidUsage(context, "fun", "The amount has to be a number from 1 to %d", this.config.maxFireballNumber);
        }
        ThrowItem throwItem = new ThrowItem(this.userManager, user.getName(), Fireball.class);
        for (int i = 0; i < amount; i++)
        {
            this.taskManager.scheduleSyncDelayedTask(module, throwItem, i * 10);
        }
    }

    @Command(
        desc = "slaps a player",
        min = 1,
        max = 2,
        usage = "<player> [damage]"
    )
    public void slap(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
              invalidUsage(context, "core", "User not found!");
        }
        
        int damage = context.getIndexed(1, Integer.class, 3);

        if (damage < 1 || damage > 20)
        {
            invalidUsage(context, "fun", "Only damage values from 1 to 20 are allowed!");
            return;
        }
        
        user.damage(damage);
    }

    @Command(
        desc = "burns a player",
        min = 1,
        max = 2,
        flags = {@Flag(longName = "unset", name = "u")},
        usage = "<player> [seconds] [-unset]"
    )
    public void burn(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            invalidUsage(context, "core", "User not found!");
        }
        
        int seconds = context.getIndexed(1, Integer.class, 5);

        if (context.hasFlag("u"))
        {
            seconds = 0;
        }
        else if (seconds < 1 || seconds > 26)
        {
            invalidUsage(context, "fun", "Only 1 to 26 seconds are permitted!");
        }

        user.setFireTicks(seconds * 20);
    }

    @Command(
        desc = "rockets a player",
        max = 1,
        usage = "[height]",
        params = @Param(names = {"player", "p"}, type = User.class)
    )
    public void rocket(CommandContext context)
    {
        RocketListener rocketListener = this.module.getRocketListener();
        int ticks = 20;
        
        int height = context.getIndexed(0, Integer.class, 10);
        User user = (context.hasNamed("player")) ? 
            context.getNamed("player", User.class, null) : 
            context.getSenderAsUser("fun", "&cThis command can only be used by a player!");
        
        if (user == null)
        {
            invalidUsage(context, "core", "User not found!");
        }

        if (height > 100)
        {
            invalidUsage(context, "fun", "Do you never wanna see %s again?", user.getName());
        }
        else
        {
            if (height < 0)
            {
                invalidUsage(context, "fun", "The height has to be greater than 0");
            }
        }
        
        user.setVelocity(new Vector(0.0, (double)height/10, 0.0));
        rocketListener.addInstance(user, ticks);
        
        for(int i = 1; i <= ticks; i++)
        {
            this.taskManager.scheduleSyncDelayedTask(module, rocketListener, 5 * i);
        }
    }

    @Command(
        desc = "an tnt carpet is falling at a player or the place the player is looking at",
        max = 1,
        flags = {@Flag(longName = "unsafe", name = "u")},
        usage = "[radius] [height <value>] [player <name>] [-unsafe]",
        params = {
            @Param(names = {"player", "p"}, type = User.class),
            @Param(names = {"height", "h"}, type = Integer.class),
            @Param(names = {"concentration", "c"})
        }
    )
    public void nuke(CommandContext context)
    {
        NukeListener nukeListener = this.module.getNukeListener();
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
                    invalidUsage(context, "fun", "The named Paramter concentration has a wron usage. 1.1 is the right. You used %s", concNamed);
                }
            }
        }
        if(radius > this.config.nukeRadiusLimit)
        {
            invalidUsage(context, "fun", "&cThe radius should not be greater than %d", this.config.nukeRadiusLimit);
        }
        if(concentration < 1)
        {
            invalidUsage(context, "fun", "&cThe concentration should not be smaller than 1");
        }
        if(concentrationOfBlocksPerCircle < 1)
        {
            invalidUsage(context, "fun", "&cThe concentration of Blocks per Circle should not be smaller than 1");
        }
        if(height < 1)
        {
            invalidUsage(context, "fun", "&cThe height can't be less than 1");
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
                    Math.cos(j * angle) * i + centerOfTheCircle.getX(), 
                    centerOfTheCircle.getY(), 
                    Math.sin(j * angle) * i + centerOfTheCircle.getZ()
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
}
