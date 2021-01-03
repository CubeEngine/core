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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.cubeengine.libcube.service.command.DefaultParameterProvider;
import org.cubeengine.libcube.service.command.annotation.ParserFor;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommandContext.Builder;
import org.spongepowered.api.command.parameter.Parameter.Key;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.server.ServerWorld;

import static java.util.stream.Collectors.toList;
import static org.cubeengine.libcube.util.ContextUtil.GLOBAL;
import static org.spongepowered.api.service.context.Context.WORLD_KEY;

@Singleton
@ParserFor(Context.class)
public class ContextParser implements ValueParser<Context>, ValueCompleter, DefaultParameterProvider<Context>
{
    private final I18n i18n;

    @Inject
    public ContextParser(I18n i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public Context apply(CommandCause commandCause)
    {
        /*
        if (invocation.getCommandSource() instanceof Player)
        {
            return new Context(WORLD_KEY, ((Player)invocation.getCommandSource()).getWorld().getName());
        }
        */
        return GLOBAL;
    }

    @Override
    public List<String> complete(CommandContext context, String currentInput)
    {
        List<String> list = Sponge.getServer().getWorldManager().worlds().stream()
                                  .map(ServerWorld::getKey)
                                  .map(ResourceKey::asString)
                                  .filter(n -> n.toLowerCase().startsWith(currentInput.toLowerCase()))
                                  .collect(toList());
        if ("global".startsWith(currentInput.toLowerCase()))
        {
            list.add("global");
        }

        if (currentInput.equals("world") || currentInput.toLowerCase().startsWith("world|"))
        {
            String subToken = currentInput.equals("world") ? "" : currentInput.substring(6);
            list.addAll(Sponge.getServer().getWorldManager().worlds().stream()
                              .map(ServerWorld::getKey)
                              .map(ResourceKey::asString)
                              .filter(n -> n.toLowerCase().startsWith(subToken.toLowerCase()))
                              .map(n -> "world|" + n)
                              .collect(toList()));
        }
        return list;
    }

    @Override
    public Optional<? extends Context> getValue(Key<? super Context> parameterKey, Mutable reader, Builder context) throws ArgumentParseException
    {
        String token = reader.parseString();
        String checkToken = token.toLowerCase();
        if (GLOBAL.getKey().equalsIgnoreCase(token))
        {
            return Optional.of(GLOBAL);
        }
        if (token.contains("|"))
        {
            String[] parts = token.split("\\|", 2);
            if (!WORLD_KEY.equals(parts[0]))
            {
                return Optional.of(new Context(parts[0], parts[1]));
            }
            if (!isValidWorld(parts[1]))
            {
                throw reader.createException(i18n.translate(context.getCause(), "Unknown context: {}", token));
            }
            checkToken = parts[1];
        }
        if (isValidWorld(checkToken)) // try for world
        {
            return Optional.of(new Context(WORLD_KEY, checkToken));
        }
        throw reader.createException(i18n.translate(context.getCause(), "Unknown context: {}", token));
    }

    private boolean isValidWorld(String token)
    {
        return Sponge.getServer().getWorldManager().world(ResourceKey.resolve(token)).isPresent();
    }


}
