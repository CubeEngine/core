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
package org.cubeengine.libcube.service.command.completer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import static org.cubeengine.libcube.util.StringUtils.startsWithIgnoreCase;

/**
 * A PlayerCompleter for the other online users but not the user sending the command
 */
public class PlayerCompleter implements Completer
{
    private static boolean canSee(CommandSource sender, Player user)
    {
        // TODO can see
        //return !(sender instanceof User) || ((User)sender).canSee(user.getPlayer().orNull());
        return true;
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        Object sender = invocation.getCommandSource();
        boolean isCmdSource = sender instanceof CommandSource;
        String token = invocation.currentToken();
        return Sponge.getServer().getOnlinePlayers().stream()
              .filter(p ->!isCmdSource || canSee(((CommandSource)sender), p)) // Filter can see ; no cmdsource can see everyone
              .filter(p -> sender != p) // Filter self out
              .map(Player::getName) // get Names
              .filter(p -> startsWithIgnoreCase(p, token)) // Filter starting with token
              .collect(Collectors.toList());
    }
}
