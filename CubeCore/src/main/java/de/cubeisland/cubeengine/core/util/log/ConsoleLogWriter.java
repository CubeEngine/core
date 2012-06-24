package de.cubeisland.cubeengine.core.util.log;

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

