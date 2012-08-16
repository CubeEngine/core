package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.mysql.MySQLDatabase;
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
    private final MySQLDatabase db;
    private final String table;

    public DatabaseHandler(Level level, MySQLDatabase db, String table)
    {
        this.setLevel(level);
        this.db = db;
        this.table = table;
        try
        {
            this.db.execute(this.db.buildQuery()
                .createTable(this.db.prefix(table, false), true)
                .startFields()
                    .field("id", AttrType.INT, 11, true, true, true)
                    .field("timestamp", AttrType.TIMESTAMP, true)
                    .field("level", AttrType.VARCHAR,20,true)
                    .field("logger", AttrType.VARCHAR, 50, true)
                    .field("message", AttrType.TEXT, true)
                    .primaryKey("id")
                .endFields()
                .engine("InnoDB").defaultcharset("utf8").autoIncrement(1)
                .endCreateTable()
            .end());
            
            this.db.prepareAndStoreStatement(this.getClass(), "insert", this.db.buildQuery()
                .insert().into(this.db.prefix(table, false))
                .cols("timestamp","level","logger","message")
                .values(4)
                .end()
            .end());
            this.db.prepareAndStoreStatement(this.getClass(), "clear", "TRUNCATE {{" + table + "}}");
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
            this.db.preparedExecute(this.getClass(), "clear");
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
            db.preparedExecute(this.getClass(), "insert", time, level, logger, msg);
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