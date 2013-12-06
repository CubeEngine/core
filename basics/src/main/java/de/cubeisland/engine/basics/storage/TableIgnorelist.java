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

import de.cubeisland.engine.core.storage.database.Table;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableIgnorelist extends Table<IgnoreList>
{
    public static TableIgnorelist TABLE_IGNORE_LIST;

    public TableIgnorelist(String prefix)
    {
        super(prefix + "ignorelist", new Version(1));
        this.addForeignKey(TABLE_USER.getPrimaryKey(), KEY);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), IGNORE);
        this.setPrimaryKey(KEY, IGNORE);
        TABLE_IGNORE_LIST = this;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (" +
                                        "`key` int(10) unsigned NOT NULL," +
                                        "`ignore` int(10) unsigned NOT NULL," +
                                        "PRIMARY KEY (`key`,`ignore`)," +
                                        "FOREIGN KEY f_user (`key`) REFERENCES `" + TABLE_USER.getName() + "` (`" + TABLE_USER.KEY.getName() + "`) ON UPDATE CASCADE ON DELETE CASCADE," +
                                        "FOREIGN KEY f_ignore (`ignore`) REFERENCES `" +TABLE_USER.getName() +"` (`" + TABLE_USER.KEY.getName() + "`) ON UPDATE CASCADE ON DELETE CASCADE) " +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci " +
                                        "COMMENT='1.0.0'").execute();
    }

    public final TableField<IgnoreList, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<IgnoreList, UInteger> IGNORE = createField("ignore", SQLDataType.INTEGERUNSIGNED, this);

    @Override
    public Class<IgnoreList> getRecordType() {
        return IgnoreList.class;
    }
}
