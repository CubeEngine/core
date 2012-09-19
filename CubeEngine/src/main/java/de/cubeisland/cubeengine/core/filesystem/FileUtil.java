package de.cubeisland.cubeengine.core.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Anselm Brehme
 */
public class FileUtil
{
    public static List<String> getFileAsStringList(File file) throws FileNotFoundException, IOException
    {
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null)
        {
            list.add(line);
        }
        return list;
    }
}
