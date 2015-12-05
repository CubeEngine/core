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
package org.cubeengine.service.command.completer;

import java.util.ArrayList;
import java.util.List;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.command.CommandSource;

import static org.cubeengine.module.core.util.StringUtils.startsWithIgnoreCase;

/**
 * A PlayerCompleter for the other online users but not the user sending the command
 */
public class PlayerCompleter implements Completer
{
    private Game game;

    public PlayerCompleter(Game game)
    {
        this.game = game;
    }

    private static boolean canSee(CommandSource sender, Player user)
    {
        // TODO can see
        //return !(sender instanceof User) || ((User)sender).canSee(user.getPlayer().orNull());
        return true;
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        List<String> playerNames = new ArrayList<>();
        final CommandSource sender = (CommandSource)invocation.getCommandSource(); // TODO prevent class cast exceptions
        for (Player player : game.getServer().getOnlinePlayers())
        {
            String name = player.getName();
            if (canSee(sender, player) && startsWithIgnoreCase(name, invocation.currentToken()))
            {
                playerNames.add(name);
            }
        }
        playerNames.remove(sender.getName());
        return playerNames;
    }
}
