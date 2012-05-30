package de.cubeisland.cubeengine.core.util.log;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public interface LogWriter
{
    public void clearLog();
    
    public void write(String text);
}
