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
package de.cubeisland.engine.conomy.account.storage;

import java.lang.Byte;
import java.lang.Long;
import java.lang.String;
import java.sql.Connection;
import java.sql.SQLException;
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

public class TableAccount extends TableImpl<AccountModel> implements TableCreator<AccountModel>
{
    public static TableAccount TABLE_ACCOUNT;

    private TableAccount(String prefix)
    {
        super(prefix + "accounts");
        IDENTITY = Keys.identity(this, this.KEY);
        PRIMARY_KEY = Keys.uniqueKey(this, this.KEY);
        UNIQUE_USERID_NAME = Keys.uniqueKey(this, this.USER_ID, this.NAME);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.PRIMARY_KEY, this, this.USER_ID);
    }

    public static TableAccount initTable(Database database)
    {
        if (TABLE_ACCOUNT != null) throw new IllegalStateException();
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_ACCOUNT = new TableAccount(config.tablePrefix);
        return TABLE_ACCOUNT;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`key` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`user_id` int(10) unsigned DEFAULT NULL,\n" +
                                        "`name` varchar(64) DEFAULT NULL,\n" +
                                        "`value` bigint(20) NOT NULL,\n" +
                                        "`mask` tinyint(4) DEFAULT NULL,\n" +
                                        "PRIMARY KEY (`key`),\n" +
                                        "UNIQUE KEY `user_id` (`user_id`,`name`),\n" +
                                        "FOREIGN KEY `f_user`(`user_id`) REFERENCES " + TABLE_USER.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<AccountModel, UInteger> IDENTITY;
    public final UniqueKey<AccountModel> PRIMARY_KEY;
    public final UniqueKey<AccountModel> UNIQUE_USERID_NAME;
    public final ForeignKey<AccountModel, UserEntity> FOREIGN_USER;

    public final TableField<AccountModel, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<AccountModel, UInteger> USER_ID = createField("user_id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<AccountModel, String> NAME = createField("name", SQLDataType.VARCHAR.length(64), this);
    public final TableField<AccountModel, Long> VALUE = createField("value", SQLDataType.BIGINT, this);
    public final TableField<AccountModel, Byte> MASK = createField("mask", SQLDataType.TINYINT, this);

    @Override
    public Identity<AccountModel, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<AccountModel> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<AccountModel>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY);
    }

    @Override
    public List<ForeignKey<AccountModel, ?>> getReferences() {
        return Arrays.<ForeignKey<AccountModel, ?>>asList(FOREIGN_USER);
    }

    @Override
    public Class<AccountModel> getRecordType() {
        return AccountModel.class;
    }
}
