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
package de.cubeisland.engine.chat;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;

public class ChatCommands
{
    private Chat module;

    public ChatCommands(Chat module)
    {
        this.module = module;
    }

    @Command(desc = "Allows you to emote", min = 1, max = NO_MAX, usage = "<message>")
    public void me(CommandContext context)
    {
        String message = context.getStrings(0);
        this.module.getCore().getUserManager().broadcastStatus(message, context.getSender());
    }

    @Command(desc = "Changes your DisplayName", usage = "<name>|-r [player]", min = 1, max = 2)
    public void nick(CommandContext context)
    {
        User forUser;
        if (context.hasArg(1))
        {
            forUser = context.getUser(1);
            if (forUser == null)
            {
                context.sendTranslated("&cUser &2%s&c not found!", context.getString(1));
                return;
           }
           if (forUser != context.getSender() && !module.perms().COMMAND_NICK_OTHER.isAuthorized(context.getSender()))
           {
               context.sendTranslated("&cYou are not allowed to change the nickname of another player!");
               return;
           }
        }
        else if (context.getSender() instanceof User)
        {
            forUser = (User)context.getSender();
        }
        else
        {
            context.sendMessage("&cYou cannot change the consoles DisplayName");
            return;
        }
        String name = context.getString(0);
        if (name.equalsIgnoreCase("-r") || name.equalsIgnoreCase("-reset"))
        {
            forUser.setDisplayName(context.getSender().getName());
            context.sendTranslated("&aDisplayName reset to &2%s", context.getSender().getName());
        }
        else
        {
            if (module.getCore().getUserManager().getUser(name, false) != null && !module.perms().COMMAND_NICK_OFOTHER.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cThere already is another player named like this!");
                return;
            }
            context.sendTranslated("&aDisplayName changed from &2%s&a to &2%s", context.getSender().getDisplayName(), name);
            ((User)context.getSender()).setDisplayName(name);
        }
    }
}
