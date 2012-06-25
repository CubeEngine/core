package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.CubeCore;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerManager
{
    private static final CubeCore core;
    
    /**
     * initialize the CoreLogger
     */
    static
    {
        core = CubeCore.getInstance();
        //Init CubeCoreLogger
        Logger coreLogger = getLogger(core.getName());
        //Console logs INFO and higher
        addConsoleHandler(coreLogger, Level.INFO);
        //own LogFile logs WARNING and higher
        addFileHandler(coreLogger, "CubeEngineLogs.log", Level.WARNING);
        //Database logs logs SEVERE and higher
        addDatabaseHandler(coreLogger, "log", Level.SEVERE);
        //Logger does NOT use Parent Logger
        coreLogger.setUseParentHandlers(false);
    }
    
    /**
     * Adds a DatabaseHandler to the specified Logger
     * 
     * @param logger the logger to add the Handler to
     * @param tablename the tablename to log into
     * @param level the minimum loglvl needed to log
     */    
    public static void addDatabaseHandler(Logger logger, String tablename, Level level)
    {
        try
        {
            DatabaseHandler dbHandler = new DatabaseHandler(core.getDB(), tablename);
            dbHandler.setLevel(level);
            logger.addHandler(dbHandler);
            //TODO remove:
            dbHandler.clearLog();
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Could not add FileHandler to Logger");
        }
    }
    
    /**
     * Adds a ConsoleHandler to the specified Logger
     * 
     * @param logger the logger to add the Handler to
     * @param level the minimum loglvl needed to log
     */ 
    public static void addConsoleHandler(Logger logger, Level level)
    {
        try
        {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(level);
            consoleHandler.setFormatter(new ConsoleFormatter());
            logger.addHandler(consoleHandler);
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Could not add FileHandler to Logger");
        }
    }
   
    /**
     * Adds a DataBaseHandler to the specified Logger
     * 
     * @param logger the logger to add the Handler to
     * @param filename the name of the file to log into
     * @param level the minimum loglvl needed to log
     */ 
    public static void addFileHandler(Logger logger, String filename, Level level)
    {
        try
        {
            FileHandler fileHandler = new FileHandler(filename, true);
            fileHandler.setLevel(level);
            fileHandler.setFormatter(new FileFormatter());
            logger.addHandler(fileHandler);
            
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Could not add FileHandler to Logger");
        }
    }

    /**
     * logs a message
     * 
     * @param plugin the plugin/logger name
     * @param msg the message to log
     * @param loglevel the Level of this log
     */
    public void log(String plugin, String msg, Level loglevel)
    {
        
        getLogger(plugin).log(loglevel, msg, plugin);
    }

    /**
     * logs a message with the default CubeCore-Logger
     * 
     * @param msg the message to log
     * @param loglevel the Level of this log
     */
    public void log(String msg, Level loglevel)
    {
        getLogger("CubeCore").log(loglevel, msg, "CubeCore");
    }

    /**
     * logs a message with the default CubeCore-Logger on INFO level
     * 
     * @param msg the message to log
     */
    public void log(String msg)
    {
        getLogger("CubeCore").log(Level.INFO, msg, "CubeCore");
    }

    /**
     * Proxy-Method 
     * gets a Logger
     * 
     * @param plugin the LoggerName
     * @return the Logger
     */
    public static Logger getLogger(String plugin)
    {
        return Logger.getLogger(plugin);
    }
}
