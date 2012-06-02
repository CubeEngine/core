package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.persistence.database.Database;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class DatabaseLogWriter implements LogWriter
{
    private Database db;
    private String trigger;
    
    public DatabaseLogWriter(Database db, String trigger)
    {
        this.db = db;
        this.trigger = trigger;
        try
        {
            this.db.query("CREATE TABLE IF NOT EXISTS {{log}}");
        }
        catch(Exception ex)
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
        catch(Exception ex)
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
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }
}
