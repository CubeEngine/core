package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
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
    private final Database database;

    public DatabaseHandler(Level level, Database database, String table)
    {
        this.setLevel(level);
        this.database = database;
        try
        {
            QueryBuilder queryBuilder = this.database.getQueryBuilder();
            this.database.execute(queryBuilder
                .createTable(table, true)
                .beginFields()
                    .field("id", AttrType.INT, 11, true, true, true)
                    .field("timestamp", AttrType.TIMESTAMP, true)
                    .field("level", AttrType.VARCHAR,20,true)
                    .field("logger", AttrType.VARCHAR, 50, true)
                    .field("message", AttrType.TEXT, true)
                    .primaryKey("id")
                .endFields()
                .engine("InnoDB").defaultcharset("utf8").autoIncrement(1)
                .end()
            .end());

            this.database.prepareAndStoreStatement(this.getClass(), "insert", queryBuilder
                .insert().into(table)
                .cols("timestamp", "level", "logger", "message")
                .end()
            .end());

            this.database.prepareAndStoreStatement(this.getClass(), "clear", queryBuilder
                .clearTable(table)
            .end());
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
            this.database.preparedExecute(this.getClass(), "clear");
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
        try
        {
            database.preparedExecute(this.getClass(), "insert",
                new Timestamp(record.getMillis()),
                record.getLevel().getLocalizedName(),
                record.getLoggerName(),
                record.getMessage()
            );
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }

    /**
     * We're writing directly
     */
    @Override
    public void flush()
    {}

    /**
     * Not needed as we use a shared database connection
     */
    @Override
    public void close() throws SecurityException
    {}
}