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

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableMail extends AutoIncrementTable<Mail, UInteger>
{
    public static TableMail TABLE_MAIL;

    public TableMail(String prefix)
    {
        super(prefix + "mail", new Version(1));
        this.setAIKey(KEY);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), USERID);
        this.addForeignKey(TABLE_USER.getPrimaryKey(),  SENDERID);
        this.addFields(KEY, MESSAGE, USERID, SENDERID);
        TABLE_MAIL = this;
    }

    public final TableField<Mail, UInteger> KEY = createField("key", U_INTEGER.nullable(false), this);
    public final TableField<Mail, String> MESSAGE = createField("message", SQLDataType.VARCHAR.length(100).nullable(false), this);
    public final TableField<Mail, UInteger> USERID = createField("userId", U_INTEGER.nullable(false), this);
    public final TableField<Mail, UInteger> SENDERID = createField("senderId", U_INTEGER, this);

    @Override
    public Class<Mail> getRecordType() {
        return Mail.class;
    }
}
