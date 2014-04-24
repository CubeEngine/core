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
import de.cubeisland.engine.core.util.formatter.MessageType;

import static de.cubeisland.cubeengine.core.command.result.paginated.PaginationManager.*;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class PaginatedResult implements CommandResult
{
    private final CommandContext context;
    private final PaginationIterator iterator;

    private int pageNumber = 0;

    public PaginatedResult(CommandContext context, List<String> lines)
    {
        this.context = context;
        this.iterator = new StringListIterator(lines);

        context.getCore().getCommandManager().getPaginationManager().registerResult(context.getSender(), this);
    }
    public PaginatedResult(CommandContext context, PaginationIterator iterator)
    {
        this.context = context;
        this.iterator = iterator;

        context.getCore().getCommandManager().getPaginationManager().registerResult(context.getSender(), this);
    }

    @Override
    public void show(CommandContext context)
    {
        context.sendTranslated(NONE, HEADER, pageNumber + 1, 0);
        for(String line : iterator.getPage(pageNumber, LINES_PER_PAGE))
        {
            context.sendMessage(line);
        }
        context.sendTranslated(NONE, FOOTER, pageNumber + 1, 0);
    }

    public void nextPage()
    {
        if (iterator.hasNextPage(pageNumber, LINES_PER_PAGE))
        {
            pageNumber++;
            this.show(this.context);
        }
        else
        {
            context.sendTranslated(NEGATIVE, "You are already at the last page.");
        }
    }
    public void prevPage()
    {
        if (pageNumber > 0)
        {
            pageNumber--;
            this.show(this.context);
        }
        else
        {
            context.sendTranslated(NEGATIVE, "You are already at the first page.");
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

        @Override
        public boolean hasNextPage(int page, int numberOfLines)
        {
            int offset = page * numberOfLines;
            if (offset < lines.size() - 1)
            {
                return true;
            }
            return false;
        }
    }
}
