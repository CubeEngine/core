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
package de.cubeisland.engine.roles.storage;

import java.lang.Byte;
import java.lang.String;
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
import de.cubeisland.engine.core.world.WorldEntity;
import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public class TablePerm extends TableImpl<UserPermission> implements TableCreator<UserPermission>
{
    public static TablePerm TABLE_PERM;

    private TablePerm(String prefix)
    {
        super(prefix + "userperms");
        PRIMARY_KEY = Keys.uniqueKey(this, this.USERID, this.WORLDID, this.PERM);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.PRIMARY_KEY, this, this.USERID);
        FOREIGN_WORLD = Keys.foreignKey(TABLE_WORLD.PRIMARY_KEY, this, this.WORLDID);
    }

    public static TablePerm initTable(Database database)
    {
        if (TABLE_PERM != null) throw new IllegalStateException();
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_PERM = new TablePerm(config.tablePrefix);
        return TABLE_PERM;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`userId` int(10) unsigned NOT NULL,\n" +
                                        "`worldId` int(10) unsigned NOT NULL,\n" +
                                        "`perm` varchar(255) NOT NULL,\n" +
                                        "`isSet` tinyint(1) NOT NULL,\n" +
                                        "PRIMARY KEY (`userId`,`worldId`,`perm`)," +
                                        "FOREIGN KEY `f_user`(`userId`) REFERENCES " + TABLE_USER.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY `f_world`(`worldId`) REFERENCES " + TABLE_WORLD.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final UniqueKey<UserPermission> PRIMARY_KEY;
    public final ForeignKey<UserPermission, UserEntity> FOREIGN_USER;
    public final ForeignKey<UserPermission, WorldEntity> FOREIGN_WORLD;

    public final TableField<UserPermission, UInteger> USERID = createField("userId", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<UserPermission, UInteger> WORLDID = createField("worldId", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<UserPermission, String> PERM = createField("perm", SQLDataType.VARCHAR.length(255), this);
    public final TableField<UserPermission, Byte> ISSET = createField("isSet", SQLDataType.TINYINT, this);

    @Override
    public UniqueKey<UserPermission> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<UserPermission>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY);
    }

    @Override
    public List<ForeignKey<UserPermission, ?>> getReferences() {
        return Arrays.<ForeignKey<UserPermission, ?>>asList(FOREIGN_WORLD, FOREIGN_USER);
    }

    @Override
    public Class<UserPermission> getRecordType() {
        return UserPermission.class;
    }
}