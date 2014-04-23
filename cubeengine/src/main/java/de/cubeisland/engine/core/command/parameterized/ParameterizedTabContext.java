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

import static java.util.Locale.ENGLISH;

public class ParameterizedTabContext extends AbstractParameterizedContext<String>
{
    public ParameterizedTabContext(CubeCommand command, CommandSender sender, Stack<String> labels, List<String> args, Set<String> flags, Map<String, String> rawParams, Type last)
    {
        super(command, sender, labels, args, flags, rawParams);
        this.last = last;
    }

    public String getString(String name)
    {
        return this.params.get(name.toLowerCase(ENGLISH));
    }

    public final Type last;

    public enum Type
    {
        ANY,
        NOTHING,
        FLAG_OR_INDEXED,
        INDEXED_OR_PARAM,
        PARAM_VALUE;
    }

    public static class LastType
    {
        public Type last;
    }
}
