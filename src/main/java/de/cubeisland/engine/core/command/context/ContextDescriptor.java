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
package de.cubeisland.engine.core.command.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;

import static java.util.Locale.ENGLISH;

public class ContextDescriptor
{
    protected ArgBounds bounds;

    protected final LinkedHashMap<Integer, CommandParameterIndexed> indexedMap;
    protected final LinkedHashMap<String, CommandParameter> namedMap;
    protected final Map<String, CommandFlag> flagMap;

    /**
     * Do not call except from @link{de.cubeisland.engine.core.command.context.ContextBuilder}
     */
    ContextDescriptor(LinkedHashMap<Integer, CommandParameterIndexed> indexedMap, LinkedHashMap<String, CommandParameter> namedMap, Map<String, CommandFlag> flagMap)
    {
        this.indexedMap = indexedMap;
        this.namedMap = namedMap;
        this.flagMap = flagMap;
    }

    public ArgBounds getArgBounds()
    {
        return this.bounds;
    }

    public CommandParameterIndexed getIndexed(int index)
    {
        return this.indexedMap.get(index);
    }

    public List<CommandParameterIndexed> getIndexedParameters()
    {
        return new ArrayList<>(this.indexedMap.values());
    }

    public CommandParameter getParameter(String name)
    {
        return this.namedMap.get(name.toLowerCase(ENGLISH));
    }

    public Set<CommandParameter> getParameters()
    {
        return new LinkedHashSet<>(this.namedMap.values());
    }

    public CommandFlag getFlag(String name)
    {
        return this.flagMap.get(name.toLowerCase(ENGLISH));
    }

    public Set<CommandFlag> getFlags()
    {
        return new HashSet<>(this.flagMap.values());
    }
}
