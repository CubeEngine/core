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

import static de.cubeisland.engine.conomy.account.storage.TableAccount.TABLE_ACCOUNT;
import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableBankAccess extends TableImpl<BankAccessModel> implements TableCreator<BankAccessModel>
{
    public static TableBankAccess TABLE_BANK_ACCESS;

    private TableBankAccess(String prefix)
    {
        super(prefix + "account_access");
        IDENTITY = Keys.identity(this, this.ID);
        PRIMARY_KEY = Keys.uniqueKey(this, this.ID);
        UNIQUE_USERID_ACCOUNTID = Keys.uniqueKey(this, this.USERID, this.ACCOUNTID);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.getPrimaryKey(), this, this.USERID);
        FOREIGN_ACCOUNT = Keys.foreignKey(TABLE_ACCOUNT.PRIMARY_KEY, this, this.ACCOUNTID);
    }

    public static TableBankAccess initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_BANK_ACCESS = new TableBankAccess(config.tablePrefix);
        return TABLE_BANK_ACCESS;
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

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<BankAccessModel, UInteger> IDENTITY;
    public final UniqueKey<BankAccessModel> PRIMARY_KEY;
    public final UniqueKey<BankAccessModel> UNIQUE_USERID_ACCOUNTID;
    public final ForeignKey<BankAccessModel, UserEntity> FOREIGN_USER;
    public final ForeignKey<BankAccessModel, AccountModel> FOREIGN_ACCOUNT;

    public final TableField<BankAccessModel, UInteger> ID = createField("id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<BankAccessModel, UInteger> USERID = createField("userId", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<BankAccessModel, UInteger> ACCOUNTID = createField("accountId", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<BankAccessModel, Byte> ACCESSLEVEL = createField("accessLevel", SQLDataType.TINYINT, this);

    @Override
    public Identity<BankAccessModel, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<BankAccessModel> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<BankAccessModel>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY, UNIQUE_USERID_ACCOUNTID);
    }

    @Override
    public List<ForeignKey<BankAccessModel, ?>> getReferences() {
        return Arrays.<ForeignKey<BankAccessModel, ?>>asList(FOREIGN_USER, FOREIGN_ACCOUNT);
    }

    @Override
    public Class<BankAccessModel> getRecordType() {
        return BankAccessModel.class;
    }
}
