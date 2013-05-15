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

import java.util.Arrays;
import java.util.List;

import de.cubeisland.cubeengine.core.command.CommandSender;

public class PaginationManager
{
    public static final String HEADER = "----------Page %d----------";
    public static final String FOOTER = "--/prev - Page %d - /next--";
    public static final String CARET = " - ";
    public static final int LINES_PER_PAGE = 5;

    public List<String> getPage(CommandSender sender, int page)
    {
         return Arrays.asList("-");
    }

    public List<String> getNextPage(CommandSender sender)
    {
        return getPage(sender, 1);
    }

    public List<String> getPrevPage(CommandSender sender)
    {
        return getPage(sender, 0);
    }

    public boolean hasResult(CommandSender sender)
    {
        return false;
    }
}
