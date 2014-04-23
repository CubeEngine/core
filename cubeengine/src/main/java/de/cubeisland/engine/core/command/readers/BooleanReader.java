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
import java.util.Set;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.exception.InvalidArgumentException;

import de.cubeisland.engine.core.util.formatter.MessageType;
import gnu.trove.set.hash.THashSet;

public class BooleanReader extends ArgumentReader
{
    private final Core core;
    private final Set<String> yesStrings;
    private final Set<String> noStrings;

    public BooleanReader(Core core)
    {
        this.core = core;
        this.yesStrings = new THashSet<>();
        this.yesStrings.add("yes");
        this.yesStrings.add("+");
        this.yesStrings.add("1");
        this.yesStrings.add("true");

        this.noStrings = new THashSet<>();
        this.noStrings.add("no");
        this.noStrings.add("-");
        this.noStrings.add("0");
        this.noStrings.add("false");
    }

    @Override
    public Boolean read(String arg, Locale locale) throws InvalidArgumentException
    {
        arg = arg.trim().toLowerCase(locale);
        if (this.yesStrings.contains(arg))
        {
            return true;
        }
        else if (this.noStrings.contains(arg))
        {
            return false;
        }
        else
        {
            String word = this.core.getI18n().translate(locale, "yes");
            if (arg.equalsIgnoreCase(word))
            {
                return true;
            }
            word = this.core.getI18n().translate(locale, "no");
            if (arg.equalsIgnoreCase(word))
            {
                return false;
            }
        }
        return Boolean.parseBoolean(arg);
    }
}
