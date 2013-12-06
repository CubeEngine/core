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
package de.cubeisland.engine.core.user;


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

public class TableUser extends AutoIncrementTable<UserEntity, UInteger>
{
    public static TableUser TABLE_USER;

    public TableUser(String prefix)
    {
        super(prefix + "user", new Version(1));
        this.setPrimaryKey(this.KEY);
        this.addUniqueKey(this.PLAYER);
        TABLE_USER = this;
    }

    public static TableUser initTable(Database database)
    {
        if (TABLE_USER == null)
        {
            MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
            TABLE_USER = new TableUser(config.tablePrefix);
        }
        return TABLE_USER;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                    "`key` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                    "`player` varchar(16) NOT NULL,\n" +
                                    "`nogc` tinyint(1) NOT NULL,\n" +
                                    "`lastseen` datetime NOT NULL,\n" +
                                    "`passwd` varbinary(128) DEFAULT NULL,\n" +
                                    "`firstseen` datetime NOT NULL,\n" +
                                    "`language` varchar(5) DEFAULT NULL,\n" +
                                    "PRIMARY KEY (`key`),\n" +
                                    "UNIQUE KEY `player` (`player`))\n" +
                                    "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                    "COMMENT='1.0.0'").execute();
    }

    public final TableField<UserEntity, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<UserEntity, String> PLAYER = createField("player", SQLDataType.VARCHAR.length(16), this);
    public final TableField<UserEntity, Byte> NOGC = createField("nogc", SQLDataType.TINYINT, this);
    public final TableField<UserEntity, Timestamp> LASTSEEN = createField("lastseen", SQLDataType.TIMESTAMP, this);
    public final TableField<UserEntity, byte[]> PASSWD = createField("passwd", SQLDataType.VARBINARY.length(128), this);
    public final TableField<UserEntity, Timestamp> FIRSTSEEN = createField("firstseen", SQLDataType.TIMESTAMP, this);
    public final TableField<UserEntity, String> LANGUAGE = createField("language", SQLDataType.VARCHAR.length(5), this);

    @Override
    public Class<UserEntity> getRecordType()
    {
        return UserEntity.class;
    }
}
