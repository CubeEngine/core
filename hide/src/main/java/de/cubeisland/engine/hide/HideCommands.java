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
package de.cubeisland.engine.hide;

import java.util.Set;
import java.util.UUID;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandHolder;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class HideCommands implements CommandHolder
{
    private final Hide module;

    public HideCommands(Hide module)
    {
        this.module = module;
    }

    @Override
    public Class<? extends CubeCommand> getCommandType()
    {
        return ReflectedCommand.class;
    }

    @Command(desc = "Hides a player.", usage = "(player)", max = 1)
    public void hide(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User target = getTargetUser(context);
        if (target == null)
        {
            return;
        }
        if (!this.module.isHidden(target))
        {
            this.module.hidePlayer(target);
            if (target == sender)
            {
                target.sendTranslated(MessageType.POSITIVE, "You are now hidden!");
            }
            else
            {
                target.sendTranslated(MessageType.POSITIVE, "You were hidden by {sender}!", sender);
                sender.sendTranslated(MessageType.POSITIVE, "{user} is now hidden!", target);
            }
        }
        else
        {
            if (target == sender)
            {
                target.sendTranslated(MessageType.NEUTRAL, "You are already hidden!");
            }
            else
            {
                sender.sendTranslated(MessageType.NEUTRAL, "{user} is already hidden!", target);
            }
        }
    }

    @Command(desc = "Unhides a player.", usage = "(player)", max = 1)
    public void unhide(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User target = getTargetUser(context);
        if (target == null)
        {
            return;
        }
        if (this.module.isHidden(target))
        {
            this.module.showPlayer(target);
            if (target == sender)
            {
                target.sendTranslated(MessageType.POSITIVE, "You are now visible!");
            }
            else
            {
                target.sendTranslated(MessageType.POSITIVE, "You were unhidden by {sender}!", sender);
                sender.sendTranslated(MessageType.POSITIVE, "{user} is now visible!", target);
            }
        }
        else
        {
            if (target == sender)
            {
                target.sendTranslated(MessageType.NEUTRAL, "You are already visible!");
            }
            else
            {
                sender.sendTranslated(MessageType.NEUTRAL, "{user} is already visible!", target);
            }
        }
    }

    @Command(desc = "Checks whether a player is hidden.", usage = "(player)", max = 1)
    public void hidden(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User target = getTargetUser(context);
        if (target == null)
        {
            return;
        }
        if (this.module.isHidden(target))
        {
            if (target == sender)
            {
                context.sendTranslated(MessageType.POSITIVE, "You are currently hidden!");
            }
            else
            {
                context.sendTranslated(MessageType.POSITIVE, "{user} is currently hidden!", target.getDisplayName());
            }
        }
        else
        {
            if (target == sender)
            {
                context.sendTranslated(MessageType.NEUTRAL, "You are currently visible!");
            }
            else
            {
                context.sendTranslated(MessageType.NEUTRAL, "{user} is currently visible!", target.getDisplayName());
            }
        }
        this.module.getHiddenUsers().contains(target.getUniqueId());
    }

    @Command(desc = "Lists all hidden players.")
    public void listhiddens(CommandContext context)
    {
        Set<UUID> hiddens = this.module.getHiddenUsers();
        if (hiddens.isEmpty())
        {
            context.sendTranslated(MessageType.NEUTRAL, "There are no hidden users!");
            return;
        }
        context.sendTranslated(MessageType.POSITIVE, "The following users are hidden:");
        for (UUID name : hiddens)
        {
            context.sendMessage(" - " + ChatFormat.YELLOW + context.getCore().getUserManager().getExactUser(name).getDisplayName());
        }
    }

    @Command(desc = "Toggles the ability to see hidden players.", usage = "(player)", max = 1)
    public void seehiddens(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User target = getTargetUser(context);
        if (target == null)
        {
            return;
        }

        if (this.module.toggleCanSeeHiddens(target))
        {
            if (target == sender)
            {
                context.sendTranslated(MessageType.POSITIVE, "You can now see hidden users!");
            }
            else
            {
                target.sendTranslated(MessageType.POSITIVE, "You can now see hidden users! (Enabled by {sender})", sender);
                context.sendTranslated(MessageType.NEUTRAL, "{user} can now see hidden users!", target);
            }
        }
        else
        {
            if (target == sender)
            {
                context.sendTranslated(MessageType.POSITIVE, "You can no longer see hidden users!");
            }
            else
            {
                target.sendTranslated(MessageType.POSITIVE, "You can no longer see hidden users! (Disabled by {sender})", sender);
                context.sendTranslated(MessageType.NEUTRAL, "{user} can no longer see hidden users!", target);
            }
        }
    }

    @Command(desc = "Checks whether a player can see hidden players.", usage = "(player)", max = 1)
    public void canseehiddens(CommandContext context)
    {
        CommandSender sender = context.getSender();
        User target = getTargetUser(context);
        if (target == null)
        {
            return;
        }
        if (this.module.canSeeHiddens(target))
        {
            if (target == sender)
            {
                context.sendTranslated(MessageType.POSITIVE, "You can currently see hidden users!");
            }
            else
            {
                context.sendTranslated(MessageType.POSITIVE, "{user} can currently see hidden users!", target);
            }
        }
        else
        {
            if (target == sender)
            {
                context.sendTranslated(MessageType.NEUTRAL, "You can't see hidden players!");
            }
            else
            {
                context.sendTranslated(MessageType.NEUTRAL, "{user} can't see hidden players!", target);
            }
        }
    }

    @Command(desc = "Lists all players who can see hidden players.")
    public void listcanseehiddens(CommandContext context)
    {
        Set<UUID> canSeeHiddens = this.module.getCanSeeHiddens();
        if (canSeeHiddens.isEmpty())
        {
            context.sendTranslated(MessageType.NEUTRAL, "No users can currently see hidden users!");
            return;
        }
        context.sendTranslated(MessageType.POSITIVE, "The following players can see hidden players:");
        for (UUID canSee : canSeeHiddens)
        {
            context.sendMessage(" - " + ChatFormat.YELLOW + context.getCore().getUserManager().getExactUser(canSee).getDisplayName());
        }
    }

    private static User getTargetUser(CommandContext context)
    {
        if (context.getArgCount() > 0)
        {
            User target = context.getUser(0);
            if (target == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Player {user} not found!", context.getString(0));
                return null;
            }
            return target;
        }
        else if (context.getSender() instanceof User)
        {
            return (User)context.getSender();
        }
        else
        {
            context.sendTranslated(MessageType.NEGATIVE, "No player specified!");
            return null;
        }
    }
}
