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

import static de.cubeisland.engine.conomy.account.storage.TableAccount.TABLE_ACCOUNT;
import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableBankAccess extends AutoIncrementTable<BankAccessModel, UInteger>
{
    public static TableBankAccess TABLE_BANK_ACCESS;

    public TableBankAccess(String prefix)
    {
        super(prefix + "account_access", new Version(1));
        this.setAIKey(ID);
        this.addUniqueKey(USERID, ACCOUNTID);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), USERID);
        this.addForeignKey(TABLE_ACCOUNT.getPrimaryKey(), ACCOUNTID);
        TABLE_BANK_ACCESS = this;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`userId` int(10) unsigned NOT NULL,\n" +
                                        "`accountId` int(10) unsigned NOT NULL,\n" +
                                        "`accessLevel` tinyint(4) NOT NULL,\n" +
                                        "PRIMARY KEY (`id`),\n" +
                                        "UNIQUE KEY `userId` (`userId`,`accountId`),\n" +
                                        "FOREIGN KEY `f_user`(`userId`) REFERENCES " + TABLE_USER.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY `f_account`(`accountId`) REFERENCES " + TABLE_ACCOUNT.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    public final TableField<BankAccessModel, UInteger> ID = createField("id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<BankAccessModel, UInteger> USERID = createField("userId", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<BankAccessModel, UInteger> ACCOUNTID = createField("accountId", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<BankAccessModel, Byte> ACCESSLEVEL = createField("accessLevel", SQLDataType.TINYINT, this);

    @Override
    public Class<BankAccessModel> getRecordType() {
        return BankAccessModel.class;
    }
}
