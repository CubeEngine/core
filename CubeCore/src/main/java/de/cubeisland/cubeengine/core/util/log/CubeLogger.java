package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.module.ModuleBase;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Faithcaio
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
        super(name, null);
    }

    /**
     * Creates a new Logger for this Module
     *
     * @param plugin the CubeEngine Module
     */
    public CubeLogger(ModuleBase plugin)
    {
        this(plugin.getName());
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
        catch (Exception ex)
        {
            this.log(Level.SEVERE, "Could not add DatabaseHandler to Logger");
            return this;
        }
    }

    /**
     * Adds a ConsoleHandler to the specified Logger
     *
     * @param level the minimum loglvl needed to log
     */
    public CubeLogger addConsoleHandler(Level level)
    {
        try
        {
            this.setUseParentHandlers(false);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(level);
            consoleHandler.setFormatter(new ConsoleFormatter());
            this.addHandler(consoleHandler);
            return this;
        }
        catch (Exception ex)
        {
            this.log(Level.SEVERE, "Could not add ConsoleHandler to Logger");
            return this;
        }
    }

    /**
     * Adds a FileHandler to this Logger
     *
     * @param filename the name of the file to log into
     * @param level the minimum loglvl needed to log
     */
    public CubeLogger addFileHandler(String filename, Level level)
    {
        try
        {
            FileHandler fileHandler = new FileHandler(filename, true);
            fileHandler.setLevel(level);
            fileHandler.setFormatter(new FileFormatter());
            this.addHandler(fileHandler);
            return this;

        }
        catch (Exception ex)
        {
            this.log(Level.SEVERE, "Could not add FileHandler to Logger");
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
        catch (Exception ex)
        {
            this.log(Level.SEVERE, "Could not add RemoteHandler to Logger");
            return this;
        }
    }

    @Override
    public void log(Level level, String msg)
    {
        log(level, msg, this.getName());
    }
}
