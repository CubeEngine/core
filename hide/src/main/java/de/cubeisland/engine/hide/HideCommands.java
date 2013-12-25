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

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandHolder;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.user.User;

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

    @Command(desc = "Hides a player.", usage = "{player}", max = 1)
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
                target.sendTranslated("&aYou are now hidden!");
            }
            else
            {
                target.sendTranslated("&aYou were hidden by &e%s&a!", sender.getDisplayName());
                sender.sendTranslated("&a%s&a is now hidden!", target.getDisplayName());
            }
        }
        else
        {
            if (target == sender)
            {
                target.sendTranslated("&eYou are already hidden!");
            }
            else
            {
                sender.sendTranslated("&e%s&a is already hidden!", target.getDisplayName());
            }
        }
    }

    @Command(desc = "Unhides a player.", usage = "{player}", max = 1)
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
                target.sendTranslated("&aYou are now visible!");
            }
            else
            {
                target.sendTranslated("&aYou were unhidden by &e%s&a!", sender.getDisplayName());
                sender.sendTranslated("&a%s&a is now visible!", target.getDisplayName());
            }
        }
        else
        {
            if (target == sender)
            {
                target.sendTranslated("&eYou are already visible!");
            }
            else
            {
                sender.sendTranslated("&e%s&a is already visible!", target.getDisplayName());
            }
        }
    }

    @Command(desc = "Checks whether a player is hidden.", usage = "{player}", max = 1)
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
                context.sendTranslated("&aYou are hidden right now!");
            }
            else
            {
                context.sendTranslated("&a%s&a is hidden right now!", target.getDisplayName());
            }
        }
        else
        {
            if (target == sender)
            {
                context.sendTranslated("&eYou are visible right now!");
            }
            else
            {
                context.sendTranslated("&e%s&a is visible right now!", target.getDisplayName());
            }
        }
        this.module.getHiddenUsers().contains(target.getName());
    }

    @Command(desc = "Lists all hidden players.")
    public void listhiddens(CommandContext context)
    {
        Set<String> hiddens = this.module.getHiddenUsers();
        if (hiddens.isEmpty())
        {
            context.sendTranslated("&eThere are no hidden users!");
            return;
        }
        context.sendTranslated("&aThe following users are hidden:");
        for (String name : hiddens)
        {
            context.sendMessage(" - &e" + context.getCore().getUserManager().getExactUser(name).getDisplayName());
        }
    }

    @Command(desc = "Toggles the ability to see hidden players.", usage = "{player}", max = 1)
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
                context.sendTranslated("&aYou can now see hidden users!");
            }
            else
            {
                target.sendTranslated("&aYou can now see hidden users! (Enabled by &e%s&a)", sender.getDisplayName());
                context.sendTranslated("&e%s&a can now see hidden users!", target.getDisplayName());
            }
        }
        else
        {
            if (target == sender)
            {
                context.sendTranslated("&aYou can no longer see hidden users!");
            }
            else
            {
                target.sendTranslated("&aYou can no longer see hidden users! (Disabled by &e%s&a)", sender.getDisplayName());
                context.sendTranslated("&e%s&a can no longer see hidden users!", target.getDisplayName());
            }
        }
    }

    @Command(desc = "Checks whether a player can see hidden players.", usage = "{player}", max = 1)
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
                context.sendTranslated("&aYou can currently see hidden users!");
            }
            else
            {
                context.sendTranslated("&a%s&a can currently see hidden users!", target.getDisplayName());
            }
        }
        else
        {
            if (target == sender)
            {
                context.sendTranslated("&eYou can't see hidden players!");
            }
            else
            {
                context.sendTranslated("&e%s&a can't see hidden players!", target.getDisplayName());
            }
        }
    }

    @Command(desc = "Lists all players who can see hidden players.")
    public void listcanseehiddens(CommandContext context)
    {
        Set<String> canSeeHiddens = this.module.getCanSeeHiddens();
        if (canSeeHiddens.isEmpty())
        {
            context.sendTranslated("&eNo users can currently see hidden users!");
            return;
        }
        context.sendTranslated("&aThe following players can see hidden players:");
        for (String user : canSeeHiddens)
        {
            context.sendMessage(" - &e" + context.getCore().getUserManager().getExactUser(user).getDisplayName());
        }
    }

    private static User getTargetUser(CommandContext context)
    {
        if (context.getArgCount() > 0)
        {
            User target = context.getUser(0);
            if (target == null)
            {
                context.sendTranslated("&cCouldn't find the user &e%s&c...", context.getString(0));
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
            context.sendTranslated("&cNo user specified!");
            return null;
        }
    }
}
