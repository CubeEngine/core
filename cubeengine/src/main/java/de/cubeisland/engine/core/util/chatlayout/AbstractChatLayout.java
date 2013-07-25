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
package de.cubeisland.engine.core.util.chatlayout;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractChatLayout<T> implements ChatLayout<T>
{
    public static final int MAX_CHAT_WIDTH = 55;
    protected T data = null;
    protected String[] compiled = null;

    public void setData(T data)
    {
        this.data = data;
        this.compiled = null;
    }

    public T getData()
    {
        return data;
    }

    public List<String> splitUp(String string, int maxLen)
    {
        List<String> parts = new LinkedList<String>();
        if (string == null)
        {
            return null;
        }
        while (string.length() > maxLen)
        {
            parts.add(string.substring(0, maxLen));
            string = string.substring(maxLen);
        }
        if (!string.isEmpty())
        {
            parts.add(string);
        }

        return parts;
    }
}
