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
package de.cubeisland.engine.log.storage;

import java.sql.Connection;
import java.sql.SQLException;

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

public class TableActionTypes extends AutoIncrementTable<ActionTypeModel, UInteger>
{
    public static TableActionTypes TABLE_ACTION_TYPE;

    private TableActionTypes(String prefix)
    {
        super(prefix + "log_actiontypes", new Version(1));
        this.setAIKey(ID);
        this.addUniqueKey(NAME);
        TABLE_ACTION_TYPE = this;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`name` varchar(32) UNIQUE NOT NULL,\n" +
                                        "PRIMARY KEY (`id`))\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    public final TableField<ActionTypeModel, UInteger> ID = createField("id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<ActionTypeModel, String> NAME = createField("name", SQLDataType.VARCHAR.length(32), this);

    @Override
    public Class<ActionTypeModel> getRecordType() {
        return ActionTypeModel.class;
    }
}
