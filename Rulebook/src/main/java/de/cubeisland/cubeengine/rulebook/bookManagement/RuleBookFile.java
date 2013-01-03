package de.cubeisland.cubeengine.rulebook.bookManagement;

import de.cubeisland.cubeengine.core.CubeEngine;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuleBookFile
{
    private final static int NumberOfCharsPerPage = 260;
    private final static int NumberOfCharsPerLine = 20;

    public static File loadFile(String parent, String child)
    {
        return new File(parent, child);
    }

    public static Set<File> getLanguageFiles(File directory)
    {
        Set<File> files = new HashSet<File>();
        if (directory.isDirectory())
        {
            for (File file : directory.listFiles())
            {
                Set<Language> languages = CubeEngine.getI18n().searchLanguages(StringUtils.stripFileExtension(file.getName()));

                if (languages.size() == 1)
                {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public static String[] convertToPages(File file) throws IOException
    {
        return convertToBookPageArray(convertToLines(readFile(file)));
    }

    private static String readFile(File file) throws IOException
    {
        if (!file.exists())
        {
            createFile(file, "");
        }

        String text = "";
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while ((line = reader.readLine()) != null)
        {
            text += (line + "\n");
        }
        reader.close();

        return text;
    }

    public static void createFile(File file, String[] txt) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (String page : txt)
        {
            writer.write(page);
        }
        writer.close();
    }

    public static void createFile(File file, String txt) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(txt);
        writer.close();
    }

    private static String[] convertToBookPageArray(List<String> lines)
    {
        List<String> pages = new ArrayList<String>();
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
        List<String> lines = new ArrayList<String>();
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
