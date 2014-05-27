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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import de.cubeisland.engine.core.filesystem.FileUtil;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

/**
 * This class provides methods to convert the AliasMaps used for items.txt etc. into a TreeMap and back.
 */
public class AliasMapFormat
{
    public static boolean parseStringList(Path file, TreeMap<String, List<String>> map, boolean update) throws IOException
    {
        return parseStringList(FileUtil.readStringList(file), map, update);
    }

    public static boolean parseStringList(InputStream stream, TreeMap<String, List<String>> map, boolean update) throws IOException
    {
        return parseStringList(FileUtil.readStringList(stream), map, update);
    }

    public static boolean parseStringList(List<String> input, TreeMap<String, List<String>> map, boolean update) throws IOException
    {
        expectNotNull(input, "Invalid input! File or Reader was null!");
        expectNotNull(map, "Map to parse into was null!");
        boolean updated = false;
        ArrayList<String> names = new ArrayList<>();
        for (String line : input)
        {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            if (line.endsWith(":"))
            {
                String key = line.substring(0, line.length() - 1);
                names = new ArrayList<>();
                if (!update)
                {
                    map.put(key, names);
                }
                else
                {
                    if (map.get(key) == null || map.get(key).isEmpty())
                    {
                        map.put(key, names);
                        updated = true;
                    }
                }
            }
            else
            {
                names.add(line);
            }
        }
        return updated;
    }

    public static void parseAndSaveStringListMap(TreeMap<String, List<String>> map, Path file) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet())
        {
            sb.append(key).append(":").append("\n");
            List<String> entitynames = map.get(key);
            for (String entityname : entitynames)
            {
                sb.append("    ").append(entityname).append("\n");
            }
        }
        FileUtil.saveFile(sb.toString(), file);
    }
}
