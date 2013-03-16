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

    public QueuedLog(Timestamp timestamp, long worldID, int x, int y, int z, int action, long causer, String block, Short data, String newBlock, Short newData, String additionalData)
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
