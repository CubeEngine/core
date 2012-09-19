package de.cubeisland.cubeengine.core.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Anselm Brehme
 */
public class FileUtil
{
    public static List<String> getFileAsStringList(File file) throws IOException
    {
        return getReaderAsStringList(new FileReader(file));
    }
    
    public static List<String> getReaderAsStringList(Reader reader) throws IOException
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

    public static void saveFile(String s, File file) throws IOException
    {
        try
        {
            FileWriter fw = new FileWriter(file);
            fw.write(s);
            fw.close();
        }
        catch (FileNotFoundException ex)
        {
            throw new IllegalStateException("Could not find the File!", ex);
        }
    }
}
