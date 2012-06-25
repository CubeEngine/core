package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.CubeCore;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Robin Bechtel-Ostmann
 * @author Faithcaio
 */
public class CubeLogger
{
    private static HashMap<String, Logger> loggers = new HashMap<String, Logger>();
    private static final CubeCore core;

    static
    {
        core = CubeCore.getInstance();
        
        //Init CubeCoreLogger
        Logger coreLogger = addLogger(core.getName());
        //Console logs INFO and higher
        addConsoleHandler(coreLogger, Level.INFO);
        //own LogFile logs WARNING and higher
        addFileHandler(coreLogger, "CubeEngineLogs.log", Level.WARNING);
        //Database logs logs SEVERE and higher
        addDatabaseHandler(coreLogger, "log", Level.SEVERE);
        //Logger does NOT use Parent Logger
        coreLogger.setUseParentHandlers(false);
    }
    
    public static void addHandler(String plugin, Handler newHandler)
    {
        loggers.get(plugin).addHandler(newHandler);
    }
            
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

    public static void addConsoleHandler(String plugin, Handler newHandler)
    {
        loggers.get(plugin).addHandler(newHandler);
    }

    public static Logger addLogger(String plugin)
    {
        Logger logger = Logger.getLogger(plugin);
        loggers.put(plugin, logger);
        return logger;
    }

    public void log(String plugin, String msg, Level loglevel)
    {
        
        loggers.get(plugin).log(loglevel, msg, plugin);
    }

    public void log(String msg, Level loglevel)
    {
        loggers.get("CubeCore").log(loglevel, msg, "CubeEngine");
    }

    public void log(String msg)
    {
        loggers.get("CubeCore").log(Level.INFO, msg, "CubeEngine");
    }

    public static Logger getLogger(String plugin)
    {
        return loggers.get(plugin);
    }
}
