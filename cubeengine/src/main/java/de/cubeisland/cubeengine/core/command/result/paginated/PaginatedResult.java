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
        int pageCount = iterator.pageCount(LINES_PER_PAGE);
        context.sendTranslated(NONE, HEADER, pageNumber + 1, pageCount);
        for(String line : iterator.getPage(pageNumber, LINES_PER_PAGE))
        {
            context.sendMessage(line);
        }
        if (pageNumber < 1)
        {
            if (pageCount == 1)
            {
                context.sendTranslated(NONE, ONE_PAGE_FOOTER, pageNumber + 1, pageCount);
            }
            else
            {
                context.sendTranslated(NONE, FIRST_FOOTER, pageNumber + 1, pageCount);
            }
        }
        else if (pageNumber >= pageCount)
        {
            context.sendTranslated(NONE, LAST_FOOTER, pageNumber + 1, pageCount);
        }
        else
        {
            context.sendTranslated(NONE, FOOTER, pageNumber + 1, pageCount);
        }
    }

    public void nextPage()
    {
        showPage(pageNumber + 1);
    }

    public void prevPage()
    {
        showPage(pageNumber - 1);
    }

    public void showPage(int pageNumber)
    {
        if (pageNumber >= 0 && pageNumber < iterator.pageCount(LINES_PER_PAGE))
        {
            this.pageNumber = pageNumber;
            this.show(this.context);
        }
        else
        {
            context.sendTranslated(NEGATIVE, "The page you want to see is out of bounds.");
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
        public int pageCount(int numberOfLinesPerPage)
        {
            return (int) Math.ceil((float) lines.size() / (float) numberOfLinesPerPage);
        }
    }
}
