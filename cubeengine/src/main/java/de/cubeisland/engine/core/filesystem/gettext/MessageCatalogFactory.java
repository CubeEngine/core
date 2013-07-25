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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class MessageCatalogFactory
{
    public MessageCatalogFactory()
    {}

    public MessageCatalog newMessageCatalog(String path)
    {
        return this.newMessageCatalog(new File(path));
    }

    public MessageCatalog newMessageCatalog(File file)
    {
        try
        {
            if (this.isBinaryCatalog(file))
            {
                return new BinaryMessageCatalog(file);
            }
            return new PlaintextMessageCatalog(file);
        }
        catch (FileNotFoundException ignored)
        {}
        if (file.getName().endsWith(".mo"))
        {
            return new BinaryMessageCatalog(file);
        }
        return new PlaintextMessageCatalog(file);
    }

    public boolean isBinaryCatalog(File file) throws FileNotFoundException
    {
        if (!file.exists())
        {
            throw new FileNotFoundException(file.getName());
        }
        FileInputStream stream = null;
        FileChannel channel = null;
        try
        {
            stream = new FileInputStream(file);
            channel = stream.getChannel();
            ByteBuffer buf = ByteBuffer.allocateDirect(4);
            int bytesRead = channel.read(buf);
            if (bytesRead < 4)
            {
                return false;
            }
            buf.rewind();
            int signature = buf.getInt();
            if (signature == BinaryMessageCatalog.SIGNATURE_LITTLE)
            {
                return true;
            }
            else if (signature == BinaryMessageCatalog.SIGNATURE_BIG)
            {
                return true;
            }
        }
        catch (Exception ignored)
        {}
        finally
        {
            if (channel != null)
            {
                try
                {
                    channel.close();
                }
                catch (IOException ignored)
                {}
            }
            if (stream != null)
            {
                try
                {
                    stream.close();
                }
                catch (IOException ignored)
                {}
            }
        }
        return false;
    }
}
