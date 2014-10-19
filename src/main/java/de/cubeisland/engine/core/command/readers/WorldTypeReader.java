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

import org.bukkit.WorldType;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.parameter.reader.ArgumentReader;
import de.cubeisland.engine.command.parameter.reader.ReaderException;
import de.cubeisland.engine.command.parameter.reader.ReaderManager;

public class WorldTypeReader implements ArgumentReader<WorldType>
{
    @Override
    public WorldType read(ReaderManager manager, Class type, CommandInvocation invocation) throws ReaderException
    {
        String arg = invocation.consume(1);
        //Locale locale = invocation.getLocale();
        return WorldType.valueOf(arg); //TODO exceptionhandling
    }
}
