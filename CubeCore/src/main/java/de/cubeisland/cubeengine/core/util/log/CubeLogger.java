package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.CubeCore;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Robin Bechtel-Ostmann
 */
public class CubeLogger
{
    private static HashMap<String, Logger> loggers = new HashMap<String, Logger>();

    public CubeLogger()
    {
        Logger coreLogger = addLogger(CubeCore.getInstance());
        addFileHandler(coreLogger, "CubeEngineLogs.log");
    }

    public static void addHandler(String plugin, Handler newHandler)
    {
        loggers.get(plugin).addHandler(newHandler);
    }

    public static void addFileHandler(Logger logger, String filename)
    {
        try
        {
            Handler fileHandler = new FileHandler(filename);
            fileHandler.setLevel(Level.WARNING);
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

    public static Logger addLogger(JavaPlugin plugin)
    {
        Logger logger = plugin.getLogger();
        loggers.put(plugin.getName(), logger);
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
