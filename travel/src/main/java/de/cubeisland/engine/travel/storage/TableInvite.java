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
package de.cubeisland.engine.travel.storage;

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
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.travel.storage.TableTeleportPoint.TABLE_TP_POINT;

public class TableInvite extends TableImpl<TeleportInvite> implements TableCreator<TeleportInvite>
{
    public static TableInvite TABLE_INVITE;

    private TableInvite(String prefix)
    {
        super(prefix + "teleportinvites");
        PRIMARY_KEY = Keys.uniqueKey(this, this.USERKEY, this.TELEPORTPOINT);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.PRIMARY_KEY, this, this.USERKEY);
        FOREIGN_TPPOINT = Keys.foreignKey(TABLE_TP_POINT.PRIMARY_KEY, this, this.TELEPORTPOINT);
    }

    public static TableInvite initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_INVITE = new TableInvite(config.tablePrefix);
        return TABLE_INVITE;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`teleportpoint` int(10) unsigned NOT NULL,\n" +
                                        "`userkey` int(10) unsigned NOT NULL,\n" +
                                        "PRIMARY KEY (`teleportpoint`,`userkey`),\n" +
                                        "FOREIGN KEY `f_tppoint`(`teleportpoint`) REFERENCES " + TABLE_USER.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY `f_user`(`userkey`) REFERENCES " + TABLE_USER.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final UniqueKey<TeleportInvite> PRIMARY_KEY;
    public final ForeignKey<TeleportInvite, UserEntity> FOREIGN_USER;
    public final ForeignKey<TeleportInvite, TeleportPointModel> FOREIGN_TPPOINT;

    public final org.jooq.TableField<TeleportInvite, org.jooq.types.UInteger> TELEPORTPOINT = createField("teleportpoint", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, this);
    public final org.jooq.TableField<TeleportInvite, org.jooq.types.UInteger> USERKEY = createField("userkey", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, this);

    @Override
    public UniqueKey<TeleportInvite> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<TeleportInvite>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY);
    }

    @Override
    public List<ForeignKey<TeleportInvite, ?>> getReferences() {
        return Arrays.<ForeignKey<TeleportInvite, ?>>asList(FOREIGN_USER, FOREIGN_TPPOINT);
    }

    @Override
    public Class<TeleportInvite> getRecordType() {
        return TeleportInvite.class;
    }
}
