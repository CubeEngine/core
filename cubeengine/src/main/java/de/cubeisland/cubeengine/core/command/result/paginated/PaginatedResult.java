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
package de.cubeisland.cubeengine.core.command.result.paginated;

import java.util.ArrayList;
import java.util.List;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;

public class PaginatedResult implements CommandResult
{
    private final CommandContext context;
    private final PaginationIterator iterator;

    private int pageNumber;

    private final PaginationManager paginationManager;

    public PaginatedResult(CommandContext context, List<String> lines)
    {
        this.context = context;
        this.iterator = new StringListIterator(lines);

        pageNumber = 0;

        paginationManager = context.getCommand().getModule().getCore().getCommandManager().getPaginationManager();
    }
    public PaginatedResult(CommandContext context, PaginationIterator iterator)
    {
        this.context = context;
        this.iterator = iterator;

        pageNumber = 0;

        paginationManager = context.getCommand().getModule().getCore().getCommandManager().getPaginationManager();
    }

    @Override
    public void show(CommandContext context)
    {
        for(String line : iterator.getPage(pageNumber, PaginationManager.LINES_PER_PAGE))
        {
            context.sendMessage(line);
        }
    }

    private class StringListIterator implements PaginationIterator
    {
        private List<String> lines;

        public StringListIterator(List<String> lines)
        {
            this.lines = lines;
        }

        @Override
        public List<String> getPage(int page, int numberOfLines)
        {
            int offset = page * numberOfLines;
            if (offset < lines.size())
            {
                int lastItem = Math.min(offset + numberOfLines, lines.size());
                return lines.subList(offset, lastItem);
            }
            return new ArrayList<>();
        }
    }
}
