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
package de.cubeisland.engine.core.filesystem.gettext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.Map;

import de.cubeisland.engine.core.Core;
import gnu.trove.map.hash.THashMap;

public class PlaintextMessageCatalog implements MessageCatalog
{
    private final Path file;

    public PlaintextMessageCatalog(Path file)
    {
        this.file = file;
    }

    public Map<String, String> read(ReadableByteChannel channel) throws IOException
    {
        Map<String, String> messages = new THashMap<>();
        BufferedReader reader = new BufferedReader(Channels.newReader(channel, Core.CHARSET.name()));

        String line;
        int spaceOffset;

        String currentId = null;

        while ((line = reader.readLine()) != null)
        {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#')
            {
                continue;
            }
            spaceOffset = line.indexOf(' ');
            if (spaceOffset == -1)
            {
                continue;
            }
            String type = line.substring(0, spaceOffset);
            String text = line.substring(spaceOffset).trim();
            if (text.length() > 2 && text.charAt(text.length() - 1) == text.charAt(0))
            {
                text = text.substring(1, text.length() - 1);
            }

            if (currentId == null)
            {
                if (type.equalsIgnoreCase("msgid"))
                {
                    currentId = text;
                }
            }
            else
            {
                messages.put(currentId, text);
                currentId = null;
            }
        }

        return messages;
    }

    public Map<String, String> read() throws IOException
    {
        try (FileChannel in = FileChannel.open(this.file))
        {
            return this.read(in);
        }
    }

    public void write(Map<String, String> messages) throws IOException
    {

    }

    public void write(OutputStream outputStream, Map<String, String> messages) throws IOException
    {

    }
}
