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

import static de.cubeisland.engine.locker.storage.TableLocks.TABLE_GUARD;
import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableAccessList extends TableImpl<AccessListModel> implements TableCreator<AccessListModel>
{
    public static TableAccessList TABLE_ACCESS_LIST;

    private TableAccessList(String prefix)
    {
        super(prefix + "lockaccesslist");
        IDENTITY = Keys.identity(this, this.ID);
        PRIMARY_KEY = Keys.uniqueKey(this, this.ID);
        UNIQUE_ACCESS = Keys.uniqueKey(this, this.USER_ID, this.LOCK_ID);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.PRIMARY_KEY, this, this.USER_ID);
        FOREIGN_GUARD = Keys.foreignKey(TABLE_GUARD.PRIMARY_KEY, this, this.LOCK_ID);
    }

    public static TableAccessList initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_ACCESS_LIST = new TableAccessList(config.tablePrefix);
        return TABLE_ACCESS_LIST;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`user_id` int(10) unsigned NOT NULL,\n" +
                                        "`lock_id` int(10) unsigned DEFAULT NULL,\n" +
                                        "`level` smallint NOT NULL,\n" +
                                        "PRIMARY KEY (`id`),\n" +
                                        "UNIQUE KEY (`user_id`, `lock_id`),\n" +
                                        "FOREIGN KEY f_user (`user_id`) REFERENCES " + TABLE_USER.getName() + " (`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY f_guard (`lock_id`) REFERENCES " + TABLE_GUARD.getName() + " (`id`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<AccessListModel, UInteger> IDENTITY;
    public final UniqueKey<AccessListModel> PRIMARY_KEY;
    public final UniqueKey<AccessListModel> UNIQUE_ACCESS;
    public final ForeignKey<AccessListModel, UserEntity> FOREIGN_USER;
    public final ForeignKey<AccessListModel, LockModel> FOREIGN_GUARD;

    public final TableField<AccessListModel, UInteger> ID = createField("id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<AccessListModel, UInteger> USER_ID = createField("user_id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<AccessListModel, UInteger> LOCK_ID = createField("lock_id", SQLDataType.INTEGERUNSIGNED, this);
    // BitMask granting the user access to a protection (this is NOT restricting) (if ACCESS_PUT is not set on a donation chest it does not matter)
    public final TableField<AccessListModel, Short> LEVEL = createField("level", SQLDataType.SMALLINT, this);


    @Override
    public Identity<AccessListModel, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<AccessListModel> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<AccessListModel>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY, UNIQUE_ACCESS);
    }

    @Override
    public List<ForeignKey<AccessListModel, ?>> getReferences() {
        return Arrays.<ForeignKey<AccessListModel, ?>>asList(FOREIGN_USER, FOREIGN_GUARD);
    }

    @Override
    public Class<AccessListModel> getRecordType() {
        return AccessListModel.class;
    }
}
