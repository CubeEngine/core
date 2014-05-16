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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public final class HelpContext extends BasicContext
{
    public HelpContext(CommandContext ctx)
    {
        super(ctx.getCommand(), ctx.getSender(), ctx.getLabels(), ctx.getArgs());
    }

    public HelpContext(CubeCommand command, CommandSender sender, Stack<String> labels, String[] args)
    {
        super(command, sender, labels, new LinkedList<Object>(Arrays.asList(args)));
    }
}
