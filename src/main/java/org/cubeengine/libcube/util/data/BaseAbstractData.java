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
package org.cubeengine.libcube.util.data;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.ValueFactory;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.spongepowered.api.data.Queries.CONTENT_VERSION;

abstract class BaseAbstractData<C extends ValueContainer<C>, T extends BaseValue<?>> implements ValueContainer<C>, DataSerializable
{
    final Map<Key<? extends BaseValue<?>>, Supplier<T>> valueGetters = Maps.newHashMap();
    final Map<Key<? extends BaseValue<?>>, Supplier<?>> getters = Maps.newHashMap();

    private final int version;

    public BaseAbstractData(int version)
    {
        this.version = version;
    }

    protected final void registerValue(Key<? extends BaseValue<?>> key, Supplier<T> function)
    {
        this.valueGetters.put(checkNotNull(key), checkNotNull(function));
    }

    protected final void registerGetter(Key<? extends BaseValue<?>> key, Supplier<?> function)
    {
        this.getters.put(checkNotNull(key, "The key cannot be null"),
                         checkNotNull(function, "The function cannot be null"));
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key)
    {
        if (!supports(key))
        {
            return Optional.empty();
        }
        return Optional.ofNullable((E)this.getters.get(key).get());
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key)
    {
        if (!this.valueGetters.containsKey(key))
        {
            return Optional.empty();
        }
        return Optional.of((V)checkNotNull(this.valueGetters.get(key).get()));
    }

    @Override
    public boolean supports(Key<?> key)
    {
        return this.getters.containsKey(checkNotNull(key));
    }

    @Override
    public Set<Key<?>> getKeys()
    {
        return ImmutableSet.copyOf(this.valueGetters.keySet());
    }

    @Override
    public Set<ImmutableValue<?>> getValues()
    {
        ImmutableSet.Builder<ImmutableValue<?>> builder = ImmutableSet.builder();
        for (Entry<Key<? extends BaseValue<?>>, Supplier<?>> entry : getters.entrySet())
        {
            if (entry.getValue().get() == null)
            {
                continue;
            }
            Supplier<T> valueSupplier = valueGetters.get(entry.getKey());
            T value = valueSupplier.get();
            builder.add(checkNotNull(value instanceof Value ? ((Value)value).asImmutable() : ((ImmutableValue<?>)value)));
        }
        return builder.build();
    }


    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        final BaseAbstractData<?, ?> other = (BaseAbstractData<?, ?>)obj;
        return Objects.equals(this.getters.values().stream().map(Supplier::get).collect(toList()),
                              other.getters.values().stream().map(Supplier::get).collect(toList()));
    }


    @Override
    public DataContainer toContainer()
    {
        DataContainer data = DataContainer.createNew().set(CONTENT_VERSION, getContentVersion());
        for (Entry<Key<?>, Supplier<?>> entry : getters.entrySet())
        {
            if (entry.getValue().get() == null)
            {
                continue;
            }
            data.set(entry.getKey().getQuery(), entry.getValue().get());
        }
        return data;
    }

    protected ValueFactory factory()
    {
        return Sponge.getRegistry().getValueFactory();
    }

    public <E> void register(Key<Value<E>> key, Supplier<E> getter, Supplier<T> valueGetter)
    {
        registerGetter(key, getter);
        registerValue(key, valueGetter);
    }

    public <E> void registerSingle(Key<Value<E>> key, Supplier<E> getter)
    {
        this.register(key, getter, () -> value(key, getter));
    }

    protected abstract <E> T value(Key<Value<E>> key, Supplier<E> getter);

    @Override
    public int getContentVersion()
    {
        return version;
    }
}
