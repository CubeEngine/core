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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;

import static java.util.Locale.ENGLISH;

public class ContextDescriptor
{
    private int indexedCount = 0;

    protected ArgBounds bounds;

    protected final LinkedHashMap<Integer, CommandParameterIndexed> indexedMap = new LinkedHashMap<>();
    protected final LinkedHashMap<String, CommandParameter> namedMap = new LinkedHashMap<>();
    protected final Map<String, CommandFlag> flagMap = new LinkedHashMap<>();

    public ArgBounds getArgBounds()
    {
        return this.bounds;
    }

    public ContextDescriptor addIndexed(List<CommandParameterIndexed> indexedParams)
    {
        if (indexedParams != null)
        {
            for (CommandParameterIndexed param : indexedParams)
            {
                this.addIndexed(param);
            }
        }
        return this;
    }

    public ContextDescriptor addIndexed(CommandParameterIndexed param)
    {
        this.indexedMap.put(indexedCount++, param);
        return this;
    }

    public ContextDescriptor removeLastIndexed()
    {
        this.indexedMap.remove(--indexedCount);
        return this;
    }

    public CommandParameterIndexed getIndexed(int index)
    {
        return this.indexedMap.get(index);
    }

    public List<CommandParameterIndexed> getIndexedParameters()
    {
        return new ArrayList<>(this.indexedMap.values());
    }


    public ContextDescriptor addNamed(Collection<CommandParameter> params)
    {
        if (params != null)
        {
            for (CommandParameter param : params)
            {
                this.addNamed(param);
            }
        }
        return this;
    }

    public ContextDescriptor addNamed(CommandParameter param)
    {
        this.namedMap.put(param.getName().toLowerCase(ENGLISH), param);
        for (String alias : param.getAliases())
        {
            alias = alias.toLowerCase(ENGLISH);
            if (!this.namedMap.containsKey(alias))
            {
                this.namedMap.put(alias, param);
            }
        }
        return this;
    }

    public ContextDescriptor removeParameter(String name)
    {
        CommandParameter param = this.namedMap.remove(name.toLowerCase(ENGLISH));
        if (param != null)
        {
            Iterator<Entry<String, CommandParameter>> it = this.namedMap.entrySet().iterator();
            while (it.hasNext())
            {
                if (it.next().getValue() == param)
                {
                    it.remove();
                }
            }
        }
        return this;
    }

    public CommandParameter getParameter(String name)
    {
        return this.namedMap.get(name.toLowerCase(ENGLISH));
    }

    public Set<CommandParameter> getParameters()
    {
        return new LinkedHashSet<>(this.namedMap.values());
    }

    public void addFlags(Collection<CommandFlag> flags)
    {
        if (flags != null)
        {
            for (CommandFlag flag : flags)
            {
                this.addFlag(flag);
            }
        }
    }

    public ContextDescriptor addFlag(CommandFlag flag)
    {
        this.flagMap.put(flag.getName().toLowerCase(ENGLISH), flag);
        final String longName = flag.getLongName().toLowerCase(ENGLISH);
        if (!this.flagMap.containsKey(longName))
        {
            this.flagMap.put(longName, flag);
        }
        return this;
    }

    public ContextDescriptor removeFlag(String name)
    {
        CommandFlag flag = this.flagMap.remove(name.toLowerCase(ENGLISH));
        if (flag != null)
        {
            Iterator<Map.Entry<String, CommandFlag>> it = this.flagMap.entrySet().iterator();
            while (it.hasNext())
            {
                if (it.next().getValue() == flag)
                {
                    it.remove();
                }
            }
        }
        return this;
    }

    public CommandFlag getFlag(String name)
    {
        return this.flagMap.get(name.toLowerCase(ENGLISH));
    }

    public Set<CommandFlag> getFlags()
    {
        return new HashSet<>(this.flagMap.values());
    }

    public void calculateArgBounds()
    {
        this.bounds = new ArgBounds(new ArrayList<>(this.indexedMap.values()));
    }
}
