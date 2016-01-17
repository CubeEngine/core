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
package org.cubeengine.service.filesystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.google.common.base.Preconditions;
import org.cubeengine.module.core.CoreModule;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.attribute.PosixFilePermissions.fromString;


/**
 * This class provides some methods to read Files lineByLine
 * and saving a String into a File.
 */
public class FileUtil
{
    public static final Set<PosixFilePermission> DEFAULT_FOLDER_PERMS = fromString("rwxrwxr-x");
    private static final Set<PosixFilePermission> READ_ONLY_PERM = PosixFilePermissions.fromString("r--r-----");

    /**
     * Reads the file line by line and returns a list of Strings containing all lines
     *
     * @param file the file
     * @return a list of lines
     */
    public static List<String> readStringList(Path file) throws IOException
    {
        checkNotNull(file, "The file must not be null!");
        try (BufferedReader reader = Files.newBufferedReader(file, CoreModule.CHARSET))
        {
            return readStringList(reader);
        }
    }

    /**
     * Reads the InputStream line by line and returns a list of Strings containing all lines
     *
     * @param stream the InputStream
     * @return a list of lines
     */
    public static List<String> readStringList(InputStream stream) throws IOException
    {
        checkNotNull(stream, "The stream may not be null!");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream)))
        {
            return readStringList(br);
        }
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
        catch (IOException ex)
        {
            // TODO getLog().debug(ex, "Failed to read lines from a buffer");
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
        try (Writer out = Files.newBufferedWriter(file, CoreModule.CHARSET))
        {
            out.write(string);
        }
    }

    public static String readToString(ReadableByteChannel in, Charset charset) throws IOException
    {
        StringBuilder builder = new StringBuilder();

        ByteBuffer buffer = ByteBuffer.allocate(2048);
        while (in.read(buffer) != -1)
        {
            builder.append(new String(buffer.array(), 0, buffer.position(), charset));
            buffer.flip();
        }
        return builder.toString();
    }

    public static boolean hideFile(Path path)
    {
        try
        {
            DosFileAttributeView attributeView = Files.getFileAttributeView(path, DosFileAttributeView.class);
            attributeView.setHidden(true);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public static boolean setReadOnly(Path file)
    {
        try
        {
            Files.getFileAttributeView(file, PosixFileAttributeView.class).setPermissions(READ_ONLY_PERM);
        }
        catch (Exception ignore)
        {
            try
            {
                Files.getFileAttributeView(file, DosFileAttributeView.class).setReadOnly(true);
            }
            catch (Exception  ignored)
            {
                return false;
            }
        }
        return true;
    }

    public static void copy(ReadableByteChannel in, WritableByteChannel out) throws IOException
    {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 4);
        while (in.read(buffer) != -1)
        {
            out.write(buffer);
            buffer.flip();
        }
    }
}
