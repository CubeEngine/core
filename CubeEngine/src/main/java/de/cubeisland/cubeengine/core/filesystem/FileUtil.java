package de.cubeisland.cubeengine.core.filesystem;

import de.cubeisland.cubeengine.core.CubeEngine;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtil
{
    private static final Logger LOGGER = CubeEngine.getLogger();
    
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

    public static List<String> readStringList(Reader reader)
    {
        // TODO I don't think a slient fail is a good idea here...
        if (reader == null)
        {
            return null;
        }
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        try
        {
            while ((line = bufferedReader.readLine()) != null)
            {
                list.add(line);
            }
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalStateException("Could not find the File!", e);
        }
        catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        finally
        {
            try
            {
                bufferedReader.close();
            }
            catch (IOException ex1)
            {}
        }
        return list;
    }

    public static void saveFile(String string, File file) throws IOException
    {
        FileWriter fw = new FileWriter(file);
        try
        {
            fw.write(string);
        }
        finally
        {
            fw.close();
        }
    }
}
