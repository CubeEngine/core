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

import de.cubeisland.engine.core.command.ArgBounds;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.result.confirm.ConfirmResult;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.TelePointManager;
import de.cubeisland.engine.travel.storage.TeleportPointModel;
import de.cubeisland.engine.travel.storage.Warp;

public class WarpAdminCommand extends ContainerCommand
{
    private final TelePointManager tpManager;
    private final Travel module;


    public WarpAdminCommand(Travel module)
    {
        super(module, "admin", "Teleport to a warp");
        this.module = module;
        this.tpManager = module.getTelepointManager();

        this.setUsage("[User] [warp]");
        this.getContextFactory().setArgBounds(new ArgBounds(0, 2));
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        context.sendTranslated(MessageType.NEGATIVE, "This is not a command on it's own.");
        context.sendTranslated(MessageType.NEUTRAL, "If you want to teleport to a users warp: {text:/warp <user>}");
        context.sendTranslated(MessageType.NEUTRAL, "To get a list of the admin commands: {text:/warp admin ?}");
        return null;
    }

    @Alias(names = {
        "clearwarps"
    })
    @Command(desc = "Clear all warps (of an user)", flags = {
        @Flag(name = "pub", longName = "public"),
        @Flag(name = "priv", longName = "Private")
    }, permDefault =  PermDefault.OP, max = 1, usage = " <user> <-public> <-Private>")
    public ConfirmResult clear(final ParameterizedContext context)
    {
        if (this.module.getConfig().clearOnlyFromConsole && !(context.getSender() instanceof ConsoleCommandSender))
        {
            context.sendMessage("You have permission to this command, but it has been disabled from in-game usage for security reasons.");
            return null;
        }
        if (context.getArgCount() > 0)
        {
            if (module.getCore().getUserManager().getUser(context.getString(0), false) == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", context.getString(0));
                return null;
            }
            else
            {
                if (context.hasFlag("pub"))
                {
                    context.sendTranslated(MessageType.NEUTRAL, "Are you sure you want to delete all public warps ever created by {user}?", context
                        .getString(0));
                    context.sendTranslated(MessageType.NEUTRAL, "To delete all the public warps, do: {text:/confirm} before 30 seconds has passed");
                }
                else if (context.hasFlag("priv"))
                {
                    context.sendTranslated(MessageType.NEUTRAL, "Are you sure you want to delete all private warps ever created by {user}?", context
                        .getString(0));
                    context.sendTranslated(MessageType.NEUTRAL, "To delete all the private warps, do: {text:/confirm} before 30 seconds has passed");
                }
                else
                {
                    context.sendTranslated(MessageType.NEUTRAL, "Are you sure you want to delete all warps ever created by {user}?", context.getString(0));
                    context.sendTranslated(MessageType.NEUTRAL, "To delete all the warps, do: {text:/confirm} before 30 seconds has passed");
                }
            }
        }
        else
        {
            if (context.hasFlag("pub"))
            {
                context.sendTranslated(MessageType.NEUTRAL, "Are you sure you want to delete all public warps ever created on this server!?");
                context.sendTranslated(MessageType.NEUTRAL, "To delete all the public warps of every user, do: {text:/confirm} before 30 seconds has passed");
            }
            else if (context.hasFlag("priv"))
            {
                context.sendTranslated(MessageType.NEUTRAL, "Are you sure you want to delete all private warps ever created on this server?");
                context.sendTranslated(MessageType.NEUTRAL, "To delete all the private warps of every user, do: {text:/confirm} before 30 seconds has passed");
            }
            else
            {
                context.sendTranslated(MessageType.NEUTRAL, "Are you sure you want to delete all warps ever created on this server!?");
                context.sendTranslated(MessageType.NEUTRAL, "To delete all the warps of every user, do: {text:/confirm} before 30 seconds has passed");
            }
        }
        return new ConfirmResult(new Runnable()
        {
            @Override
            public void run()
            {
                if (context.getArgCount() == 0)
                { // No user
                    int mask = context.getFlagCount() == 0 ? tpManager.ALL : 0;
                    if (context.hasFlag("pub"))
                    {
                        mask |= tpManager.PUBLIC;
                    }
                    if (context.hasFlag("priv"))
                    {
                        mask |= tpManager.PRIVATE;
                    }
                    tpManager.deleteWarps(mask);
                    context.sendTranslated(MessageType.POSITIVE, "The warps are now deleted");
                }
                else
                {
                    User user = context.getUser(0);
                    int mask = context.getFlagCount() == 0 ? tpManager.ALL : 0;
                    if (context.hasFlag("pub"))
                    {
                        mask |= tpManager.PUBLIC;
                    }
                    if (context.hasFlag("priv"))
                    {
                        mask |= tpManager.PRIVATE;
                    }
                    tpManager.deleteWarps(user, mask);
                    context.sendTranslated(MessageType.POSITIVE, "The warps are now deleted");
                }
            }
        }, context);
    }

    @Command(names = {"privae", "makeprivate"}, permDefault = PermDefault.OP,
             desc = "Make a users warp private", min = 1, max = 1, usage = " owner:home")
    public void makePrivate(CommandContext context)
    {
        Warp warp;
        warp = tpManager.getWarp(context.getString(0));
        if (warp == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Warp {name} not found!", context.getString(0));
            return;
        }
        if (!warp.isPublic())
        {
            context.sendTranslated(MessageType.NEGATIVE, "{name#warp} is already private!", context.getString(0));
            return;
        }
        warp.setVisibility(TeleportPointModel.VISIBILITY_PRIVATE);
        context.sendTranslated(MessageType.POSITIVE, "{name#warp} is now private", context.getString(0));
    }

    @Command(names = {"public"}, permDefault =  PermDefault.OP,
             desc = "Make a users warp public", min = 1, max = 1, usage = " owner:home")
    public void makePublic(CommandContext context)
    {
        Warp warp;
        warp = tpManager.getWarp(context.getString(0));
        if (warp == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Warp {name} not found!", context.getString(0));
            return;
        }
        if (warp.isPublic())
        {
            context.sendTranslated(MessageType.NEGATIVE, "{name#warp} is already public!", context.getString(0));
            return;
        }
        warp.setVisibility(TeleportPointModel.VISIBILITY_PUBLIC);
        context.sendTranslated(MessageType.POSITIVE, "{name#warp} is now public", context.getString(0));
    }
}
