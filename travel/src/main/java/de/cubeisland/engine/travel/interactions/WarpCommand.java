/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.travel.interactions;

import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.engine.core.command.ArgBounds;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.result.AsyncResult;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.TelePointManager;
import de.cubeisland.engine.travel.storage.Warp;

import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PRIVATE;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;

public class WarpCommand extends ContainerCommand
{
    private final Travel module;
    private final TelePointManager telePointManager;

    public WarpCommand(Travel module)
    {
        super(module, "warp", "Teleport to a warp");
        this.module = module;
        this.telePointManager = module.getTelepointManager();

        this.getContextFactory().setArgBounds(new ArgBounds(0, 1));
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (context.isSender(User.class) && context.getArgCount() > 0)
        {
            User sender = (User)context.getSender();
            Warp warp = telePointManager.getWarp(sender, context.getString(0).toLowerCase());
            if (warp == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "You don't have access to any warp with that name");
                return null;
            }

            Location location = warp.getLocation();
            if (location == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "This warp is in a world that no longer exists!");
                return null;
            }
            sender.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
            context.sendTranslated(MessageType.POSITIVE, "You have been teleported to the warp {name}", context.getString(0));
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
    }, permDefault = PermDefault.OP, desc = "Create a warp", min = 1, max = 1)
    public void createWarp(ParameterizedContext context)
    {
        if (this.telePointManager.getNumberOfWarps() == this.module.getConfig().warps.max)
        {
            context.sendTranslated(MessageType.CRITICAL, "The server have reached it's maximum number of warps!");
            context.sendTranslated(MessageType.NEGATIVE, "Some warps have to be delete for new ones to be made");
            return;
        }
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            String name = context.getString(0);
            if (telePointManager.hasWarp(name) && !context.hasFlag("priv"))
            {
                context
                    .sendTranslated(MessageType.NEGATIVE, "A public warp by that name already exist! maybe you want to include the -private flag?");
                return;
            }
            if (name.contains(":") || name.length() >= 32)
            {
                context
                    .sendTranslated(MessageType.NEGATIVE, "Warps may not have names that are longer then 32 characters, and they may not contain colon(:)'s!");
                return;
            }
            Location loc = sender.getLocation();
            Warp warp = telePointManager.createWarp(loc, name, sender, (context.hasFlag("priv") ? VISIBILITY_PRIVATE : VISIBILITY_PUBLIC));
            context.sendTranslated(MessageType.POSITIVE, "Your warp {name} has been created!", warp.getName());
            return;
        }
        context.sendTranslated(MessageType.CRITICAL, "This command can only be used by users!");
    }

    @Alias(names = {
        "removewarp", "deletewarp", "delwarp", "remwarp"
    })
    @Command(names = {
        "remove", "delete"
    }, permDefault = PermDefault.OP, desc = "Remove a warp", min = 1, max = 1)
    public void removeWarp(CommandContext context)
    {
        Warp warp;
        if (context.getSender() instanceof User)
        {
            warp = telePointManager.getWarp((User)context.getSender(), context.getString(0));
        }
        else
        {
            warp = telePointManager.getWarp(context.getString(0));
        }
        if (warp == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "The warp could not be found");
            return;
        }
        telePointManager.deleteWarp(warp);
        context.sendTranslated(MessageType.POSITIVE, "The warp is now deleted");
    }

    @Command(permDefault = PermDefault.OP, desc = "Rename a warp", min = 2, max = 2)
    public void rename(CommandContext context)
    {
        String name = context.getString(1);
        Warp warp;
        if (context.getSender() instanceof User)
        {
            warp = telePointManager.getWarp((User)context.getSender(), context.getString(0));
        }
        else
        {
            warp = telePointManager.getWarp(context.getString(0));
        }
        if (warp == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "The warp could not be found");
            return;
        }

        if (name.contains(":") || name.length() >= 32)
        {
            context
                .sendTranslated(MessageType.NEGATIVE, "Warps may not have names that are longer then 32 characters, and they may not contain colon(:)'s!");
            return;
        }

        telePointManager.renameWarp(warp, name);
        context.sendTranslated(MessageType.POSITIVE, "The warps name is now changed");
    }

    @Command(permDefault = PermDefault.OP, desc = "Move a warp", min = 1, max = 2)
    public void move(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (!(sender instanceof User))
        {
            return;
        }
        User user = (User)sender;

        Warp warp = telePointManager.getWarp(user, context.getString(0));
        if (warp == null)
        {
            user.sendTranslated(MessageType.NEGATIVE, "That warp could not be found!");
            return;
        }
        if (!warp.isOwner(user))
        {
            user.sendTranslated(MessageType.NEGATIVE, "You are not allowed to edit that warp!");
            return;
        }
        warp.setLocation(user.getLocation());
        warp.update();
        user.sendTranslated(MessageType.POSITIVE, "The warp is now moved to your current location");
    }

    @Command(permDefault = PermDefault.TRUE, desc = "Search for a warp", min = 1, max = 2)
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
            context.sendTranslated(MessageType.POSITIVE, "Found a direct match: {name#home} owned by {user}", first.getName(), first.getOwner());
            return null;
        }

        return new AsyncResult()
        {
            TreeMap<String, Integer> results;

            @Override
            public void main(CommandContext context)
            {
                results = telePointManager.searchWarp(context.getString(0), context.getSender());
            }

            @Override
            public void onFinish(CommandContext context)
            {
                context.sendTranslated(MessageType.NEUTRAL, "Here is the top {integer} results:", context.getArg(1, Integer.class, 5));
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

    @Command(permDefault = PermDefault.TRUE, desc = "List all available warps", flags = {
        @Flag(name = "pub", longName = "public"),
        @Flag(name = "priv", longName = "private"),
        @Flag(name = "o", longName = "owned"),
        @Flag(name = "i", longName = "invited")
    }, usage = "<user> <-PUBlic> <-PRIVate> <-Owned> <-Invited>", min = 0, max = 1)
    public void list(ParameterizedContext context)
    {
        int mask = context.getFlagCount() < 1 ? telePointManager.ALL : 0;
        if (context.hasFlag("pub"))
        {
            mask |= telePointManager.PUBLIC;
        }
        if (context.hasFlag("priv"))
        {
            mask |= telePointManager.PRIVATE;
        }
        if (context.hasFlag("o"))
        {
            mask |= telePointManager.OWNED;
        }
        if (context.hasFlag("i"))
        {
            mask |= telePointManager.INVITED;
        }

        Set<Warp> warps;
        if (context.getArgCount() == 1)
        {
            User user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", context.getString(0));
                return;
            }
            warps = telePointManager.listWarps(context.getUser(0), mask);
        }
        else if (context.isSender(User.class))
        {
            User user = (User)context.getSender();
            warps = telePointManager.listWarps(user, mask);
        }
        else
        {
            warps = telePointManager.listWarps(mask);
        }

        if (warps.isEmpty())
        {
            context.sendTranslated(MessageType.NEGATIVE, "The query returned no warps!");

        }
        else
        {
            context.sendTranslated(MessageType.NEUTRAL, "Here are the warps:");
            for (Warp warp : warps)
            {
                context.sendMessage(warp.getOwner().getDisplayName() + ":" + warp.getName());
            }
        }
    }
}
