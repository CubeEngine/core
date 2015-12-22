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
package org.cubeengine.service.command.readers;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;

public abstract class CatalogTypeReader<T extends CatalogType> implements ArgumentReader<T>
{
    private Class<T> type;

    public CatalogTypeReader(Class<T> type)
    {
        this.type = type;
    }

    @Override
    public T read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String token = invocation.consume(1);
        for (T cType : Sponge.getRegistry().getAllOf(this.type))
        {
            if (cType.getName().equalsIgnoreCase(token))
            {
                return cType;
            }
        }
        throw new ReaderException("Could not find CatalogType: '" + token + "' in " + this.type.getName());
    }
}
