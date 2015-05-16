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
package de.cubeisland.engine.module.service.paginate;

import java.util.List;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.result.CommandResult;
import de.cubeisland.engine.module.service.command.CommandContext;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.NONE;

public class PaginatedResult implements CommandResult
{
    private final CommandContext context;
    private final PaginationIterator iterator;

    private int pageNumber = 0;

    public PaginatedResult(CommandContext context, List<String> lines)
    {
        this(context, new StringListIterator(lines));
    }

    public PaginatedResult(CommandContext context, PaginationIterator iterator)
    {
        this.context = context;
        this.iterator = iterator;

        context.getModule().getModularity().start(PaginationManager.class).registerResult(context.getSource(), this);
    }

    @Override
    public void process(CommandInvocation invocation)
    {
        int pageCount = iterator.pageCount(SpongePaginationManager.LINES_PER_PAGE);
        context.sendTranslated(NONE, SpongePaginationManager.HEADER, pageNumber + 1, pageCount);
        iterator.getPage(pageNumber, SpongePaginationManager.LINES_PER_PAGE).forEach(context::sendMessage);
        if (pageNumber < 1)
        {
            if (pageCount == 1)
            {
                context.sendTranslated(NONE, SpongePaginationManager.ONE_PAGE_FOOTER, pageNumber + 1, pageCount);
                return;
            }
            context.sendTranslated(NONE, SpongePaginationManager.FIRST_FOOTER, pageNumber + 1, pageCount);
            return;
        }
        if (pageNumber >= pageCount)
        {
            context.sendTranslated(NONE, SpongePaginationManager.LAST_FOOTER, pageNumber + 1, pageCount);
            return;
        }
        context.sendTranslated(NONE, SpongePaginationManager.FOOTER, pageNumber + 1, pageCount);
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
        if (pageNumber >= 0 && pageNumber < iterator.pageCount(SpongePaginationManager.LINES_PER_PAGE))
        {
            this.pageNumber = pageNumber;
            this.process(this.context.getInvocation());
            return;
        }
        context.sendTranslated(NEGATIVE, "The page you want to see is out of bounds.");
    }
}
