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
        this.addFields(ID, USERID, ACCOUNTID, ACCESSLEVEL);
        TABLE_BANK_ACCESS = this;
    }

    public final TableField<BankAccessModel, UInteger> ID = createField("id", U_INTEGER.nullable(false), this);
    public final TableField<BankAccessModel, UInteger> USERID = createField("userId", U_INTEGER.nullable(false), this);
    public final TableField<BankAccessModel, UInteger> ACCOUNTID = createField("accountId", U_INTEGER.nullable(false), this);
    public final TableField<BankAccessModel, Byte> ACCESSLEVEL = createField("accessLevel", SQLDataType.TINYINT.nullable(false), this);

    @Override
    public Class<BankAccessModel> getRecordType() {
        return BankAccessModel.class;
    }
}
