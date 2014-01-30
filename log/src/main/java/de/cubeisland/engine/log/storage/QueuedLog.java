/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class QueuedLog
{
    private final Object[] logdata;

    private QueuedLog(Object... logdata)
    {
        this.logdata = logdata;
    }

    public QueuedLog(Timestamp timestamp, Long worldID, Integer x, Integer y, Integer z, long action, Long causer, String block, Long data, String newBlock, Byte newData, String additionalData)
    {
        this(timestamp, action, worldID, x, y, z, causer, block, data, newBlock, newData, additionalData);
    }

    public void bindTo(PreparedStatement stmt) throws SQLException
    {
        for (int i = 0; i < logdata.length; ++i)
        {
            stmt.setObject(i + 1, logdata[i]);
        }
        stmt.addBatch();
    }
}
