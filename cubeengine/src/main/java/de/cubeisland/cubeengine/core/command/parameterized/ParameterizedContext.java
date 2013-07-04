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
package de.cubeisland.cubeengine.core.command.parameterized;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.cubeengine.core.command.BasicContext;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.user.User;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class ParameterizedContext extends BasicContext
{
    private final Set<String> flags;
    private final Map<String, Object> params;

    private final int flagCount;
    private final int paramCount;

    public ParameterizedContext(CubeCommand command, CommandSender sender, Stack<String> labels, LinkedList<String> args, Set<String> flags, Map<String, Object> params)
    {
        super(command, sender, labels, args);
        this.flags = flags;
        this.params = params;

        this.flagCount = flags.size();
        this.paramCount = params.size();
    }

    public boolean hasFlag(String name)
    {
        return this.flags.contains(name.toLowerCase(Locale.ENGLISH));
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
        return new THashSet<String>(this.flags);
    }

    public boolean hasParams()
    {
        return this.paramCount > 0;
    }

    public Map<String, Object> getParams()
    {
        return new THashMap<String, Object>(this.params);
    }

    public boolean hasParam(String name)
    {
        return this.params.containsKey(name.toLowerCase(Locale.ENGLISH));
    }

    public <T> T getParam(String name)
    {
        return (T)this.params.get(name.toLowerCase(Locale.ENGLISH));
    }

    public <T> T getParam(String name, T def)
    {
        try
        {
            T value = this.getParam(name);
            if (value != null)
            {
                return value;
            }
        }
        catch (Exception ignored)
        {}
        return def;
    }

    public String getString(String name)
    {
        return this.getParam(name);
    }

    public String getString(String name, String def)
    {
        return this.getParam(name, def);
    }

    public User getUser(String name)
    {
        return this.getParam(name, null);
    }
}
