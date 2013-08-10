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
package de.cubeisland.engine.rulebook.bookManagement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.filesystem.FileUtil;
import de.cubeisland.engine.core.i18n.Language;
import de.cubeisland.engine.core.util.StringUtils;

public class RuleBookFile
{
    private final static int NumberOfCharsPerPage = 260;
    private final static int NumberOfCharsPerLine = 20;

    public static Path loadFile(String parent, String child)
    {
        return Paths.get(parent, child);
    }

    public static Set<Path> getLanguageFiles(Path directory)
    {
        Set<Path> files = new HashSet<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory))
        {
            for (Path file : directoryStream)
            {
                Set<Language> languages = CubeEngine.getI18n().searchLanguages(StringUtils.stripFileExtension(file.getFileName().toString()));

                if (languages.size() == 1)
                {
                    files.add(file);
                }
            }
        }
        catch (IOException ignored)
        {}
        return files;
    }

    public static String[] convertToPages(Path file) throws IOException
    {
        return convertToBookPageArray(convertToLines(readFile(file)));
    }

    private static String readFile(Path file) throws IOException
    {
        if (!Files.exists(file))
        {
            Files.createFile(file);
            return "";
        }

        try (FileChannel in = FileChannel.open(file))
        {
            return FileUtil.readToString(in, Core.CHARSET);
        }
    }

    public static void createFile(Path file, String[] txt) throws IOException
    {
        createFile(file, StringUtils.implode("\n", txt));
    }

    public static void createFile(Path file, String txt) throws IOException
    {
        try (BufferedWriter writer = Files.newBufferedWriter(file, Core.CHARSET))
        {
            writer.write(txt);
        }
    }

    private static String[] convertToBookPageArray(List<String> lines)
    {
        List<String> pages = new ArrayList<>();
        pages.add("");
        int page = 0;

        for (String line : lines)
        {
            if ((getNumberOfLines(pages.get(page)) + getNumberOfLines(line)) > 13)
            {
                page++;
                pages.add("");
            }
            if (pages.get(page).length() == 0)
            {
                pages.set(page, line);
            }
            else
            {
                pages.set(page, pages.get(page) + "\n" + line);
            }
        }

        return pages.toArray(new String[pages.size()]);
    }

    private static List<String> convertToLines(String text)
    {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n"))
        {
            line = line.trim();
            while (line.length() > NumberOfCharsPerPage)
            {
                int index = line.substring(0, NumberOfCharsPerPage).lastIndexOf(" ");
                if (index == -1)
                {
                    index = NumberOfCharsPerPage;
                }
                lines.add(line.substring(0, index));
                line = line.substring(index).trim();
            }
            lines.add(line);
        }
        return lines;
    }

    public static int getNumberOfLines(String string)
    {
        return (int)Math.ceil((double)string.length() / (double)NumberOfCharsPerLine);
    }

    public static int getNumberOfLines(String[] strings)
    {
        int result = 0;
        for (String string : strings)
        {
            result += getNumberOfLines(string);
        }
        return result;
    }
}
