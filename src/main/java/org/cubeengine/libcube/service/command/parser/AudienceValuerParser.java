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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommandContext.Builder;
import org.spongepowered.api.command.parameter.Parameter.Key;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.util.Nameable;

public class AudienceValuerParser implements ValueParser<Audience>, ValueCompleter
{
    @Override
    public List<CommandCompletion> complete(CommandContext context, String currentInput)
    {
        final String token = currentInput.toLowerCase();
        final List<CommandCompletion> list = Sponge.server().onlinePlayers().stream().map(Nameable::name).filter(name -> name.toLowerCase().startsWith(token))
                                                   .map(CommandCompletion::of)
                                                   .collect(Collectors.toList());
        if ("console".startsWith(token))
        {
            list.add(CommandCompletion.of("console"));
        }
        return list;
    }

    @Override
    public Optional<? extends Audience> parseValue(Key<? super Audience> parameterKey, Mutable reader, Builder context) throws ArgumentParseException
    {
        final String name = reader.parseString();
        if ("console".equals(name))
        {
            return Optional.of(Sponge.systemSubject());
        }
        return Sponge.server().player(name);
    }
}
