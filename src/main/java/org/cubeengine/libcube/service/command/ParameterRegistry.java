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


import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.google.inject.Injector;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.audience.Audience;
import org.cubeengine.libcube.service.command.parser.AudienceValuerParser;
import org.cubeengine.libcube.service.command.parser.ServerPlayerDefaultParameterProvider;
import org.cubeengine.libcube.service.command.parser.ServerWorldValueParser;
import org.cubeengine.libcube.service.command.parser.StringListParser;
import org.cubeengine.libcube.service.command.parser.UserDefaultParameterProvider;
import org.cubeengine.libcube.service.command.parser.Vector2iValueParser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.standard.ResourceKeyedValueParameters;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.weather.WeatherType;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3d;

public class ParameterRegistry
{
    private static final Map<Type, Supplier<ValueParser<?>>> parsers = new HashMap<>();
    private static final Map<Type, Supplier<ValueCompleter>> completers = new HashMap<>();
    private static final Map<Type, Supplier<DefaultParameterProvider<?>>> defaultProviders = new HashMap<>();

    public static void register(Type type, Object valueParameter)
    {
        final Type valueParameterType = valueParameter.getClass();
        if (valueParameter instanceof ValueParser) {
            parsers.put(type, () -> ((ValueParser<?>) valueParameter));
            parsers.put(valueParameterType, () -> ((ValueParser<?>) valueParameter));
        }
        if (valueParameter instanceof ValueCompleter) {
            completers.put(type, () -> (ValueCompleter) valueParameter);
            completers.put(valueParameterType, () -> ((ValueCompleter) valueParameter));
        }
        if (valueParameter instanceof DefaultParameterProvider) {
            defaultProviders.put(type, () -> (DefaultParameterProvider) valueParameter);
            defaultProviders.put(valueParameterType, () -> ((DefaultParameterProvider) valueParameter));
        }
    }

    private static <VP extends ValueParser<T> & ValueCompleter,T> void registerSponge(Type type, Supplier<VP> valueParameter)
    {
        parsers.put(type, () -> valueParameter.get());
        completers.put(type, () -> valueParameter.get());
    }

    static
    {
        register(ServerWorld.class, new ServerWorldValueParser());
        register(Audience.class, new AudienceValuerParser());
        registerSponge(String.class, ResourceKeyedValueParameters.STRING);
        registerSponge(ServerPlayer.class, ResourceKeyedValueParameters.PLAYER);
        registerSponge(User.class, ResourceKeyedValueParameters.USER);
        registerSponge(Boolean.class, ResourceKeyedValueParameters.BOOLEAN);
        final DefaultParameterProvider<Boolean> booleanDefaultProvider = c -> false;
        register(Boolean.class, booleanDefaultProvider);
        register(boolean.class, booleanDefaultProvider);
        registerSponge(boolean.class, ResourceKeyedValueParameters.BOOLEAN);
        registerSponge(Integer.class, ResourceKeyedValueParameters.INTEGER);
        registerSponge(int.class, ResourceKeyedValueParameters.INTEGER);
        registerSponge(Double.class, ResourceKeyedValueParameters.DOUBLE);
        registerSponge(double.class, ResourceKeyedValueParameters.DOUBLE);
        register(ServerPlayer.class, new ServerPlayerDefaultParameterProvider());
        register(User.class, new UserDefaultParameterProvider());
        registerSponge(Vector3d.class, ResourceKeyedValueParameters.VECTOR3D);
        register(Vector2i.class, new Vector2iValueParser());
        registerSponge(ItemType.class, () -> registryTypeParser("minecraft", RegistryTypes.ITEM_TYPE));
        registerSponge(BlockType.class, () -> registryTypeParser("minecraft", RegistryTypes.BLOCK_TYPE));
        registerSponge(EntityType.class, () -> registryTypeParser("minecraft", RegistryTypes.ENTITY_TYPE));
        registerSponge(Difficulty.class, () -> registryTypeParser("sponge", RegistryTypes.DIFFICULTY));
        registerSponge(GameMode.class, () -> registryTypeParser("sponge", RegistryTypes.GAME_MODE));
        registerSponge(EnchantmentType.class, () -> registryTypeParser("minecraft", RegistryTypes.ENCHANTMENT_TYPE));
        registerSponge(WeatherType.class, () -> registryTypeParser("sponge", RegistryTypes.WEATHER_TYPE));
        registerSponge(ItemStackSnapshot.class, ResourceKeyedValueParameters.ITEM_STACK_SNAPSHOT);

        register((new TypeToken<List<String>>() {}).getType(), new StringListParser());
        registerSponge((new TypeToken<Collection<Entity>>() {}).getType(), ResourceKeyedValueParameters.MANY_ENTITIES);
        registerSponge((new TypeToken<Collection<ServerPlayer>>() {}).getType(), ResourceKeyedValueParameters.MANY_PLAYERS);
        registerSponge((new TypeToken<Collection<GameProfile>>() {}).getType(), ResourceKeyedValueParameters.MANY_GAME_PROFILES);
    }

