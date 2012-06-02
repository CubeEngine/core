package de.cubeisland.cubeengine.core.util.log;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class Logger 
{
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.S");   //[datum] [module] [type] message
    private ArrayList<LogWriter> writerList;
    private Date date;
    
    public Logger()
    {
        this.date = new Date();
        this.writerList = new ArrayList<LogWriter>();
    }
    
    public void addLogWriter(LogWriter writer)
    {
        this.writerList.add(writer);
    }
       
    public void log(String msg, LogType type)
    {
        String logEntry = "[" + dateFormat.format(this.date) + "] [" + type.getType() + "] - " + msg;
        try
        {
            for(LogWriter writer: writerList)
            {
                writer.write(logEntry);
            }
        }
        catch(Exception ex)
        {
             ex.printStackTrace(System.err);
        }
    }
}
