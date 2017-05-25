/*
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
package org.cubeengine.libcube.service.command.readers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.butler.parameter.argument.ArgumentParser;
import org.cubeengine.butler.parameter.argument.DefaultValue;
import org.cubeengine.butler.parameter.argument.ReaderException;
import org.cubeengine.libcube.util.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class CommandSourceParser implements ArgumentParser<CommandSource>, DefaultValue<CommandSource>, Completer
{
    @Override
    public CommandSource parse(Class type, CommandInvocation invocation) throws ReaderException
    {
        String token = invocation.currentToken();
        if ("console".equalsIgnoreCase(token))
        {
            invocation.consume(1);
            return Sponge.getServer().getConsole();
        }

        Optional<Player> player = Sponge.getServer().getPlayer(token);
        if (!player.isPresent())
        {
            throw new ReaderException("Player {} not found", token);
        }
        invocation.consume(1);
        return player.get();
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
    public List<String> suggest(CommandInvocation invocation)
    {
        ArrayList<String> list = new ArrayList<>();
        String token = invocation.currentToken().toLowerCase();
        list.addAll(Sponge.getServer().getOnlinePlayers().stream().map(Player::getName)
                          .filter(p -> StringUtils.startsWithIgnoreCase(p, token)).collect(Collectors.toList()));
        if ("console".startsWith(token))
        {
            list.add("console");
        }
        return list;
    }
}
