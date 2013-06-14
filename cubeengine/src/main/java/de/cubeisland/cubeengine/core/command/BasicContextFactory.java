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
package de.cubeisland.cubeengine.core.command;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

import de.cubeisland.cubeengine.core.command.exception.IncorrectUsageException;

public class BasicContextFactory implements ContextFactory
{
    private ArgBounds bounds;

    public BasicContextFactory()
    {
        this(new ArgBounds(0, 0));
    }

    public BasicContextFactory(ArgBounds bounds)
    {
        this.bounds = bounds;
    }

    @Override
    public ArgBounds getArgBounds()
    {
        return this.bounds;
    }

    public void setArgBounds(ArgBounds newBounds)
    {
        this.bounds = newBounds;
    }

    @Override
    public BasicContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] commandLine)
    {
        if (commandLine.length < this.getArgBounds().getMin())
        {
            throw new IncorrectUsageException("You've given too few arguments.");
        }
        if (this.getArgBounds().getMax() > ArgBounds.NO_MAX && commandLine.length > this.getArgBounds().getMax())
        {
            throw new IncorrectUsageException("You've given too many arguments.");
        }
        return new BasicContext(command, sender, labels, new LinkedList<String>(Arrays.asList(commandLine)));
    }

    @Override
    public CommandContext parse(CubeCommand command, CommandContext context)
    {
        return new BasicContext(command, context.getSender(), context.getLabels(), context.getArgs());
    }
}
