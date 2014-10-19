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

import java.util.Locale;

import org.bukkit.Difficulty;

import de.cubeisland.engine.command.parameter.reader.ArgumentReader;
import de.cubeisland.engine.command.parameter.reader.ReaderException;
import de.cubeisland.engine.command.parameter.reader.ReaderManager;
import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.core.CubeEngine;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;

public class DifficultyReader implements ArgumentReader<Difficulty>
{
    @Override
    public Difficulty read(ReaderManager manager, Class type, CommandInvocation invocation) throws ReaderException
    {
        String arg = invocation.consume(1);
        Locale locale = invocation.getLocale();
        try
        {
            Difficulty difficulty = Difficulty.getByValue(Integer.valueOf(arg));
            if (difficulty == null)
            {
                throw new ReaderException(CubeEngine.getCore().getI18n().translate(locale, NEGATIVE, "The given difficulty level is unknown!"));
            }
            return difficulty;
        }
        catch (NumberFormatException e)
        {
            try
            {
                return Difficulty.valueOf(arg.toUpperCase(locale));
            }
            catch (IllegalArgumentException ex)
            {
                throw new ReaderException(CubeEngine.getCore().getI18n().translate(locale, NEGATIVE, "{input} is not a known difficulty!", arg));
            }
        }
    }
}
