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
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import static org.cubeengine.module.core.util.StringUtils.startsWithIgnoreCase;

/**
 * Represents a list of users.
 * If it is all users the list is the currently online users
 */
// TODO use User instead of Player
public class UserList
{
    private final List<Player> list;
    private final boolean all;

    public UserList(List<Player> list)
    {
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
            return Sponge.getServer().getOnlinePlayers();
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
            if (sender instanceof Player)
            {
                // TODO can see other???
                return !((Player) sender).get(Keys.INVISIBLE).orElse(false);
            }
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public UserList read(Class type, CommandInvocation invocation) throws ReaderException
        {
            if ("*".equals(invocation.currentToken()))
            {
                invocation.consume(1);
                return new UserList(null);
            }
            return new UserList((List<Player>)invocation.getManager().read(List.class, User.class, invocation));
        }

        @Override
        public List<String> getSuggestions(CommandInvocation invocation)
        {
            List<String> list = new ArrayList<>();

            final CommandSource sender = invocation.getContext(CommandSource.class);
            for (Player player : game.getServer().getOnlinePlayers())
            {
                String name = player.getName();
                if (canSee(sender,  player) && startsWithIgnoreCase(name, invocation.currentToken()))
                {
                    list.add(name);
                }
            }

            if (invocation.currentToken().isEmpty())
            {
                list.add("*");
            }

            list.remove(sender.getName());
            // TODO complete actual lists
            return list;
        }
    }
}
