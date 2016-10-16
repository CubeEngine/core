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
package org.cubeengine.libcube.service.command.readers;

import java.util.List;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.DefaultValue;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.World;

import static java.util.stream.Collectors.toList;
import static org.cubeengine.libcube.util.ContextUtil.GLOBAL;
import static org.spongepowered.api.service.context.Context.WORLD_KEY;

public class ContextReader implements ArgumentReader<Context>, Completer, DefaultValue<Context>
{
    @Override
    public Context read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String token = invocation.consume(1);
        String checkToken = token.toLowerCase();
        if (GLOBAL.getType().equalsIgnoreCase(token))
        {
            return GLOBAL;
        }
        if (token.contains("|"))
        {
            String[] parts = token.split("\\|", 2);
            if (!WORLD_KEY.equals(parts[0]))
            {
                return new Context(parts[0], parts[1]);
            }
            if (!isValidWorld(parts[1]))
            {
                throw new ReaderException("Unknown context: {}", token);
            }
            checkToken = parts[1];
        }
        if (isValidWorld(checkToken)) // try for world
        {
            return new Context(WORLD_KEY, checkToken);
        }
        throw new ReaderException("Unknown context: {}", token);
    }

    private boolean isValidWorld(String token)
    {
        return Sponge.getServer().getWorld(token).isPresent();
    }

    @Override
    public Context getDefault(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof Player)
        {
            return new Context(WORLD_KEY, ((Player)invocation.getCommandSource()).getWorld().getName());
        }
        return GLOBAL;
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        String token = invocation.currentToken();
        List<String> list = Sponge.getServer().getWorlds().stream()
                .map(World::getName)
                .filter(n -> n.toLowerCase().startsWith(token.toLowerCase()))
                .collect(toList());
        if ("global".startsWith(token.toLowerCase()))
        {
            list.add("global");
        }

        if (token.equals("world") || token.toLowerCase().startsWith("world|"))
        {
            String subToken = token.equals("world") ? "" : token.substring(6);
            list.addAll(Sponge.getServer().getWorlds().stream()
                    .map(World::getName)
                    .filter(n -> n.toLowerCase().startsWith(subToken.toLowerCase()))
                    .map(n -> "world|" + n)
                    .collect(toList()));
        }
        return list;
    }
}
