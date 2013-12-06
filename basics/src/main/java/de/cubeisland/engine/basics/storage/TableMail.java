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
        TABLE_MAIL = this;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`key` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`message` varchar(100) NOT NULL,\n" +
                                        "`userId` int(10) unsigned NOT NULL,\n" +
                                        "`senderId` int(10) unsigned DEFAULT NULL,\n" +
                                        "PRIMARY KEY (`key`),\n" +
                                        "FOREIGN KEY f_user (`userId`) REFERENCES " + TABLE_USER.getName() + " (`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY f_sender (`senderId`) REFERENCES " + TABLE_USER.getName() + " (`key`) ON UPDATE CASCADE ON DELETE CASCADE)" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    public final TableField<Mail, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<Mail, String> MESSAGE = createField("message", SQLDataType.VARCHAR.length(100), this);
    public final TableField<Mail, UInteger> USERID = createField("userId", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<Mail, UInteger> SENDERID = createField("senderId", SQLDataType.INTEGERUNSIGNED, this);

    @Override
    public Class<Mail> getRecordType() {
        return Mail.class;
    }
}
