package de.cubeisland.cubeengine.core.util.log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class CubeLogger
{
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.S");   //[datum] [module] [type] message
    private ArrayList<LogWriter> writerList;
    private Date date;
    
    private static Logger logger;
    
    public CubeLogger(JavaPlugin plugin)
    {
        this.date = new Date();
        this.writerList = new ArrayList<LogWriter>();
        
        logger = plugin.getLogger();
    }
    
    public void addLogWriter(LogWriter writer)
    {
        this.writerList.add(writer);
    }
       
    public void log(String msg, LogType type)
    {
        String logEntry = type + " - " + msg; // [Type] - message
        //ConsoleLogWriter always writes TIME [INFOMRATION] ...
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
