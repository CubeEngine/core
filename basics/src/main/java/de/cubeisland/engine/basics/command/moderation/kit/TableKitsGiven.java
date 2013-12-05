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

public class TableKitsGiven extends TableImpl<KitsGiven> implements TableCreator<KitsGiven>
{
    public static TableKitsGiven TABLE_KITS;

    private TableKitsGiven(String prefix)
    {
        super(prefix + "kits");
        PRIMARY_KEY = Keys.uniqueKey(this, this.USERID, this.KITNAME);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.getPrimaryKey(), this, this.USERID);
    }

    public static TableKitsGiven initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_KITS = new TableKitsGiven(config.tablePrefix);
        return TABLE_KITS;
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

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final UniqueKey<KitsGiven> PRIMARY_KEY;
    public final ForeignKey<KitsGiven, UserEntity> FOREIGN_USER;

    public final org.jooq.TableField<KitsGiven, org.jooq.types.UInteger> USERID = createField("userId", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, this);
    public final org.jooq.TableField<KitsGiven, java.lang.String> KITNAME = createField("kitName", org.jooq.impl.SQLDataType.VARCHAR.length(50), this);
    public final org.jooq.TableField<KitsGiven, java.lang.Integer> AMOUNT = createField("amount", org.jooq.impl.SQLDataType.INTEGER, this);

    @Override
    public UniqueKey<KitsGiven> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<KitsGiven>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY);
    }

    @Override
    public List<ForeignKey<KitsGiven, ?>> getReferences() {
        return Arrays.<ForeignKey<KitsGiven, ?>>asList(FOREIGN_USER);
    }

    @Override
    public Class<KitsGiven> getRecordType() {
        return KitsGiven.class;
    }
}
