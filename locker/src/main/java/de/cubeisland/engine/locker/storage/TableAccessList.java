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
package de.cubeisland.engine.locker.storage;

import java.sql.Connection;
import java.sql.SQLException;

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.locker.storage.TableLocks.TABLE_LOCK;

public class TableAccessList extends AutoIncrementTable<AccessListModel, UInteger>
{
    public static TableAccessList TABLE_ACCESS_LIST;

    public TableAccessList(String prefix)
    {
        super(prefix + "lockaccesslist", new Version(1));
        this.setAIKey(ID);
        this.addUniqueKey(USER_ID, LOCK_ID);
        this.addUniqueKey(USER_ID, OWNER_ID);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), USER_ID);
        this.addForeignKey(TABLE_LOCK.getPrimaryKey(), LOCK_ID);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), OWNER_ID);
        TABLE_ACCESS_LIST = this;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`user_id` int(10) unsigned NOT NULL,\n" +
                                        "`lock_id` int(10) unsigned DEFAULT NULL,\n" +
                                        "`level` smallint NOT NULL,\n" +
                                        "`owner_id` int(10) unsigned DEFAULT NULL,\n" +
                                        "PRIMARY KEY (`id`),\n" +
                                        "UNIQUE KEY (`user_id`, `lock_id`),\n" +
                                        "UNIQUE KEY (`user_id`, `owner_id`),\n" +
                                        "FOREIGN KEY f_user (`user_id`) REFERENCES " + TABLE_USER.getName() + " (`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY f_guard (`lock_id`) REFERENCES " + TABLE_LOCK.getName() + " (`id`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY f_global_user (`owner_id`) REFERENCES " + TABLE_USER.getName() + " (`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    public final TableField<AccessListModel, UInteger> ID = createField("id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<AccessListModel, UInteger> USER_ID = createField("user_id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<AccessListModel, UInteger> LOCK_ID = createField("lock_id", SQLDataType.INTEGERUNSIGNED, this);
    // BitMask granting the user access to a protection (this is NOT restricting) (if ACCESS_PUT is not set on a donation chest it does not matter)
    public final TableField<AccessListModel, Short> LEVEL = createField("level", SQLDataType.SMALLINT, this);

    public final TableField<AccessListModel, UInteger> OWNER_ID = createField("owner_id", SQLDataType.INTEGERUNSIGNED, this);

    @Override
    public Class<AccessListModel> getRecordType() {
        return AccessListModel.class;
    }
}
