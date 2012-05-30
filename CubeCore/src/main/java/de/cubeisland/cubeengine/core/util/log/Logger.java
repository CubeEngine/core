package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.module.Module;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class Logger 
{
    public static final byte LOGLEVEL2 = 2; //Log into file
    public static final byte LOGLEVEL1 = 1; //Log into console
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.S");   //[datum] [module] [type] message
    private String moduleName;
    private Date date;
    private FileLogWriter fileLogWriter;
    
    public Logger(Module module)
    {
        File logDir = module.getCore().getFileManager().getLogDir();
        this.moduleName = module.getModuleName();
        this.date = new Date();
        
       this.fileLogWriter = new FileLogWriter(logDir.getAbsolutePath() + moduleName + ".log"); 
    }
    
    public void msg(String msg, byte logLevel)
    {
        String dateStr = dateFormat.format(this.date);
        try
        {  
            if(logLevel == LOGLEVEL1)
            {
                System.out.println(" [" + this.moduleName + "] [MSG] " + msg);
            }
            else if(logLevel == LOGLEVEL2)
            {
                fileLogWriter.write("[" + dateStr + "] [" + this.moduleName + "] [MSG] " + msg);
            }
            else if(logLevel == LOGLEVEL1 + LOGLEVEL2)
            {
                System.out.println(" [" + this.moduleName + "] [MSG] " + msg);
                fileLogWriter.write("[" + dateStr + "] [" + this.moduleName + "] [MSG] " + msg);
            }
        }
        catch(Exception ex)
        {
             ex.printStackTrace(System.err);
        }
    }
    
    public void notification(String msg, byte logLevel)
    {
        String dateStr = dateFormat.format(this.date);
        try
        {
            if(logLevel == LOGLEVEL1)
            {
                System.out.println(" [" + this.moduleName + "] [NOTIFICATION] " + msg);
            }
            else if(logLevel == LOGLEVEL2)
            {
                fileLogWriter.write("[" + dateStr + "] [" + this.moduleName + "] [NOTIFICATION] " + msg);
            }
            else if(logLevel == LOGLEVEL1 + LOGLEVEL2)
            {
                System.out.println(" [" + this.moduleName + "] [MSG] " + msg);
                fileLogWriter.write("[" + dateStr + "] [" + this.moduleName + "] [NOTIFICATION] " + msg);
            }
        }
        catch(Exception ex)
        {
             ex.printStackTrace(System.err);
        }
    }
    
    public void warning(String msg, byte logLevel)
    {
        String dateStr = dateFormat.format(this.date);
        try
        {
            if(logLevel == LOGLEVEL1)
            {
                System.out.println(" [" + this.moduleName + "] [WARNING] " + msg);
            }
            else if(logLevel == LOGLEVEL2)
            {
                fileLogWriter.write("[" + dateStr + "] [" + this.moduleName + "] [WARNING] " + msg);
            }
            else if(logLevel == LOGLEVEL1 + LOGLEVEL2)
            {
                System.out.println(" [" + this.moduleName + "] [MSG] " + msg);
                fileLogWriter.write("[" + dateStr + "] [" + this.moduleName + "] [WARNING] " + msg);
            }
        }
        catch(Exception ex)
        {
             ex.printStackTrace(System.err);
        }
    }
       
    public void error(String msg, byte logLevel)
    {
        String dateStr = dateFormat.format(this.date);
        try
        {
            if(logLevel == LOGLEVEL1)
            {
                System.out.println(" [" + this.moduleName + "] [ERROR] " + msg);
            }
            else if(logLevel == LOGLEVEL2)
            {
                fileLogWriter.write("[" + dateStr + "] [" + this.moduleName + "] [ERROR] " + msg);
            }
            else if(logLevel == LOGLEVEL1 + LOGLEVEL2)
            {
                System.out.println(" [" + this.moduleName + "] [MSG] " + msg);
                fileLogWriter.write("[" + dateStr + "] [" + this.moduleName + "] [ERROR] " + msg);
            }
        }
        catch(Exception ex)
        {
             ex.printStackTrace(System.err);
        }
    }
}
