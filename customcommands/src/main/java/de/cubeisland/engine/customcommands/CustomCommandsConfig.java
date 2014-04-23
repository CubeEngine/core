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
package de.cubeisland.engine.customcommands;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import de.cubeisland.engine.reflect.ReflectedYaml;

import static java.util.Locale.ENGLISH;

@SuppressWarnings("all")
public class CustomCommandsConfig extends ReflectedYaml
{
    public HashMap<String, String> commands = new HashMap<>();
    public boolean surpressMessage;

    @Override
    public void onLoaded(File loadedFrom)
    {
        HashMap<String, String> dummyMap = new HashMap<>();

        for(Entry<String, String> entry : commands.entrySet())
        {
            dummyMap.put(entry.getKey().toLowerCase(ENGLISH), entry.getValue());
        }

        commands = dummyMap;
    }
}
