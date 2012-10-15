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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.SmallFireball;
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
        params = {@Param(names = {"player", "p"}, types = {User.class})},
        usage = "[player <name>]"
    )
    public void lightning(CommandContext context)
    {
        User user;
        Location location;

        if(context.hasNamed("player"))
        {
            user = context.getNamed("player", User.class);
            if (user == null)
            {
                invalidUsage(context, "core", "User not found!");
            }
            location = user.getLocation();
        }
        else
        {
            user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");
            location = user.getTargetBlock(null, config.lightningDistance).getLocation();
        }

        user.getWorld().strikeLightning(location);
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
        int amount = 1;
        Class materialClass = null;

        if (context.hasIndexed(1))
        {
            amount = context.getIndexed(1, Integer.class, 1);
            if(amount > this.config.maxThrowNumber)
            {
                invalidUsage(context, "fun", "The maximum amount is %d", this.config.maxThrowNumber);
            }
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
        flags = {@Flag(longName = "small", name = "s")},
        usage = "[amount] [-small]"
    )
    public void fireball(CommandContext context)
    {
        User user = context.getSenderAsUser("core", "&cThis command can only be used by a player!");

        int amount = 1;
        Class material;

        if (context.hasIndexed(0))
        {
            amount = context.getIndexed(0, Integer.class, 1);
        }

        if (context.hasFlag("s"))
        {
            material = SmallFireball.class;
        }
        else
        {
            material = Fireball.class;
        }

        ThrowItem throwItem = new ThrowItem(this.userManager, user.getName(), material);
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
        if (user != null)
        {
            int damage = 3;
            if (context.hasIndexed(1))
            {
                damage = context.getIndexed(1, Integer.class, 3);
            }

            if (damage < 1 || damage > 20)
            {
                invalidUsage(context, "fun", "Only damage values from 1 to 20 are allowed!");
                return;
            }

            user.damage(damage);
        }
        else
        {
            invalidUsage(context, "core", "User not found!");
        }
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
        int seconds = 5;

        if (context.hasIndexed(1))
        {
            seconds = context.getIndexed(1, Integer.class, 5);
        }

        if (context.hasFlag("u"))
        {
            seconds = 0;
        }
        else
        {
            if (seconds < 1 || seconds > 26)
            {
                invalidUsage(context, "fun", "Only 1 to 26 seconds are permitted!");
            }
        }

        user.setFireTicks(seconds * 20);
    }

    @Command(
        desc = "rockets a player",
        min = 2,
        max = 2,
        usage = "<player> <distance>"
    )
    public void rocket(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            invalidUsage(context, "core", "User not found!");
        }

        int distance = context.getIndexed(1, Integer.class, 8);

        if (distance > 100)
        {
            invalidUsage(context, "fun", "Do you never wanna see %s again?", user.getName());
        }
        else
        {
            if (distance < 0)
            {
                invalidUsage(context, "fun", "The distance has to be greater than 0");
            }
        }

        user.setVelocity(new Vector(0.0, (double)distance / 10, 0.0));
        
        RocketListener rocketListener = this.module.getRocketListener();
        rocketListener.addPlayer(user);
        this.taskManager.scheduleSyncDelayedTask(module, rocketListener, 110);
    }

    @Command(
        desc = "an tnt carpet is falling at a player or the place the player is looking at",
        max = 1,
        flags = {@Flag(longName = "unsafe", name = "u")},
        usage = "[radius] [height <value>] [player <name>] [-unsafe]",
        params = {
            @Param(names = {"player", "p"}, types = {User.class}),
            @Param(names = {"height", "h"}, types = {Integer.class})
        }
    )
    public void nuke(CommandContext context)
    {
        int spawnLimit = 20;
        User user;
        Location centerOfTheCircle;
        int radius = 0;
        Integer height = 5;
        NukeListener nukeListener = this.module.getNukeListener();
        
        int noBlock = 0;
        
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
        
        if(context.hasIndexed(0))
        {
            radius = context.getIndexed(0, Integer.class, 1);
            if(radius > spawnLimit)
            {
                invalidUsage(context, "fun", "&cThe radius should be not over %d", spawnLimit);
            }
        }
        if(context.hasNamed("height"))
        {
            height = context.getNamed("height", Integer.class);
            if(height == null)
            {
                height = 5;
            }
            else if(height < 1)
            {
                invalidUsage(context, "fun", "&cThe height can't be less than 1");
            }
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
        
        for(int i = radius; i > 0; i--)
        {
            double angle = 2 * Math.PI / (i * 4);
            for(int j = 0; j < (i * 4); j++)
            {
                double x = Math.cos(j * angle) * i + centerOfTheCircle.getX();
                double z = Math.sin(j * angle) * i + centerOfTheCircle.getZ();
                TNTPrimed tnt = user.getWorld().spawn(new Location(centerOfTheCircle.getWorld(), x, centerOfTheCircle.getY(), z), TNTPrimed.class);
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
            }
        }
    }
}
