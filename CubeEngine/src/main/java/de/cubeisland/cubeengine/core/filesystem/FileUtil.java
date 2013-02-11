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
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.logger.LogLevel.ERROR;

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
     * @return a list of lines
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
     * Reads the InputStream line by line and returns a list of Strings containing all lines
     *
     * @param stream the InputStream
     * @return a list of lines
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
     * @return a list of lines
     */
    public static List<String> readStringList(Reader reader)
    {
        if (reader == null)
        {
            return null;
        }
        List<String> list = new LinkedList<String>();
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
            LOGGER.log(ERROR, e.getMessage(), e);
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

    public static String readToString(InputStream stream, Charset charset)
    {
        StringBuilder builder = new StringBuilder();

        byte[] buffer = new byte[512];
        int bytesRead;
        do
        {
            try
            {
                bytesRead = stream.read(buffer);
                if (bytesRead > 0)
                {
                    builder.append(new String(buffer, 0, bytesRead, charset));
                }
            }
            catch (IOException e)
            {
                break;
            }
        }
        while (bytesRead > 0);

        return builder.toString();
    }
}
