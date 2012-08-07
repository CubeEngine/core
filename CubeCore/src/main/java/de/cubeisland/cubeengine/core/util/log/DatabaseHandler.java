package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.persistence.database.Database;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Robin Bechtel-Ostmann
 */
public class DatabaseHandler extends Handler
{
    private final Database db;
    private final String TABLE;

    public DatabaseHandler(Level level, Database db, String table)
    {
        this.setLevel(level);
        this.db = db;
        this.TABLE = table;
        try
        {
            this.db.exec("CREATE TABLE IF NOT EXISTS {{" + TABLE + "}} ("
                + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                + "`timestamp` timestamp NOT NULL,"
                + "`level` varchar(20) NOT NULL,"
                + "`logger` varchar(50) NOT NULL,"
                + "`message` text NOT NULL,"
                + "PRIMARY KEY (`id`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
            this.db.prepareStatement("insertLog", "INSERT INTO {{" + TABLE + "}} (timestamp, level, logger, message) VALUES (?,?,?,?)");
            this.db.prepareStatement("clearLog", "TRUNCATE {{" + TABLE + "}}");
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }

    public void clearLog()
    {
        try
        {
            this.db.preparedExec("clearLog");
        }
        catch (SQLException ex)
        {
        }
    }

    @Override
    public void publish(LogRecord record)
    {
        if (!isLoggable(record))
        {
            return;
        }
        final Timestamp time = new Timestamp(record.getMillis());
        final String level = record.getLevel().getLocalizedName();
        final String msg = record.getMessage();
        final String logger = record.getLoggerName();
        try
        {
            db.preparedExec("insertLog", time, level, logger, msg);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void flush()
    {
        //nicht nötig da direkt alles geschrieben wird
    }

    @Override
    public void close() throws SecurityException
    {
        //hier nicht nötig DB wird woanders geclosed
    }
}