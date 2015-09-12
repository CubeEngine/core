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
package org.cubeengine.service.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.completer.Completer;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.command.CommandSource;

import static org.cubeengine.module.core.util.StringUtils.startsWithIgnoreCase;

/**
 * Represents a list of users.
 * If it is all users the list is the currently online users
 */
public class UserList
{
    private final List<Player> list;
    private final boolean all;
    private final Game game;

    public UserList(List<Player> list, Game game)
    {
        this.game = game;
        if (list == null)
        {
            all = true;
            this.list = Collections.emptyList();
        }
        else
        {
            all = false;
            this.list = list;
        }
    }

    public Collection<Player> list()
    {
        if (all)
        {
            return game.getServer().getOnlinePlayers();
        }
        return list;
    }

    public boolean isAll()
    {
        return all;
    }

    public static class UserListReader implements ArgumentReader<UserList>, Completer
    {
        private Game game;

        public UserListReader(Game game)
        {
            this.game = game;
        }

        private static boolean canSee(CommandSource sender, Player user)
        {
            // TODO return !(sender instanceof User) || ((User)sender).canSee(user);
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public UserList read(Class type, CommandInvocation invocation) throws ReaderException
        {
            if ("*".equals(invocation.currentToken()))
            {
                invocation.consume(1);
                return new UserList(null, game);
            }
            return new UserList((List<Player>)invocation.getManager().read(List.class, Player.class, invocation), game);
        }

        @Override
        public List<String> getSuggestions(CommandInvocation invocation)
        {
            List<String> list = new ArrayList<>();
            if (invocation.currentToken().isEmpty())
            {
                list.add("*");
            }

            final CommandSource sender = invocation.getContext(CommandSource.class);
            for (Player player : game.getServer().getOnlinePlayers())
            {
                String name = player.getName();
                if (canSee(sender,  player) && startsWithIgnoreCase(name, invocation.currentToken()))
                {
                    list.add(name);
                }
            }
            list.remove(sender.getName());
            // TODO complete actual lists
            return list;
        }
    }
}
