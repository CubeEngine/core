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

import org.bukkit.Location;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.command.result.confirm.ConfirmResult;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.Warp;
import de.cubeisland.engine.travel.storage.WarpManager;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PRIVATE;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

public class WarpCommand extends ContainerCommand
{
    private final Travel module;
    private final WarpManager manager;

    public WarpCommand(Travel module)
    {
        super(module, "warp", "Teleport to a warp");
        this.getContextFactory().removeLastIndexed();
        this.addIndexed(new CommandParameterIndexed(new String[]{"warp"}, String.class, false, true, 1));
        this.addIndexed(new CommandParameterIndexed(new String[]{"owner"}, User.class, false, true, 1));
        this.module = module;
        this.manager = module.getWarpManager();
    }

    @Override
    public CommandResult run(CommandContext context)
    {
        if (context.isSender(User.class) && context.getArgCount() > 0)
        {
            User sender = (User)context.getSender();
            Warp warp = manager.find(sender, context.getString(0));
            if (warp == null)
            {
                context.sendTranslated(NEGATIVE, "You do not have access to any warp with that name");
                return null;
            }
            Location location = warp.getLocation();
            if (location == null)
            {
                context.sendTranslated(NEGATIVE, "This warp is in a world that no longer exists!");
                return null;
            }
            if (sender.teleport(location, COMMAND))
            {
                if (warp.getWelcomeMsg() != null)
                {
                    context.sendMessage(warp.getWelcomeMsg());
                }
                else
                {
                    if (warp.isOwner(sender))
                    {
                        context.sendTranslated(POSITIVE, "You have been teleported to your home {name}!", warp.getName());
                    }
                    else
                    {
                        context.sendTranslated(POSITIVE, "You have been teleported to the home {name} of {user}!", warp.getName(), warp.getOwnerName());
                    }
                }
                return null;
            }
            context.sendTranslated(CRITICAL, "The teleportation got aborted!");
            return null;
        }
        return super.run(context);
    }

