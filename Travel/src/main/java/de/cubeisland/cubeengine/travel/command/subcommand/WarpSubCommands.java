package de.cubeisland.cubeengine.travel.command.subcommand;

import java.util.TreeMap;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.result.AsyncResult;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.travel.Travel;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;
import de.cubeisland.cubeengine.travel.storage.TeleportPoint;
import de.cubeisland.cubeengine.travel.storage.Warp;

public class WarpSubCommands
{
    private final TelePointManager telePointManager;

    public WarpSubCommands(Travel module)
    {
        this.telePointManager = module.getTelepointManager();
    }

    @Alias(names = {
    "createwarp", "mkwarp", "makewarp"
    })
    @Command(names = {
    "create", "make"
    }, flags = {
            @Flag(name = "p", longName = "private")
    }, desc = "Create a warp", min = 1, max = 1)
    public void createWarp(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User) context.getSender();
            String name = context.getString(0);
            if (telePointManager.hasWarp(name) && !context.hasFlag("p"))
            {
                context.sendMessage("travel", "A public warp by that name already exist! maybe you want to include the -private flag?");
                return;
            }
            if (name.contains(":") || name.length() >= 32)
            {
                context.sendMessage("travel", "&4Warps may not have names that are longer then 32 characters, and they may not contain colon(:)'s!");
                return;
            }
            Location loc = sender.getLocation();
            Warp warp = telePointManager.createWarp(loc, name, sender, (context.hasFlag("p") ? TeleportPoint.Visibility.PRIVATE : TeleportPoint.Visibility.PUBLIC));
            context.sendMessage("travel", "Your warp have been created");
            return;
        }
        context.sendMessage("travel", "You have to be in the world to set a warp");


    }

    @Alias(names = {
    "removewarp", "deletewarp", "delwarp", "remwarp"
    })
    @Command(names = {
    "remove", "delete"
    }, desc = "Remove a warp", min = 1, max = 1)
    public void removeWarp(CommandContext context)
    {
        Warp warp;
        if (context.getSender() instanceof User)
        {
            warp = telePointManager.getWarp((User) context.getSender(), context.getString(0));
        }
        else
        {
            warp = telePointManager.getWarp(context.getString(0));
        }
        if (warp == null)
        {
            context.sendMessage("travel", "The warp could not be found");
            return;
        }
        telePointManager.deleteWarp(warp);
        context.sendMessage("travel", "The warp is now deleted");
    }

    @Command(desc = "Rename a warp", min = 2, max = 2)
    public void rename(CommandContext context)
    {
        String name = context.getString(1);
        Warp warp;
        if (context.getSender() instanceof User)
        {
            warp = telePointManager.getWarp((User) context.getSender(), context.getString(0));
        }
        else
        {
            warp = telePointManager.getWarp(context.getString(0));
        }
        if (warp == null)
        {
            context.sendMessage("travel", "The warp could not be found");
            return;
        }

        if (name.contains(":") || name.length() >= 32)
        {
            context.sendMessage("travel", "&4Warps may not have names that are longer then 32 characters, and they may not contain colon(:)'s!");
            return;
        }

        telePointManager.renameWarp(warp, name);
        context.sendMessage("travel", "The warps name is now changed");
    }

    @Command(desc = "Move a warp", min = 1, max = 2)
    public void move(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (!(sender instanceof User)) return;
        User user = (User)sender;

        Warp warp = telePointManager.getWarp(user, context.getString(0));
        if (warp == null)
        {
            user.sendMessage("travel", "That warp could not be found!");
            return;
        }
        if (!warp.isOwner(user))
        {
            user.sendMessage("travel", "You are not allowed to edit that warp!");
            return;
        }
        warp.setLocation(user.getLocation());
        warp.update();
        user.sendMessage("travel", "The warp is now moved to your current location");
    }

    @Command(desc = "Search for a warp", min = 1, max = 2)
    public CommandResult search(CommandContext context)
    {
        String search = context.getString(0);
        Warp first;
        if (context.getSender() instanceof User)
        {
            first = telePointManager.getWarp((User)context.getSender(), search);
        }
        else
        {
            first = telePointManager.getWarp(search);
        }
        if (first != null)
        {
            context.sendMessage("travel", "Found a direct match: %s owned by %s", first.getName(), first.getOwner().getDisplayName());
            return null;
        }

        return new AsyncResult()
        {
            TreeMap<String, Integer> results;

            @Override
            public void asyncMain(CommandContext context)
            {
                results = telePointManager.searchWarp(context.getString(0), context.getSender());
            }

            @Override
            public void onFinish(CommandContext context)
            {
                context.sendMessage("travel", "Here is the top %d results:", context.getArg(1, Integer.class, 5));
                int position = 1;
                for (String warp : results.keySet())
                {
                    context.sendMessage(position++ + ". " + warp);
                    if (position == context.getArg(1, Integer.class, 5))
                    {
                        break;
                    }
                }
            }
        };
    }

    @Command(desc = "List all available warps")
    public void list(CommandContext context)
    {}
}
