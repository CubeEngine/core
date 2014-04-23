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

import java.util.List;
import java.util.Stack;

import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;

public interface ContextFactory
{
    ArgBounds getArgBounds();

    ContextFactory addIndexed(List<CommandParameterIndexed> indexedParams);
    ContextFactory addIndexed(CommandParameterIndexed param);
    ContextFactory removeLastIndexed();
    CommandParameterIndexed getIndexed(int index);
    List<CommandParameterIndexed> getIndexedParameters();

    CommandContext parse(CubeCommand command, CommandContext context);
    CommandContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] rawArgs);
    CommandContext tabCompleteParse(CubeCommand command, CommandSender sender, Stack<String> labels,String[] rawArgs);

    void calculateArgBounds();
}
