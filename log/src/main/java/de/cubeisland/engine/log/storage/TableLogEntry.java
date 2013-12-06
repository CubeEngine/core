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

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;
import static de.cubeisland.engine.log.storage.TableActionTypes.TABLE_ACTION_TYPE;

public class TableLogEntry extends AutoIncrementTable<LogEntry, UInteger>
{
    private static TableLogEntry TEMP_TABLE_LOG_ENTRY;
    public static TableLogEntry TABLE_LOG_ENTRY;

    private TableLogEntry(String prefix, boolean temp)
    {
        super(prefix + (temp ? "TEMP_log_entries" : "log_entries"), new Version(1));
        this.setAIKey(ID);
        this.addIndex(DATE);
        this.addIndex(CAUSER);
        this.addIndex(BLOCK);
        this.addIndex(NEWBLOCK);
        this.addIndex(X, Y, Z, WORLD);
        this.addIndex(X, Y, Z, WORLD, DATE);
        this.addForeignKey(TABLE_WORLD.getPrimaryKey(), WORLD);
        this.addForeignKey(TABLE_ACTION_TYPE.getPrimaryKey(), ACTION);
        this.addFields(ID, DATE, WORLD, X, Y, Z, ACTION, CAUSER, BLOCK, DATA, NEWBLOCK, NEWDATA, ADDITIONALDATA);
    }

    public static TableLogEntry initTable(Database database)
    {
        TABLE_LOG_ENTRY = new TableLogEntry(database.getTablePrefix(), false);
        return TABLE_LOG_ENTRY;
    }

    protected static TableLogEntry initTempTable(Database database)
    {
        if (TEMP_TABLE_LOG_ENTRY == null)
        {
            TEMP_TABLE_LOG_ENTRY = new TableLogEntry(database.getTablePrefix(), true);
        }
        database.registerTable(TEMP_TABLE_LOG_ENTRY); // recreate Table
        return TEMP_TABLE_LOG_ENTRY;
    }

    public final TableField<LogEntry, UInteger> ID = createField("id", U_INTEGER.nullable(false), this);
    public final TableField<LogEntry, Timestamp> DATE = createField("date", SQLDataType.TIMESTAMP.nullable(false), this);
    public final TableField<LogEntry, UInteger> WORLD = createField("world", U_INTEGER, this);
    public final TableField<LogEntry, Integer> X = createField("x", SQLDataType.INTEGER, this);
    public final TableField<LogEntry, Integer> Y = createField("y", SQLDataType.INTEGER, this);
    public final TableField<LogEntry, Integer> Z = createField("z", SQLDataType.INTEGER, this);
    public final TableField<LogEntry, UInteger> ACTION = createField("action", U_INTEGER.nullable(false), this);
    public final TableField<LogEntry, Long> CAUSER = createField("causer", SQLDataType.BIGINT, this);
    public final TableField<LogEntry, String> BLOCK = createField("block", SQLDataType.VARCHAR.length(255), this);
    public final TableField<LogEntry, Long> DATA = createField("data", SQLDataType.BIGINT, this);
    public final TableField<LogEntry, String> NEWBLOCK = createField("newBlock", SQLDataType.VARCHAR.length(255), this);
    public final TableField<LogEntry, Byte> NEWDATA = createField("newData", SQLDataType.TINYINT, this);
    public final TableField<LogEntry, String> ADDITIONALDATA = createField("additionalData", SQLDataType.VARCHAR.length(255), this);

    @Override
    public Class<LogEntry> getRecordType() {
        return LogEntry.class;
    }
}
