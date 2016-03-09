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

import java.io.IOException;
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
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermissions.fromString;


public class FileUtil
{
    public static final Set<PosixFilePermission> DEFAULT_FOLDER_PERMS = fromString("rwxrwxr-x");
    private static final Set<PosixFilePermission> READ_ONLY_PERM = PosixFilePermissions.fromString("r--r-----");

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
