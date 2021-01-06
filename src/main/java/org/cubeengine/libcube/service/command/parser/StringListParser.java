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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;
import org.spongepowered.api.command.parameter.CommandContext.Builder;
import org.spongepowered.api.command.parameter.Parameter.Key;
import org.spongepowered.api.command.parameter.managed.ValueParser;

public class StringListParser implements ValueParser<List<String>>
{
    @Override
    public Optional<? extends List<String>> getValue(Key<? super List<String>> parameterKey, Mutable reader, Builder context) throws ArgumentParseException
    {
        List<String> list = new ArrayList<>();
        do
        {
            if (reader.peekString().isEmpty())
            {
                return Optional.empty();
            }
            list.add(reader.parseString());
        }
        while (reader.canRead());
        return Optional.of(list);
    }
}
