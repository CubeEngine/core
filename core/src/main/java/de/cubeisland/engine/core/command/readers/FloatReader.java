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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.exception.InvalidArgumentException;

public class FloatReader extends ArgumentReader
{
    @Override
    public Float read(String arg, Locale locale) throws InvalidArgumentException
    {
        try
        {
            return NumberFormat.getInstance(locale).parse(arg).floatValue();
        }
        catch (ParseException e)
        {
            try
            {
                return NumberFormat.getInstance().parse(arg).floatValue(); // Try default locale
            }
            catch (ParseException e1)
            {
                throw new InvalidArgumentException("Could not parse {input} to float!", arg);
            }
        }
    }
}
