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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
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
        this.addForeignKey(TABLE_WORLD.getPrimaryKey(), WORLD);
        this.addForeignKey(TABLE_ACTION_TYPE.getPrimaryKey(), ACTION);
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

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`date` datetime NOT NULL,\n" +
                                        "`world` int(10) unsigned DEFAULT NULL,\n" +
                                        "`x` int(11) DEFAULT NULL,\n" +
                                        "`y` int(11) DEFAULT NULL,\n" +
                                        "`z` int(11) DEFAULT NULL,\n" +
                                        "`action` int(10) unsigned NOT NULL,\n" +
                                        "`causer` bigint(20) DEFAULT NULL,\n" +
                                        "`block` varchar(255) DEFAULT NULL,\n" +
                                        "`data` bigint(20) DEFAULT NULL,\n" +
                                        "`newBlock` varchar(255) DEFAULT NULL,\n" +
                                        "`newData` tinyint(4) DEFAULT NULL,\n" +
                                        "`additionalData` varchar(255) DEFAULT NULL,\n" +
                                        "PRIMARY KEY (`id`),\n" +
                                        "FOREIGN KEY `world` (`world`) REFERENCES " + TABLE_WORLD.getName() + " (`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY `action` (`action`) REFERENCES " + TABLE_ACTION_TYPE.getName() + " (`id`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "KEY `loc_date` (`x`,`y`,`z`,`world`,`date`),\n" +
                                        "KEY `date` (`date`),\n" +
                                        "KEY `loc` (`x`,`y`,`z`,`world`),\n" +
                                        "KEY `causer` (`causer`),\n" +
                                        "KEY `block` (`block`),\n" +
                                        "KEY `newBlock` (`newBlock`))\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    public final TableField<LogEntry, UInteger> ID = createField("id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<LogEntry, Timestamp> DATE = createField("date", SQLDataType.TIMESTAMP, this);
    public final TableField<LogEntry, UInteger> WORLD = createField("world", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<LogEntry, Integer> X = createField("x", SQLDataType.INTEGER, this);
    public final TableField<LogEntry, Integer> Y = createField("y", SQLDataType.INTEGER, this);
    public final TableField<LogEntry, Integer> Z = createField("z", SQLDataType.INTEGER, this);
    public final TableField<LogEntry, UInteger> ACTION = createField("action", SQLDataType.INTEGERUNSIGNED, this);
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