    private static <T> ValueParameter<T> registryTypeParser(String defaultNameSpace, DefaultedRegistryType<T> registryType)
    {
        return VariableValueParameters.registryEntryBuilder(c -> Sponge.game().registries(), registryType).defaultNamespace(defaultNameSpace).build();
    }

    static <T> ValueParser<T> getParser(Injector injector, Type type, Class<? extends ValueParser<T>> parserType, boolean last, boolean greedy)
    {
        if (parserType != null)
        {
            final Supplier<ValueParser<?>> parser = parsers.get(parserType);
            if (parser != null)
            {
                return (ValueParser<T>) parser.get();
            }
            if (ValueParser.class.isAssignableFrom(parserType))
            {
                final ValueParser<T> instance = injector.getInstance(parserType);
                parsers.put(parserType, () -> instance);
                return instance;
            }
            throw new IllegalStateException("ValueParser cannot be created! " + parserType);
        }

        final Supplier<ValueParser<?>> parser = parsers.get(type);
        if (parser != null)
        {
            if (last && greedy && parser.get() == ResourceKeyedValueParameters.STRING.get())
            {
                return (ValueParser<T>) ResourceKeyedValueParameters.REMAINING_JOINED_STRINGS.get();
            }
            return (ValueParser<T>) parser.get();
        }

        if (type instanceof Class && ((Class<?>)type).isEnum())
        {
            // TODO cache me
            return (ValueParameter<T>)VariableValueParameters.enumChoices(((Class<? extends Enum>)type));
        }

        throw new IllegalArgumentException("No parser was registered for " + type);
    }

    static ValueCompleter getCompleter(Injector injector, Type type, Class<? extends ValueCompleter> completerType)
    {
        if (completerType != null)
        {
            final Supplier<ValueCompleter> customCompleter = completers.get(TypeToken.get(completerType));
            if (customCompleter != null)
            {
                return customCompleter.get();
            }
            if (ValueCompleter.class.isAssignableFrom(completerType))
            {
                final ValueCompleter valueCompleter = injector.getInstance(completerType);
                completers.put(type, () -> valueCompleter);
                return valueCompleter;
            }
            throw new IllegalStateException("Completer cannot be created! " + completerType);
        }
        final Supplier<ValueCompleter> completer = completers.get(type);
        if (completer == null)
        {
            if (type instanceof Class && ((Class<?>)type).isEnum())
            {
                // TODO cache me
                return VariableValueParameters.enumChoices(((Class<? extends Enum>)type));
            }
            return null;
        }
        return completer.get();
    }

    static <T> DefaultParameterProvider<T> getDefaultProvider(Injector injector, Type type, Class<?> customType)
    {
        if (customType != DefaultParameterProvider.class && DefaultParameterProvider.class.isAssignableFrom(customType))
        {
            final Supplier<DefaultParameterProvider<T>> provider = (Supplier) defaultProviders.get(TypeToken.get(customType));
            if (provider != null) {
                return provider.get();
            }
            final DefaultParameterProvider<T> defaultProvider = (DefaultParameterProvider<T>) injector.getInstance(customType);
            defaultProviders.put(type, () -> defaultProvider);
            return defaultProvider;
        }
        else
        {
            if (customType != DefaultParameterProvider.class)
            {
                type = customType;
            }
            final Supplier<DefaultParameterProvider<T>> provider = (Supplier) defaultProviders.get(type);
            if (provider != null) {
                return provider.get();
            }
        }
        throw new IllegalArgumentException("No default provider was registered for " + type);
    }
}
