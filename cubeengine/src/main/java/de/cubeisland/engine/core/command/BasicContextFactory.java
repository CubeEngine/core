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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;

public class BasicContextFactory implements ContextFactory
{
    private ArgBounds bounds;
    private final LinkedHashMap<Integer, CommandParameterIndexed> indexedMap = new LinkedHashMap<>();
    private int indexedCount = 0;

    public BasicContextFactory()
    {
        this(Collections.<CommandParameterIndexed>emptyList());
    }

    public BasicContextFactory(List<CommandParameterIndexed> indexed)
    {
        this.bounds = new ArgBounds(indexed);
        this.addIndexed(indexed);
    }

    @Override
    public ArgBounds getArgBounds()
    {
        return this.bounds;
    }

    @Override
    public BasicContextFactory addIndexed(List<CommandParameterIndexed> indexedParams)
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

    @Override
    public BasicContextFactory addIndexed(CommandParameterIndexed param)
    {
        this.indexedMap.put(indexedCount++, param);
        return this;
    }

    @Override
    public BasicContextFactory removeLastIndexed()
    {
        this.indexedMap.remove(--indexedCount);
        return this;
    }

    @Override
    public CommandParameterIndexed getIndexed(int index)
    {
        return this.indexedMap.get(index);
    }

    @Override
    public List<CommandParameterIndexed> getIndexedParameters()
    {
        return new ArrayList<>(this.indexedMap.values());
    }

    @Override
    public BasicContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] rawArgs)
    {
        return new BasicContext(command, sender, labels, new LinkedList<>(Arrays.asList(rawArgs)));
    }

    @Override
    public CommandContext tabCompleteParse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] rawArgs)
    {
        return new BasicContext(command, sender, labels, new LinkedList<>(Arrays.asList(rawArgs)));
    }

    @Override
    public CommandContext parse(CubeCommand command, CommandContext context)
    {
        return new BasicContext(command, context.getSender(), context.getLabels(), context.getArgs());
    }

    @Override
    public void calculateArgBounds()
    {
        this.bounds = new ArgBounds(new ArrayList<>(this.indexedMap.values()));
    }
}
