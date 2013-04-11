package de.cubeisland.cubeengine.travel.interactions;

import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.result.AsyncResult;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.travel.Travel;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;
import de.cubeisland.cubeengine.travel.storage.TeleportPoint;
import de.cubeisland.cubeengine.travel.storage.Warp;

public class WarpCommand extends ContainerCommand
{
    private final TelePointManager telePointManager;

    public WarpCommand(Travel module)
    {
        super(module, "warp", "Teleport to a warp");
        this.telePointManager = module.getTelepointManager();
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (context.isSender(User.class) && context.getArgCount() > 0)
        {
            User sender = (User) context.getSender();
            Warp warp = telePointManager.getWarp(sender, context.getString(0).toLowerCase());
            if (warp == null)
            {
                context.sendTranslated("&4You don't have access to any warp with that name");
                return null;
            }

            sender.teleport(warp.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
            context.sendTranslated("&6You have been teleported to the warp &9%s", context.getString(0));
        }
        else
        {
            return super.run(context);
        }
        return null;
    }


    @Alias(names = {
        "createwarp", "mkwarp", "makewarp"
    })
    @Command(names = {
        "create", "make"
    }, flags = {
        @Flag(name = "priv", longName = "private")
    }, desc = "Create a warp", min = 1, max = 1)
    public void createWarp(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User) context.getSender();
            String name = context.getString(0);
            if (telePointManager.hasWarp(name) && !context.hasFlag("priv"))
            {
                context.sendTranslated("A public warp by that name already exist! maybe you want to include the -private flag?");
                return;
            }
            if (name.contains(":") || name.length() >= 32)
            {
                context.sendTranslated("&4Warps may not have names that are longer then 32 characters, and they may not contain colon(:)'s!");
                return;
            }
            Location loc = sender.getLocation();
            Warp warp = telePointManager.createWarp(loc, name, sender, (context.hasFlag("priv") ? TeleportPoint.Visibility.PRIVATE : TeleportPoint.Visibility.PUBLIC));
            context.sendTranslated("Your warp have been created");
            return;
        }
        context.sendTranslated("You have to be in the world to set a warp");


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
            context.sendTranslated("The warp could not be found");
            return;
        }
        telePointManager.deleteWarp(warp);
        context.sendTranslated("The warp is now deleted");
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
            context.sendTranslated("The warp could not be found");
            return;
        }

        if (name.contains(":") || name.length() >= 32)
        {
            context.sendTranslated("&4Warps may not have names that are longer then 32 characters, and they may not contain colon(:)'s!");
            return;
        }

        telePointManager.renameWarp(warp, name);
        context.sendTranslated("The warps name is now changed");
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
            user.sendTranslated("That warp could not be found!");
            return;
        }
        if (!warp.isOwner(user))
        {
            user.sendTranslated("You are not allowed to edit that warp!");
            return;
        }
        warp.setLocation(user.getLocation());
        warp.update();
        user.sendTranslated("The warp is now moved to your current location");
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
            context.sendTranslated("Found a direct match: %s owned by %s", first.getName(), first.getOwner().getDisplayName());
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
                context.sendTranslated("Here is the top %d results:", context.getArg(1, Integer.class, 5));
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
    {
        // TODO
    }
}
