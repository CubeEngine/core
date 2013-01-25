package de.cubeisland.cubeengine.log.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author Anselm Brehme
 */
public abstract class QueuedLog
{
    private long insertId = -1;
    private Object[] mainData;

    public void setInsertId(long insertId)
    {
        this.insertId = insertId;
    }

    public long getInsertId()
    {
        return this.insertId;
    }

    public abstract void run();

    void run(long key)
    {
        this.insertId = key;
        this.run();
    }

    public void addMainLogData(Object... maindata)
    {
        this.mainData = maindata;
    }

    public void addMainDataToBatch(PreparedStatement stmt) throws SQLException
    {
        for (int i = 0; i < this.mainData.length; ++i)
        {
            stmt.setObject(i + 1, this.mainData[i]);
        }
        stmt.addBatch();
    }
}
