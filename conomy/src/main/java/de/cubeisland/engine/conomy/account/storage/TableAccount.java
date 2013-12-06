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
        this.addFields(KEY, USER_ID, NAME, VALUE, MASK);
        TABLE_ACCOUNT = this;
    }

    public final TableField<AccountModel, UInteger> KEY = createField("key", U_INTEGER.nullable(false), this);
    public final TableField<AccountModel, UInteger> USER_ID = createField("user_id", U_INTEGER, this);
    public final TableField<AccountModel, String> NAME = createField("name", SQLDataType.VARCHAR.length(64), this);
    public final TableField<AccountModel, Long> VALUE = createField("value", SQLDataType.BIGINT.nullable(false), this);
    public final TableField<AccountModel, Byte> MASK = createField("mask", SQLDataType.TINYINT, this);

    @Override
    public Class<AccountModel> getRecordType() {
        return AccountModel.class;
    }
}
