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

import java.sql.Timestamp;
import java.util.Arrays;

import org.jooq.DSLContext;
import org.jooq.Query;

import static de.cubeisland.engine.log.storage.TableLogEntry.TABLE_LOG_ENTRY;

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

    public Query createInsert(DSLContext dsl)
    {
        return dsl.insertInto(TABLE_LOG_ENTRY, TABLE_LOG_ENTRY.DATE, TABLE_LOG_ENTRY.ACTION,
                              TABLE_LOG_ENTRY.WORLD, TABLE_LOG_ENTRY.X, TABLE_LOG_ENTRY.Y, TABLE_LOG_ENTRY.Z,
                              TABLE_LOG_ENTRY.CAUSER,
                              TABLE_LOG_ENTRY.BLOCK, TABLE_LOG_ENTRY.DATA,
                              TABLE_LOG_ENTRY.NEWBLOCK, TABLE_LOG_ENTRY.NEWDATA, TABLE_LOG_ENTRY.ADDITIONALDATA)
            .values(Arrays.asList(logdata));
    }
}
