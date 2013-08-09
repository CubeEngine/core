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
import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.TableCreator;
import de.cubeisland.engine.core.storage.database.mysql.Keys;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import de.cubeisland.engine.core.util.Version;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

public class TableUser extends TableImpl<UserEntity> implements TableCreator<UserEntity>
{

    public static TableUser TABLE_USER;

    private TableUser(String prefix)
    {
        super(prefix + "user");
        IDENTITY = Keys.identity(this, this.KEY);
        PRIMARY_KEY = Keys.uniqueKey(this, this.KEY);
        UNIQUE_PLAYER = Keys.uniqueKey(this, this.PLAYER);
    }

    public static TableUser initTable(Database database)
    {
        if (TABLE_USER == null)
        {
            MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
            TABLE_USER = new TableUser(config.tablePrefix);
            return TABLE_USER;
        }
        throw new IllegalStateException();
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

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<UserEntity, UInteger> IDENTITY;
    public final UniqueKey<UserEntity> PRIMARY_KEY;
    public final UniqueKey<UserEntity> UNIQUE_PLAYER;

    public final TableField<UserEntity, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<UserEntity, String> PLAYER = createField("player", SQLDataType.VARCHAR.length(16), this);
    public final TableField<UserEntity, Byte> NOGC = createField("nogc", SQLDataType.TINYINT, this);
    public final TableField<UserEntity, Timestamp> LASTSEEN = createField("lastseen", SQLDataType.TIMESTAMP, this);
    public final TableField<UserEntity, byte[]> PASSWD = createField("passwd", SQLDataType.VARBINARY.length(128), this);
    public final TableField<UserEntity, Timestamp> FIRSTSEEN = createField("firstseen", SQLDataType.TIMESTAMP, this);
    public final TableField<UserEntity, String> LANGUAGE = createField("language", SQLDataType.VARCHAR.length(5), this);

    @Override
    public Identity<UserEntity, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<UserEntity> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<UserEntity>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY, UNIQUE_PLAYER);
    }

    @Override
    public Class<UserEntity> getRecordType()
    {
        return UserEntity.class;
    }
}
