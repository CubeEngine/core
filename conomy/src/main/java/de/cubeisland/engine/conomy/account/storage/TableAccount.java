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

import java.sql.Connection;
import java.sql.SQLException;

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableAccount extends AutoIncrementTable<AccountModel, UInteger>
{
    public static TableAccount TABLE_ACCOUNT;

    public TableAccount(String prefix)
    {
        super(prefix + "accounts", new Version(1));
        this.setAIKey(KEY);
        this.addUniqueKey(USER_ID, NAME);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), USER_ID);
        TABLE_ACCOUNT = this;
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

    public final TableField<AccountModel, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<AccountModel, UInteger> USER_ID = createField("user_id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<AccountModel, String> NAME = createField("name", SQLDataType.VARCHAR.length(64), this);
    public final TableField<AccountModel, Long> VALUE = createField("value", SQLDataType.BIGINT, this);
    public final TableField<AccountModel, Byte> MASK = createField("mask", SQLDataType.TINYINT, this);

    @Override
    public Class<AccountModel> getRecordType() {
        return AccountModel.class;
    }
}
