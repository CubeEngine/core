package de.cubeisland.cubeengine.core.util.log;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public enum LogType
{
    MESSAGE("MSG"),
    NOTIFICATION("NOTICE"),
    WARNING("WARNING"),
    ERROR("ERR");
    
    private String type;
    
    private LogType(String type)
    {
        this.type = type;
    }
    
    public String getType()
    {
        return this.type;
    }
}
