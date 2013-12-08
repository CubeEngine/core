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
package de.cubeisland.engine.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MacroProcessor
{
    private final Map<String, Integer> keyIndexMap;
    private final int size;

    public MacroProcessor(Collection<String> keys)
    {
        this.keyIndexMap = new HashMap<>();
        int size = 0;
        for (String key : keys)
        {
            this.keyIndexMap.put(key.toLowerCase(), size++);
        }
        this.size = size;
    }

    public MacroProcessor(String... keys)
    {
        this(Arrays.asList(keys));
    }

    public String process(String message, String... values)
    {
        if (values.length != size)
        {
            throw new IllegalArgumentException("The number of values does not match the number of macros!");
        }

        StringBuilder finalString = new StringBuilder();

        char[] chars = message.toCharArray();
        for (int i = 0; i < chars.length; ++i)
        {
            switch (chars[i])
            {
                case '\\':
                    if (i + 1 < chars.length)
                    {
                        finalString.append(chars[++i]);
                        break;
                    }
                case '{':
                    i = replaceVar(finalString, chars, i + 1, values);
                    break;
                default:
                    finalString.append(chars[i]);
            }
        }

        return finalString.toString();
    }

    private int replaceVar(StringBuilder out, char[] in, int offset, String[] values)
    {
        int i = offset;
        String name = "";
        boolean done = false;
        for (; i < in.length && !done; ++i)
        {
            switch (in[i])
            {
                case '}':
                    done = true;
                    --i;
                    break;
                default:
                    name += in[i];
            }
        }

        Integer key = this.keyIndexMap.get(name.toLowerCase());
        if (key != null && key >= 0 && key < values.length)
        {
            out.append(values[key]);
        }

        return i;
    }
}
