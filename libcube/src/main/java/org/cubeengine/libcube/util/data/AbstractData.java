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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractData<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>>
    extends BaseAbstractData<M, Value<?>>
    implements DataManipulator<M, I>
{
    private final Map<Key<?>, Consumer<Object>> setters = Maps.newHashMap();
    private final Class<?>[] dataHolders;

    protected AbstractData(int version, Class<?>... dataHolders)
    {
        super(version);
        this.dataHolders = dataHolders;
        registerKeys();
    }

    protected final <E> void registerSetter(Key<? extends BaseValue<E>> key, Consumer<E> function)
    {
        this.setters.put(checkNotNull(key), checkNotNull((Consumer)function));
    }

    protected abstract void registerKeys();

    @Override
    public <E> M set(Key<? extends BaseValue<E>> key, E value)
    {
        checkArgument(supports(key), "This data manipulator doesn't support the following key: " + key.toString());
        this.setters.get(key).accept(value);
        return (M)this;
    }

    @Override
    public <E> M transform(Key<? extends BaseValue<E>> key, Function<E, E> function)
    {
        checkArgument(supports(key));
        this.setters.get(key).accept(checkNotNull(function.apply((E)this.getters.get(key).get())));
        return (M)this;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getters, this.setters, this.valueGetters);
    }

    public <T> void register(Key<Value<T>> key, Supplier<T> getter, Consumer<T> setter, Supplier<Value<?>> valueGetter)
    {
        register(key, getter, valueGetter);
        registerSetter(key, setter);
    }

    public <T> void registerSingle(Key<Value<T>> key, Supplier<T> getter, Consumer<T> setter)
    {
        this.register(key, getter, setter, () -> value(key, getter));
    }

    @Override
    protected <T> Value<?> value(Key<Value<T>> key, Supplier<T> getter)
    {
        return factory().createValue(key, getter.get());
    }

    public boolean supports(DataHolder dataHolder)
    {
        if (dataHolders.length == 0)
        {
            return true;
        }
        for (Class<?> holder : dataHolders)
        {
            if (holder.isAssignableFrom(dataHolder.getClass()))
            {
                return true;
            }
        }
        return false;
    }
}
