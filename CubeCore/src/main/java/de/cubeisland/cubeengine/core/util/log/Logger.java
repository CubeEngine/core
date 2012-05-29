package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.CubeCore;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class logger 
{
    private FileWriter writer;
    private String moduleName;

    public logger(CubeCore core, String moduleName)
    {
        this.moduleName = moduleName;
        File logDir = core.getFileManager().getLogDir();
        
        try
        {
            writer = new FileWriter(logDir.getPath() + this.moduleName + ".log");
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    public void log(String msg)
    {
        try
        {
            writer.append(msg);
        }
        catch(Exception ex)
        {
             ex.printStackTrace(System.err);
        }
    }
}