    @Alias(names = {"createwarp", "mkwarp", "makewarp"})
    @Command(names = {"create", "make"}, desc = "Create a warp",
             flags = {@Flag(name = "priv", longName = "private")},
             indexed = @Grouped(@Indexed("name")))
    public void createWarp(ParameterizedContext context)
    {
        if (this.manager.getCount() >= this.module.getConfig().warps.max)
        {
            context.sendTranslated(CRITICAL, "The server have reached its maximum number of warps!");
            context.sendTranslated(NEGATIVE, "Some warps must be deleted for new ones to be made");
            return;
        }
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(CRITICAL, "This command can only be used by users!");
        }
        User sender = (User)context.getSender();
        String name = context.getString(0);
        if (manager.has(sender, name))
        {
            context.sendTranslated(NEGATIVE, "A warp by that name already exist!");
            return;
        }
        if (name.contains(":") || name.length() >= 32)
        {
            context.sendTranslated(NEGATIVE, "Warps may not have names that are longer than 32 characters or contain colon(:)'s!");
            return;
        }
        Location loc = sender.getLocation();
        Warp warp = manager.create(sender, name, loc, !context.hasFlag("priv"));
        context.sendTranslated(POSITIVE, "Your warp {name} has been created!", warp.getName());
    }

    @Alias(names = {"removewarp", "deletewarp", "delwarp", "remwarp"})
    @Command(names = {"remove", "delete"}, desc = "Remove a warp",
             indexed = @Grouped(@Indexed("warp")))
    public void removeWarp(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            return; // TODO
        }
        User sender = (User)context.getSender();
        String name = context.getString(0);
        Warp warp = manager.find(sender, name);
        if (warp == null || !warp.isOwner(sender))
        {
            sender.sendTranslated(NEGATIVE, "You do not own a warp named {name#warp}!", name);
            return;
        }
        manager.delete(warp);
        sender.sendTranslated(POSITIVE, "Your warp named {name} has been removed", name);
    }

    @Command(desc = "Rename a warp",
             indexed = {@Grouped(@Indexed("warp")),
                        @Grouped(@Indexed("new name"))})
    public void rename(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            return; // TODO
        }
        User sender = (User)context.getSender();
        String name = context.getString(0);
        Warp warp = manager.find(sender, name);
        if (warp == null || !warp.isOwner(sender))
        {
            context.sendTranslated(NEGATIVE, "You do not own a warp named {name#warp}!", name);
            return;
        }
        String newName = context.getString(1);
        if (name.contains(":") || name.length() >= 32)
        {
            context.sendTranslated(NEGATIVE, "Warps may not have names that are longer than 32 characters or contain colon(:)'s!");
            return;
        }

        if (manager.rename(warp, newName))
        {
            context.sendTranslated(POSITIVE, "The warp {name} has been renamed too {name}", name, newName);
            return;
        }
        context.sendTranslated(POSITIVE, "Could not rename the warp to {name}", newName);
    }

    @Command(desc = "Move a warp", indexed = @Grouped(@Indexed("warp")))
    public void move(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            return;
        }
        User sender = (User)context.getSender();
        String name = context.getString(0);
        Warp warp = manager.find(sender, name);
        if (warp == null || !warp.isOwner(sender))
        {
            context.sendTranslated(NEGATIVE, "You do not own a warp named {name#warp}!", name);
            return;
        }
        warp.setLocation(sender.getLocation());
        warp.update();
        context.sendTranslated(POSITIVE, "The warp is now moved to your current location");
    }

    @Command(desc = "List all available warps",
             flags = {@Flag(name = "pub", longName = "public"),
                      @Flag(name = "o", longName = "owned"),
                      @Flag(name = "i", longName = "invited")})
    public void list(ParameterizedContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            return;
            // TODO
        }
        User user = (User)context.getSender();
        Set<Warp> warps = this.manager.list(user, context.hasFlag("o"), context.hasFlag("pub"), context.hasFlag("i"));
        if (warps.isEmpty())
        {
            user.sendTranslated(NEGATIVE, "No warps are available to you!");
            return;
        }
        user.sendTranslated(NEUTRAL, "The following warps are available to you:");
        for (Warp warp : warps)
        {
            if (warp.isPublic())
            {
                if (warp.isOwner(user))
                {
                    user.sendTranslated(NEUTRAL, "  {name#warp} ({text:public})", warp.getName());
                }
                else
                {
                    user.sendTranslated(NEUTRAL, "  {user}:{name#warp} ({text:public})", warp.getOwnerName(), warp.getName());
                }
            }
            else
            {
                if (warp.isOwner(user))
                {
                    user.sendTranslated(NEUTRAL, "  {name#warp} ({text:private})", warp.getName());
                }
                else
                {
                    user.sendTranslated(NEUTRAL, "  {user}:{name#warp} ({text:private})", warp.getOwnerName(), warp.getName());
                }
            }
        }
    }

    @Command(names = {"private", "makeprivate"}, desc = "Make a players warp private",
             indexed = @Grouped(@Indexed("[owner:]warp")))
    public void makePrivate(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(CRITICAL, "This command can only be used by players!");
            return;
        }
        User sender = (User)context.getSender();
        String name = context.getString(0);
        Warp warp = this.manager.find(sender, name);
        if (warp == null || !warp.isOwner(sender))
        {
            sender.sendTranslated(NEGATIVE, "You do not own a warp named {name#home}!", name);
            return;
        }
        if (!warp.isPublic())
        {
            sender.sendTranslated(NEGATIVE, "This warp is already private!");
            return;
        }
        warp.setVisibility(VISIBILITY_PRIVATE);
        context.sendTranslated(POSITIVE, "Your warp {name} is now private", name);
    }

    @Command(names = "public", desc = "Make a users warp public",
             indexed = @Grouped(@Indexed("[owner:]warp")))
    public void makePublic(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(CRITICAL, "This command can only be used by players!");
            return;
        }
        User sender = (User)context.getSender();
        String name = context.getString(0);
        Warp warp = this.manager.find(sender, name);
        if (warp == null || !warp.isOwner(sender))
        {
            sender.sendTranslated(NEGATIVE, "You do not own a warp named {name#home}!", name);
            return;
        }
        if (warp.isPublic())
        {
            sender.sendTranslated(NEGATIVE, "This warp is already public!");
            return;
        }
        warp.setVisibility(VISIBILITY_PUBLIC);
        context.sendTranslated(POSITIVE, "Your warp {name} is now public", name);
    }

    @Alias(names = {"clearwarps"})
    @Command(desc = "Clear all warps (of a player)", flags = {
        @Flag(name = "pub", longName = "public"),
        @Flag(name = "priv", longName = "private")},
             indexed = @Grouped(req = false, value = @Indexed("user")))
    public ConfirmResult clear(final ParameterizedContext context)
    {
        if (this.module.getConfig().clearOnlyFromConsole && !(context.getSender() instanceof ConsoleCommandSender))
        {
            context.sendMessage("You have permission to this command, but it has been disabled from ingame usage for security reasons.");
            return null;
        }
        if (context.getArgCount() > 0)
        {
            if (module.getCore().getUserManager().findExactUser(context.getString(0)) == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
                return null;
            }
            else
            {
                if (context.hasFlag("pub"))
                {
                    context.sendTranslated(NEUTRAL, "Are you sure you want to delete all public warps ever created by {user}?", context
                        .getString(0));
                    context.sendTranslated(NEUTRAL, "To delete all the public warps, do: {text:/confirm} before 30 seconds has passed");
                }
                else if (context.hasFlag("priv"))
                {
                    context.sendTranslated(NEUTRAL, "Are you sure you want to delete all private warps ever created by {user}?", context
                        .getString(0));
                    context.sendTranslated(NEUTRAL, "To delete all the private warps, do: {text:/confirm} before 30 seconds has passed");
                }
                else
                {
                    context.sendTranslated(NEUTRAL, "Are you sure you want to delete all warps ever created by {user}?", context.getString(0));
                    context.sendTranslated(NEUTRAL, "To delete all the warps, do: {text:/confirm} before 30 seconds has passed");
                }
            }
        }
        else
        {
            if (context.hasFlag("pub"))
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all public warps ever created on this server!?");
                context.sendTranslated(NEUTRAL, "To delete all the public warps of every player, do: {text:/confirm} before 30 seconds has passed");
            }
            else if (context.hasFlag("priv"))
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all private warps ever created on this server?");
                context.sendTranslated(NEUTRAL, "To delete all the private warps of every player, do: {text:/confirm} before 30 seconds has passed");
            }
            else
            {
                context.sendTranslated(NEUTRAL, "Are you sure you want to delete all warps ever created on this server!?");
                context.sendTranslated(NEUTRAL, "To delete all the warps of every player, do: {text:/confirm} before 30 seconds has passed");
            }
        }
        return new ConfirmResult(new Runnable()
        {
            @Override
            public void run()
            {
                if (context.getArgCount() == 0)
                { // No user
                    manager.massDelete(context.hasFlag("priv"), context.hasFlag("pub"));
                    context.sendTranslated(POSITIVE, "The warps are now deleted");
                }
                else
                {
                    User user = context.getUser(0);
                    manager.massDelete(user, context.hasFlag("priv"), context.hasFlag("pub"));
                    context.sendTranslated(POSITIVE, "Deleted warps.");
                }
            }
        }, context);
    }
}
