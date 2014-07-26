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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;

import static java.util.Locale.ENGLISH;

public class ContextBuilder
{
    private final LinkedHashMap<Integer, CommandParameterIndexed> indexedMap = new LinkedHashMap<>();
    private final LinkedHashMap<String, CommandParameter> namedMap = new LinkedHashMap<>();
    private final Map<String, CommandFlag> flagMap = new LinkedHashMap<>();

    private ContextDescriptor descriptor = newDescriptor();

    public static ContextBuilder build()
    {
        return new ContextBuilder();
    }

    protected ContextDescriptor newDescriptor()
    {
        return new ContextDescriptor(indexedMap, namedMap, flagMap);
    }

    public ContextBuilder addIndexed(List<CommandParameterIndexed> params)
    {
        for (CommandParameterIndexed param : params)
        {
            this.add(param);
        }
        return this;
    }

    public ContextBuilder add(CommandParameterIndexed param)
    {
        indexedMap.put(indexedMap.size(), param);
        return this;
    }

    public ContextBuilder addNamed(Collection<CommandParameter> params)
    {
        for (CommandParameter param : params)
        {
            this.add(param);
        }
        return this;
    }

    public ContextBuilder add(CommandParameter param)
    {
        namedMap.put(param.getName().toLowerCase(ENGLISH), param);
        for (String alias : param.getAliases())
        {
            alias = alias.toLowerCase(ENGLISH);
            if (namedMap.containsKey(alias))
            {
                namedMap.put(alias, param);
            }
        }
        return this;
    }

    public ContextBuilder addFlags(Collection<CommandFlag> flags)
    {
        for (CommandFlag flag : flags)
        {
            this.add(flag);
        }
        return this;
    }

    public ContextBuilder add(CommandFlag flag)
    {
        flagMap.put(flag.getName().toLowerCase(ENGLISH), flag);
        final String longName = flag.getLongName().toLowerCase(ENGLISH);
        if (!flagMap.containsKey(longName))
        {
            flagMap.put(longName, flag);
        }
        return this;
    }

    public ContextDescriptor get()
    {
        ContextDescriptor descriptor = this.descriptor;
        descriptor.bounds = new ArgBounds(new ArrayList<>(this.indexedMap.values()));
        this.descriptor = null;
        return descriptor;
    }
}
