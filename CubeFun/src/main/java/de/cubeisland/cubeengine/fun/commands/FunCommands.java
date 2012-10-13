/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.listeners.RocketListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;

/**
 *
 * @author Wolfi
 */
public class FunCommands
{
    Fun module;
    UserManager userManager;
    TaskManager taskManager;
    RocketListener rocketListener;

    public FunCommands(Fun module)
    {
        this.module = module;
        this.userManager = module.getUserManager();
        this.taskManager = module.getCore().getTaskManager();

        this.rocketListener = module.getRocketListener();
    }

    @Command(
        names =
    {
        "lightning", "strike"
    },
    desc = "strucks a player or the place you are looking at by lightning.",
    max = 1,
    usage = "[player]")
    public void lightning(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User user = this.userManager.getUser(sender);
        Location location = null;

        if (context.getIndexed().isEmpty() && sender instanceof Player)
        {
            location = user.getTargetBlock(null, 200).getLocation();
        }
        else
        {
            if (!context.getIndexed().isEmpty())
            {
                user = context.getUser(0);

                if (user == null)
                {
                    invalidUsage(context, "core", "User not found!");
                }

                location = user.getLocation();
            }
            else
            {
                sender.sendMessage("This command can only be used by a player!");
                return;
            }
        }

        user.getWorld().strikeLightning(location);
    }

    @Command(
        names =
    {
        "throw"
    },
    desc = "The CommandSender throws a certain amount of snowballs. Default is one.",
    min = 1,
    max = 2,
    usage = "<egg|snowball> [amount]")
    public void throwItem(CommandContext context)
    {
        User user = context.getSenderAsUser("core", "&cThis command can only be used by a player!");

        String material = context.getString(0);
        int amount = 1;
        Class materialClass;

        if (context.hasIndexed(1))
        {
            amount = context.getIndexed(1, Integer.class, 1);
        }

        if (material.equalsIgnoreCase("snowball"))
        {
            materialClass = Snowball.class;
        }
        else
        {
            if (material.equalsIgnoreCase("egg"))
            {
                materialClass = Egg.class;
            }
            else
            {
                invalidUsage(context, "fun", "The Item %s is not supported", material);
                return;
            }
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
    flags =
    {
        @Flag(longName = "small", name = "s")
    },
    usage = "[amount] [-small]")
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
    usage = "<player> [damage]")
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
                invalidUsage(context, "fun", "only damagevalues between 1 and 20 are allowed!");
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
    flags =
    {
        @Flag(longName = "unset", name = "u")
    },
    usage = "<player> [seconds] [-unset]")
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
                invalidUsage(context, "fun", "only 1 to 26 seconds are permitted!");
            }
        }

        user.setFireTicks(seconds * 20);
    }

    @Command(
        desc = "rockets a player",
    min = 2,
    max = 2,
    usage = "<player> <distance>")
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

        this.rocketListener.addPlayer(user);
        this.taskManager.scheduleSyncDelayedTask(module, this.rocketListener, 110);
    }

    @Command(
    desc = "an tnt carpet is falling at a player or the place the player is looking at",
    max = 2,
    usage = "[radius] [height] [player <name>]",
    params =
    {
        @Param(
                names =
        {
            "player", "p"
        },
        types =
        {
            User.class
        })
    })
    public void nuke(CommandContext context)
    {
        context.sendMessage(ChatColor.RED + "not implemented yet");
        /*
         * User user = null;
         * Location location = null;
         * int radius = 1;
         *
         * if(context.hasNamed("player"))
         * {
         * user = context.getNamed("player", User.class);
         * location = user.getTargetBlock(null, 40).getLocation();
         * }
         * else
         * {
         * user = context.getSenderAsUser("core", "&cThis command can only be
         * used by a player!");
         * if(user == null)
         * {
         * invalidUsage(context, "fun", "User not found");
         * }
         * location = user.getLocation();
         * }
         *
         * while(location.getBlock().getType() != Material.AIR)
         * {
         * location = location.add(0,1,0);
         * }
         *
         * user.getWorld().spawn(location, TNTPrimed.class);
         * */
    }
}
