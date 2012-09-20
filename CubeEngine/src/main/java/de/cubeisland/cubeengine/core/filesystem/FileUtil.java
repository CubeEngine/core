package de.cubeisland.cubeengine.core.filesystem;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class FileUtil
{
    public static List<String> readStringList(InputStream stream) throws IOException
    {
        if (stream == null)
        {
            return null;
        }
        return readStringList(new InputStreamReader(stream));
    }

    public static List<String> readStringList(File file) throws IOException
    {
        if (file == null)
        {
            return null;
        }
        return readStringList(new FileReader(file));
    }

    public static List<String> readStringList(Reader reader) throws IOException
    {
        try
        {
            ArrayList<String> list = new ArrayList<String>();
            BufferedReader breader = new BufferedReader(reader);
            String line;
            while ((line = breader.readLine()) != null)
            {
                list.add(line);
            }
            return list;
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalStateException("Could not find the File!", e);
        }
    }

    public static boolean parseStringList(File file, TIntObjectHashMap<List<String>> map, boolean update) throws IOException
    {
        return parseStringList(readStringList(file), map, update);
    }

    public static boolean parseStringList(InputStream stream, TIntObjectHashMap<List<String>> map, boolean update) throws IOException
    {
        return parseStringList(readStringList(stream), map, update);
    }

    public static boolean parseStringList(List<String> input, TIntObjectHashMap<List<String>> map, boolean update) throws IOException
    {
        Validate.notNull(input,"Invalid input! File or Reader was null!");
        Validate.notNull(map,"Map to parse into was null!");
        boolean updated = false;
        ArrayList<String> names = new ArrayList<String>();
        for (String line : input)
        {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            if (line.endsWith(":"))
            {
                Integer id = Integer.parseInt(line.substring(0, line.length() - 1));
                names = new ArrayList<String>();
                if (!update)
                {
                    map.put(id, names);
                }
                else if (map.get(id) == null || map.get(id).isEmpty())
                {
                    map.put(id, names);
                    updated = true;
                }
            }
            else
            {
                names.add(line);
            }
        }
        return updated;
    }

    public static void saveFile(String string, File file) throws IOException
    {
        try
        {
            FileWriter fw = new FileWriter(file);
            fw.write(string);
            fw.close();
        }
        catch (FileNotFoundException ex)
        {
            throw new IllegalStateException("Could not find the File!", ex);
        }
    }
}
