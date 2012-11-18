package de.cubeisland.cubeengine.core.filesystem;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class provides some methods to read Files lineByLine
 * and saving a String into a File.
 */
public class FileUtil
{
    private static final Logger LOGGER = CubeEngine.getLogger();

    /**
     * Reads the file line by line and returns a list of Strings containing all lines
     *
     * @param file the file
     * @return
     */
    public static List<String> readStringList(File file)
    {
        try
        {
            if (file == null)
            {
                return null;
            }
            return readStringList(new FileReader(file));
        }
        catch (FileNotFoundException ex)
        {
            return null;
        }
    }

    /**
     * Reads the inputstream line by line and returns a list of Strings containing all lines
     *
     * @param stream the inputstream
     * @return
     */
    public static List<String> readStringList(InputStream stream)
    {
        if (stream == null)
        {
            return null;
        }
        return readStringList(new InputStreamReader(stream));
    }

    /**
     * Reads the reader line by line and returns a list of Strings containing all lines
     *
     * @param reader the reader
     * @return
     */
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
        catch (IOException e)
        {
            LOGGER.log(LogLevel.ERROR, e.getMessage(), e);
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

    /**
     * Saves given String into the file
     *
     * @param string the string
     * @param file   the file to save to
     * @throws IOException
     */
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
