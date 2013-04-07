package de.cubeisland.cubeengine.log.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class QueuedLog
{
    private Object[] logdata;

    private QueuedLog(Object... logdata)
    {
        this.logdata = logdata;
    }

    public QueuedLog(Timestamp timestamp, Long worldID, Integer x, Integer y, Integer z, long action, Long causer, String block, Long data, String newBlock, Byte newData, String additionalData)
    {
        this(timestamp, action, worldID, x, y, z, causer, block, data, newBlock, newData, additionalData);
    }

    public void addDataToBatch(PreparedStatement stmt) throws SQLException
    {
        for (int i = 0; i < this.logdata.length; ++i)
        {
            stmt.setObject(i + 1, this.logdata[i]);
        }
        stmt.addBatch();
    }
}
