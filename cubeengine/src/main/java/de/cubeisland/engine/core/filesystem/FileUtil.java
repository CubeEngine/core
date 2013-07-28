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
package de.cubeisland.engine.core.filesystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;


/**
 * This class provides some methods to read Files lineByLine
 * and saving a String into a File.
 */
public class FileUtil
{
    /**
     * Reads the file line by line and returns a list of Strings containing all lines
     *
     * @param file the file
     * @return a list of lines
     */
    public static List<String> readStringList(Path file)
    {
        assert file != null: "The file must not be null!";
        try (BufferedReader reader = Files.newBufferedReader(file, Core.CHARSET))
        {
            return readStringList(reader);
        }
        catch (IOException ex)
        {
            return null;
        }
    }

    /**
     * Reads the InputStream line by line and returns a list of Strings containing all lines
     *
     * @param stream the InputStream
     * @return a list of lines
     */
    public static List<String> readStringList(InputStream stream)
    {
        assert stream != null: "The stream may not be null!";

        return readStringList(new BufferedReader(new InputStreamReader(stream)));
    }

    /**
     * Reads the reader line by line and returns a list of Strings containing all lines
     *
     * @param reader the reader
     * @return a list of lines
     */
    public static List<String> readStringList(BufferedReader reader)
    {
        if (reader == null)
        {
            return null;
        }
        List<String> list = new LinkedList<>();
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        try
        {
            while ((line = bufferedReader.readLine()) != null)
            {
                list.add(line);
            }
        }
        catch (IOException e)
        {
            CubeEngine.getLog().error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                bufferedReader.close();
            }
            catch (IOException ignore)
            {}
        }
        return list;
    }

    /**
     * Saves given String into the file
     *
     * @param string the string
     * @param file   the file to save to
     * @throws IOException
     */
    public static void saveFile(String string, Path file) throws IOException
    {
        try (Writer out = Files.newBufferedWriter(file, Core.CHARSET))
        {
            out.write(string);
        }
    }

    public static String readToString(ReadableByteChannel in, Charset charset) throws IOException
    {
        StringBuilder builder = new StringBuilder();

        ByteBuffer buffer = ByteBuffer.allocate(2048);
        while (in.read(buffer) >= 0 || buffer.position() > 0)
        {
            buffer.flip();
            builder.append(new String(buffer.array(), 0, buffer.position(), charset));
            buffer.compact();
        }

        return builder.toString();
    }
}
