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
package de.cubeisland.engine.basics.command.moderation.kit;

import java.sql.Connection;
import java.sql.SQLException;

import de.cubeisland.engine.core.storage.database.Table;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableKitsGiven extends Table<KitsGiven>
{
    public static TableKitsGiven TABLE_KITS;

    public TableKitsGiven(String prefix)
    {
        super(prefix + "kits", new Version(1));
        this.setPrimaryKey(USERID);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), USERID);
        TABLE_KITS = this;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`userId` int(10) unsigned NOT NULL,\n" +
                                        "`kitName` varchar(50) NOT NULL,\n" +
                                        "`amount` int(11) NOT NULL,\n" +
                                        "PRIMARY KEY (`userId`,`kitName`),\n" +
                                        "FOREIGN KEY `f_user`(`userId`) REFERENCES " + TABLE_USER.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    public final TableField<KitsGiven, UInteger> USERID = createField("userId", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<KitsGiven, String> KITNAME = createField("kitName", SQLDataType.VARCHAR.length(50), this);
    public final TableField<KitsGiven, Integer> AMOUNT = createField("amount", SQLDataType.INTEGER, this);

    @Override
    public Class<KitsGiven> getRecordType() {
        return KitsGiven.class;
    }
}
