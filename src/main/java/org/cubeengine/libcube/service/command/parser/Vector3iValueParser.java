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

import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;
import org.spongepowered.api.command.parameter.CommandContext.Builder;
import org.spongepowered.api.command.parameter.Parameter.Key;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public class Vector3iValueParser implements ValueParser<Vector3i>
{
    @Override
    public Optional<? extends Vector3i> parseValue(Key<? super Vector3i> parameterKey, Mutable reader, Builder context) throws ArgumentParseException
    {
        final int x = reader.parseInt();
        reader.skipWhitespace();
        final int y = reader.parseInt();
        reader.skipWhitespace();
        final int z = reader.parseInt();
        return Optional.of(new Vector3i(x, y, z));
    }
}
