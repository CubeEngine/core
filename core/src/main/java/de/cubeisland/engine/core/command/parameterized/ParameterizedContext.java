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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.user.User;

import static java.util.Locale.ENGLISH;

public class ParameterizedContext extends AbstractParameterizedContext<Object>
{
    public ParameterizedContext(CubeCommand command, CommandSender sender, Stack<String> labels, List<Object> args, Set<String> flags, Map<String, Object> params)
    {
        super(command, sender, labels, args, flags, params);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParam(String name)
    {
        return (T)this.params.get(name.toLowerCase(ENGLISH));
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
        catch (ClassCastException ignored)
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
