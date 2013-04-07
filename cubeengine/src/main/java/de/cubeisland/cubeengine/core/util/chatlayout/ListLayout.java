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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ListLayout extends AbstractChatLayout<List<String>>
{
    private final String bullet;

    public ListLayout()
    {
        this(" - ");
    }

    public ListLayout(String bullet)
    {
        this.bullet = bullet;
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

        final int maxLen = MAX_CHAT_WIDTH - this.bullet.length();
        List<String> lines = new LinkedList<String>();
        final String spacer = StringUtils.repeat(" ", this.bullet.length());

        for (String entry : data)
        {
            List<String> parts = this.splitUp(entry, maxLen);
            Iterator<String> iter = parts.iterator();
            lines.add(this.bullet + iter.next());
            while (iter.hasNext())
            {
                lines.add(spacer + iter.next());
            }
        }

        return this.compiled = lines.toArray(new String[lines.size()]);
    }
}
