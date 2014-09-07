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
package de.cubeisland.engine.core.command.parameterized.completer;

import java.util.ArrayList;
import java.util.List;

import de.cubeisland.engine.command.Completer;
import de.cubeisland.engine.command.context.CommandContext;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;

/**
 * A PlayerCompleter for the other online users but not the user sending the command
 */
public class PlayerCompleter implements Completer
{
    private static boolean canSee(CommandSender sender, User user)
    {
        if (sender instanceof User)
        {
            return ((User)sender).canSee(user);
        }
        return true;
    }

    @Override
    public List<String> complete(CommandContext context, String token)
    {
        List<String> playerNames = new ArrayList<>();
        if (context instanceof CubeContext)
        {
            final CommandSender sender = ((CubeContext)context).getSource();
            for (User player : CubeEngine.getUserManager().getOnlineUsers())
            {
                String name = player.getName();
                if (canSee(sender,  player) && startsWithIgnoreCase(name, token))
                {
                    playerNames.add(name);
                }
            }
            playerNames.remove(sender.getName());
        }
        return playerNames;
    }
}
