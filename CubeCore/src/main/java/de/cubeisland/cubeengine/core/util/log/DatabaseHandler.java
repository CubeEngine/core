package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.persistence.database.Database;
import java.util.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Robin Bechtel-Ostmann
 */
public class DatabaseHandler extends Handler
{
    private Database db;
    private String trigger;

    public DatabaseHandler(Database db, String trigger)
    {
        Formatter dbFormatter = new Formatter();

        this.db = db;
        this.trigger = trigger;
        try
        {
            this.db.query("CREATE TABLE IF NOT EXISTS {{log}}");
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }

    public void clearLog()
    {
        try
        {
            this.db.query("DROPTABLE {{log}}");
            this.db.query("CREATE TABLE {{log}}");
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }

    public void write(String text)
    {
        try
        {
            this.db.query("INSERT INTO {{log}} (date, trigger, message) VALUES(NOW(), " + this.trigger + ", " + text + ")");
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void publish(LogRecord record)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flush()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws SecurityException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
