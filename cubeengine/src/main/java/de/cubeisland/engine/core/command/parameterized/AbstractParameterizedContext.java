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
package de.cubeisland.engine.core.command.parameterized;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.engine.core.command.BasicContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import gnu.trove.set.hash.THashSet;

import static java.util.Locale.ENGLISH;

public abstract class AbstractParameterizedContext<T> extends BasicContext
{
    protected final Set<String> flags;
    protected final Map<String, T> params;
    protected final int flagCount;
    protected final int paramCount;

    public AbstractParameterizedContext(CubeCommand command, CommandSender sender, Stack<String> labels,
                                        List<Object> args, Set<String> flags, Map<String, T> params)
    {
        super(command, sender, labels, args);
        this.flags = flags;
        this.params = params;
        this.flagCount = flags.size();
        this.paramCount = params.size();
    }

    public boolean hasFlag(String name)
    {
        return this.flags.contains(name.toLowerCase(ENGLISH));
    }

    public boolean hasFlags(String... names)
    {
        for (String name : names)
        {
            if (!this.hasFlag(name))
            {
                return false;
            }
        }
        return true;
    }

    public int getFlagCount()
    {
        return this.flagCount;
    }

    public Set<String> getFlags()
    {
        return new THashSet<>(this.flags);
    }

    public boolean hasParams()
    {
        return this.paramCount > 0;
    }

    public LinkedHashMap<String, T> getParams()
    {
        return new LinkedHashMap<>(this.params);
    }

    public boolean hasParam(String name)
    {
        return this.params.containsKey(name.toLowerCase(ENGLISH));
    }
}
