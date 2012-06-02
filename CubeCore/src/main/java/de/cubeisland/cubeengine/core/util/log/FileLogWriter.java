package de.cubeisland.cubeengine.core.util.log;

import java.io.FileWriter;
import java.io.File;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class FileLogWriter implements LogWriter
{
    private FileWriter writer;
    private File logFile;
    
    public FileLogWriter(String filePath)
    {
        try
        {
            logFile = new File(filePath);
            writer = new FileWriter(filePath);
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    public void clearLog()
    {
        try
        {
            writer.close();
            writer = null;
            logFile.delete();
            writer = new FileWriter(logFile.getPath());
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    public void write(String text)
    {
        try
        {
            writer.append(text);
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
}
