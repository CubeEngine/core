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

    @Command(desc = "Changes your DisplayName", usage = "<name>|-r", min = 1, max = 1)
    // TODO param change nick of other player /w perm
    // TODO perm to take name of a player that is already Playing on the server
    public void nick(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            String name = context.getString(0);
            if (name.equalsIgnoreCase("-r") || name.equalsIgnoreCase("-reset"))
            {
                ((User)context.getSender()).setDisplayName(context.getSender().getName());
                context.sendTranslated("&aDisplayName reset to &2%s", context.getSender().getName());
            }
            else
            {
                context.sendTranslated("&aDisplayName changed from &2%s&a to &2%s", context.getSender().getDisplayName(), name);
                ((User)context.getSender()).setDisplayName(name);
            }
            return;
        }
        context.sendMessage("&cYou cannot change the consoles DisplayName");
    }
}
