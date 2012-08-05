package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.persistence.database.Database;
import java.io.File;
import java.text.MessageFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Anselm Brehme
 */
public class CubeLogger extends Logger
{
    /**
     * Creates a new Logger by this name
     *
     * @param name the name
     */
    public CubeLogger(String name)
    {
        super("[" + name + "]", null);
        Logger bukkitlogger = Logger.getLogger(name);
        this.setParent(bukkitlogger);
        this.setUseParentHandlers(false);
    }

    /**
     *
     * @param database the database to log into
     * @param tablename the tablename to log into
     * @param level the minimum loglvl needed to log
     */
    public CubeLogger addDatabaseHandler(Database database, String tablename, Level level)
    {
        try
        {
            DatabaseHandler dbHandler = new DatabaseHandler(database, tablename);
            dbHandler.setLevel(level);
            this.addHandler(dbHandler);
            return this;
        }
        catch (SecurityException e)
        {
            this.log(Level.SEVERE, "Could not add DatabaseHandler to Logger", e);
            return this;
        }
    }

    /**
     * Adds a ConsoleHandler to the specified Logger (Does not work with along
     * with Bukkit-Logger!)
     *
     * @param level the minimum loglvl needed to log
     */
    public CubeLogger addConsoleHandler(Level level)
    {
        //Do not use this Handler yet!
        try
        {
            this.setUseParentHandlers(false);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(level);
            consoleHandler.setFormatter(new ConsoleFormatter());
            this.addHandler(consoleHandler);
            return this;
        }
        catch (SecurityException e)
        {
            this.log(Level.SEVERE, "Could not add ConsoleHandler to Logger", e);
            return this;
        }
    }

    /**
     * Adds a FileHandler to this Logger
     *
     * @param filename the name of the file to log into
     * @param level the minimum loglvl needed to log
     */
    public CubeLogger addFileHandler(File file, Level level)
    {
        try
        {
            FileHandler fileHandler = new FileHandler(file.getPath(), true);
            fileHandler.setLevel(level);
            fileHandler.setFormatter(new FileFormatter());
            this.addHandler(fileHandler);
            return this;

        }
        catch (Exception e)
        {
            this.log(Level.SEVERE, "Could not add FileHandler to Logger", e);
            return this;
        }
    }

    /**
     * Adds a RemoteHandler to this Logger
     *
     * @param level the minimum loglvl needed to log
     */
    public CubeLogger addRemoteHandler(Level level)
    {
        try
        {
            Handler remoteHandler = new RemoteHandler();
            remoteHandler.setLevel(level);
            this.addHandler(remoteHandler);
            return this;
        }
        catch (SecurityException e)
        {
            this.log(Level.SEVERE, "Could not add RemoteHandler to Logger", e);
            return this;
        }
    }

    //Pass ConsoleLogging to BukkitLogger
    @Override
    public void log(LogRecord record)
    {
        String msg = record.getMessage();
        Object[] params = record.getParameters();
        if (!(params == null || params.length == 0))
        {
            Pattern.compile("\\{\\d").matcher(msg).find();
            msg = MessageFormat.format(msg, record.getParameters());
        }
        record.setParameters(null);
        record.setMessage(this.getName() + " " + msg);
        this.getParent().log(record);
        record.setMessage(msg);
        super.log(record);
    }

    public void exception(String msg, Throwable t)
    {
        this.log(Level.SEVERE, msg, t);
    }
}
