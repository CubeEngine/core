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

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandHolder;
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
        User target = getTargetUser(context);
        if (target == null)
        {
            return;
        }
        this.module.hidePlayer(target, true);
    }

    @Command(desc = "Unhides a player.", usage = "{player}", max = 1)
    public void unhide(CommandContext context)
    {
        User target = getTargetUser(context);
        if (target == null)
        {
            return;
        }
    }

    @Command(desc = "Checks whether a player is hidden.", usage = "{player}", max = 1)
    public void hidden(CommandContext context)
    {
        User target = getTargetUser(context);
        if (target == null)
        {
            return;
        }
    }

    @Command(desc = "Lists all hidden players.")
    public void listhiddens(CommandContext context)
    {

    }

    @Command(desc = "Toggles the ability to see hidden players.", usage = "{player}", max = 1)
    public void seehiddens(CommandContext context)
    {
        User target = getTargetUser(context);
        if (target == null)
        {
            return;
        }
    }

    @Command(desc = "Checks whether a player can see hidden players.", usage = "{player}", max = 1)
    public void canseehiddens(CommandContext context)
    {
        User target = getTargetUser(context);
        if (target == null)
        {
            return;
        }
    }

    @Command(desc = "Lists all players who can see hidden players.")
    public void listcanseehiddens(CommandContext context)
    {
        context.sendTranslated("&aThe following players can see hidden players:");
        for (User user : this.module.getCanSeeHiddens())
        {
            context.sendMessage(" - &e" + user.getDisplayName());
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
