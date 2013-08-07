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
package de.cubeisland.engine.basics.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.TableCreator;
import de.cubeisland.engine.core.storage.database.mysql.Keys;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Version;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableBasicsUser extends TableImpl<BasicsUserEntity> implements TableCreator<BasicsUserEntity>
{
    public static TableBasicsUser TABLE_BASIC_USER;

    private TableBasicsUser(String prefix)
    {
        super(prefix + "basicuser");
        IDENTITY = Keys.identity(this, this.KEY);
        PRIMARY_KEY = Keys.uniqueKey(this, this.KEY);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.PRIMARY_KEY, this, this.KEY);
    }

    public static TableBasicsUser initTable(Database database)
    {
        if (TABLE_BASIC_USER == null)
        {
            MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
            TABLE_BASIC_USER = new TableBasicsUser(config.tablePrefix);
            return TABLE_BASIC_USER;
        }
        throw new IllegalStateException();
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`key` int(10) unsigned NOT NULL,\n" +
                                        "`muted` timestamp NULL DEFAULT NULL,\n" +
                                        "`godMode` tinyint(1) NOT NULL,\n" +
                                        "PRIMARY KEY (`key`))\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
        connection.prepareStatement("ALTER TABLE " + this.getName() +
                                        "\nADD FOREIGN KEY f_user (`key`) REFERENCES `cube_user` (`key`) ON UPDATE CASCADE ON DELETE CASCADE;");
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<BasicsUserEntity, UInteger> IDENTITY;
    public final UniqueKey<BasicsUserEntity> PRIMARY_KEY;
    public final ForeignKey<BasicsUserEntity, UserEntity> FOREIGN_USER;

    public final TableField<BasicsUserEntity, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<BasicsUserEntity, Timestamp> MUTED = createField("muted", SQLDataType.TIMESTAMP, this);
    public final TableField<BasicsUserEntity, Byte> GODMODE = createField("godMode", SQLDataType.TINYINT, this);

    @Override
    public Identity<BasicsUserEntity, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<BasicsUserEntity> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<BasicsUserEntity>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY);
    }

    @Override
    public List<ForeignKey<BasicsUserEntity, ?>> getReferences() {
        return Arrays.<ForeignKey<BasicsUserEntity, ?>>asList(FOREIGN_USER);
    }

    @Override
    public Class<BasicsUserEntity> getRecordType() {
        return BasicsUserEntity.class;
    }
}
