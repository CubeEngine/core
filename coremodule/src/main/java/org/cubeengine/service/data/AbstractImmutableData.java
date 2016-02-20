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
package org.cubeengine.service.data;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

public abstract class AbstractImmutableData<I extends ImmutableDataManipulator<I, M>, M extends DataManipulator<M, I>>
    extends BaseAbstractData<I, ImmutableValue<?>>
    implements ImmutableDataManipulator<I, M>
{

    protected AbstractImmutableData(int version)
    {
        super(version);
        registerGetters();
    }

    protected abstract void registerGetters();

    @Override
    public final I copy()
    {
        return (I)this;
    }

    // Beyond this point is involving keyFieldGetters or keyValueGetters. No external
    // implementation required.

    @Override
    public <E> Optional<I> with(Key<? extends BaseValue<E>> key, E value)
    {
        if (!supports(key))
        {
            return Optional.empty();
        }
        return Optional.of(asMutable().set(key, value).asImmutable());
    }

    // Then finally traditional java stuff.

    @Override
    public int hashCode()
    {
        return Objects.hashCode(this.getters.values().stream().map(Supplier::get).collect(Collectors.toList()));
    }

    @Override
    protected <E> ImmutableValue<?> value(Key<Value<E>> key, Supplier<E> getter)
    {
        return factory().createValue(key, getter.get()).asImmutable();
    }
}
