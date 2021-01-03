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
package org.cubeengine.libcube.service.command.parser;

import net.kyori.adventure.text.Component;
import org.cubeengine.libcube.service.command.DefaultParameterProvider;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServerWorldValueParser implements ValueParser<ServerWorld>, ValueCompleter, DefaultParameterProvider<ServerWorld>
{

    @Override
    public Optional<? extends ServerWorld> getValue(Parameter.Key<? super ServerWorld> parameterKey, ArgumentReader.Mutable reader,
            CommandContext.Builder context) throws ArgumentParseException
    {
        final ResourceKey key = reader.parseResourceKey("minecraft");
        final Optional<ServerWorld> world = Sponge.getServer().getWorldManager().world(key);
        if (!world.isPresent())
        {
            throw reader.createException(Component.text("World " + key + " does not exist."));
        }
        return world;
    }

    @Override
    public List<ClientCompletionType> getClientCompletionType()
    {
        return Arrays.asList(ClientCompletionTypes.RESOURCE_KEY.get());
    }

    @Override
    public List<String> complete(CommandContext context, String currentInput)
    {
        final List<String> list = new ArrayList<>();
        Sponge.getServer().getWorldManager().worlds().stream()
                     .map(ServerWorld::getKey)
                     .filter(k -> k.getFormatted().startsWith(currentInput) || k.getNamespace().equals("minecraft") && k.getValue().startsWith(currentInput))
                     .forEach(k -> {
                         list.add(k.getFormatted());
                         if (k.getNamespace().equals("minecraft"))
                         {
                             list.add(k.getValue());
                         }
                     });
        return list;
    }

    @Override
    public ServerWorld apply(CommandCause commandCause)
    {
        if (commandCause.getAudience() instanceof ServerPlayer)
        {
            return ((ServerPlayer)commandCause.getAudience()).getWorld();
        }
        return null;
    }
}
