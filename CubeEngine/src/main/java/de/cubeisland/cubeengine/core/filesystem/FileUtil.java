package de.cubeisland.cubeengine.core.filesystem;

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
            breader.close();
            return list;
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalStateException("Could not find the File!", e);
        }
        finally
        {
            reader.close();
        }
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
