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
package org.cubeengine.service.command.readers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.DefaultValue;
import org.cubeengine.butler.parameter.reader.ReaderException;

import org.cubeengine.service.command.CommandManager;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.command.CommandSource;

public class CommandSourceReader implements ArgumentReader<CommandSource>, DefaultValue<CommandSource>, Completer
{
    private final CommandManager cm;
    private Game game;

    public CommandSourceReader(CommandManager cm, Game game)
    {
        this.cm = cm;
        this.game = game;
    }

    @Override
    public CommandSource read(Class type, CommandInvocation invocation) throws ReaderException
    {
        if ("console".equalsIgnoreCase(invocation.currentToken()))
        {
            invocation.consume(1);
            return cm.getConsoleSender();
        }
        return game.getServer().getPlayer(invocation.consume(1)).get();
    }

    @Override
    public CommandSource getDefault(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof Player)
        {
            return (Player)invocation.getCommandSource();
        }
        throw new ReaderException("You need to provide a player");
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        ArrayList<String> list = new ArrayList<>();
        String token = invocation.currentToken().toLowerCase();
        list.addAll(game.getServer().getOnlinePlayers().stream().map(Player::getName).filter(p -> p.startsWith(token)).collect(Collectors.toList()));
        if ("console".startsWith(token))
        {
            list.add("console");
        }
        return list;
    }
}
