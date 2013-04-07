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
package de.cubeisland.cubeengine.core.util.chatlayout;

import de.cubeisland.cubeengine.core.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class TwoColumnLayout extends AbstractChatLayout<List<String[]>>
{
    private final int columnWidth;
    private final String emptyCell;

    public TwoColumnLayout()
    {
        this.columnWidth = (MAX_CHAT_WIDTH - 1) / 2;
        this.emptyCell = StringUtils.repeat(' ', this.columnWidth);
    }

    @Override
    public String[] compile()
    {
        if (this.compiled != null)
        {
            return this.compiled;
        }
        if (this.data == null)
        {
            throw new IllegalStateException("No data set yet!");
        }

        List<String> lines = new LinkedList<String>();

        for (String[] entry : this.data)
        {
            if (entry.length > 1)
            {
                List<String> leftParts = this.splitUp(entry[0], columnWidth);
                List<String> rightParts = this.splitUp(entry[1], columnWidth);

                int diff = leftParts.size() - rightParts.size();
                if (diff > 0)
                {
                    for (; diff >= 0; --diff)
                    {
                        rightParts.add(this.emptyCell);
                    }
                }
                else if (diff < 0)
                {
                    for (; diff <= 0; ++diff)
                    {
                        leftParts.add(this.emptyCell);
                    }
                }

                final int size = leftParts.size();
                for (int i = 0; i < size; ++i)
                {
                    lines.add(StringUtils.padRight(leftParts.get(i), this.columnWidth + 1) + rightParts.get(i));
                }
            }
        }

        return this.compiled = lines.toArray(new String[lines.size()]);
    }
}
