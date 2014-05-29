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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import gnu.trove.set.hash.THashSet;

public class CubeContextFactory extends ContextReader
{
    public CubeContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] rawArgs)
    {
        final List<String> indexed = new LinkedList<>();
        final Set<String> flags = new THashSet<>();
        final Map<String, String> named = new LinkedHashMap<>();
        return new CubeContext(rawArgs, indexed, named, flags, this.parse(rawArgs, indexed, named, flags), command, sender, labels);
    }
}
