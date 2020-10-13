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
package org.cubeengine.libcube.service.command;


import org.cubeengine.libcube.service.command.parser.ServerWorldValueParser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameters;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ParameterRegistry
{
    private static final Map<Class, Supplier<ValueParser<?>>> parsers = new HashMap<>();
    private static final Map<Class, Supplier<ValueCompleter>> completers = new HashMap<>();
    private static final Map<Class, Supplier<DefaultParameterProvider<?>>> defaultProviders = new HashMap<>();

    public static void register(Class<?> clazz, Object valueParameter)
    {
        if (valueParameter instanceof ValueParser) {
            parsers.put(clazz, () -> ((ValueParser<?>) valueParameter));
            parsers.put(valueParameter.getClass(), () -> ((ValueParser<?>) valueParameter));
        }
        if (valueParameter instanceof ValueCompleter) {
            completers.put(clazz, () -> (ValueCompleter) valueParameter);
            completers.put(valueParameter.getClass(), () -> ((ValueCompleter) valueParameter));
        }
        if (valueParameter instanceof DefaultParameterProvider) {
            defaultProviders.put(clazz, () -> (DefaultParameterProvider) valueParameter);
            defaultProviders.put(valueParameter.getClass(), () -> ((DefaultParameterProvider) valueParameter));
        }
    }
    private static <T> void registerSponge(Class<T> clazz, Supplier<CatalogedValueParameter<T>> valueParameter)
    {
        parsers.put(clazz, () -> valueParameter.get());
        completers.put(clazz, () -> valueParameter.get());
    }

    static
    {
        register(ServerWorld.class, new ServerWorldValueParser());
        registerSponge(String.class, CatalogedValueParameters.STRING);
    }

    static ValueParser<?> getParser(Class<?> type, boolean last)
    {
        if (type == String.class && last)
        {
            return CatalogedValueParameters.REMAINING_JOINED_STRINGS.get();
        }
        final Supplier<ValueParser<?>> parser = parsers.get(type);
        if (parser != null)
        {
            return parser.get();
        }
        throw new IllegalArgumentException("No parser was registered for " + type);
    }

    static ValueCompleter getCompleter(Class<?> type)
    {
        final Supplier<ValueCompleter> completer = completers.get(type);
        if (completer != null) {
            return completer.get();
        }
        return null;
    }

    static <T> DefaultParameterProvider<T> getDefaultProvider(Class<?> type)
    {
        final Supplier<DefaultParameterProvider<T>> completer = (Supplier) defaultProviders.get(type);
        if (completer != null) {
            return completer.get();
        }
        throw new IllegalArgumentException("No default provider was registered for " + type);
    }
}
