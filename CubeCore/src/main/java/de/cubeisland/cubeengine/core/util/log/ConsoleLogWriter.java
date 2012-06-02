package de.cubeisland.cubeengine.core.util.log;

import java.io.File;
import java.io.FileWriter;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class ConsoleLogWriter implements LogWriter
{
    public ConsoleLogWriter()
    {
        
    }
    
    public void clearLog()
    {
       
    }
    
    public void write(String text)
    {
        System.out.println(text);
    }

}

