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
package de.cubeisland.engine.core.command.readers;

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.world.DimensionType;

// TODO generic CatalogType Reader
public class EnvironmentReader implements ArgumentReader<DimensionType>
{
    private final GameRegistry registry;

    public EnvironmentReader(Game game)
    {
        registry = game.getRegistry();
    }

    @Override
    public DimensionType read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String token = invocation.consume(1);
        for (DimensionType dimensionType : registry.getAllOf(DimensionType.class))
        {
            if (dimensionType.getName().equalsIgnoreCase(token))
            {
                return dimensionType;
            }
        }
        throw new ReaderException(""); // TODO error message
    }
}
