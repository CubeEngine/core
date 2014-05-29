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
package de.cubeisland.engine.core.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.command.ContextParser.Type;

import static java.util.Locale.ENGLISH;

public class ReadContext extends BaseCommandContext
{
    List<Object> indexed;
    Map<String, Object> named;

    public ReadContext(String[] rawArgs, List<String> rawIndexed, Map<String, String> rawNamed, Set<String> flags,
                       Type last)
    {
        super(rawArgs, rawIndexed, rawNamed, flags, last);
    }

    public List<Object> getIndexed()
    {
        return new ArrayList<>(this.indexed);
    }

    @SuppressWarnings("unchecked")
    public <T> T getArg(int i)
    {
        if (this.hasIndexed(i))
        {
            return (T)this.indexed.get(i);
        }
        return null;
    }

    public <T> T getArg(int index, T def)
    {
        T value = this.getArg(index);
        if (value == null)
        {
            value = def;
        }
        return value;
    }

    public Map<String, Object> getParams()
    {
        return new LinkedHashMap<>(this.named);
    }

    @SuppressWarnings("unchecked")
    public <T> T getArg(String name)
    {
        return (T)this.named.get(name.toLowerCase(ENGLISH));
    }

    public <T> T getArg(String name, T def)
    {
        T value = this.getArg(name);
        if (value == null)
        {
            value = def;
        }
        return value;
    }
}
