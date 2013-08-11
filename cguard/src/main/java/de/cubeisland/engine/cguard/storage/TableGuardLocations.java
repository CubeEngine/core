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
package de.cubeisland.engine.cguard.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.TableCreator;
import de.cubeisland.engine.core.storage.database.mysql.Keys;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.core.world.WorldEntity;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.cguard.storage.TableGuards.TABLE_GUARD;
import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public class TableGuardLocations extends TableImpl<GuardLocationModel>implements TableCreator<GuardLocationModel>
{
        public static TableGuardLocations TABLE_GUARD_LOCATION;

        private TableGuardLocations(String prefix)
        {
            super(prefix + "guardlocation");
            IDENTITY = Keys.identity(this, this.ID);
            PRIMARY_KEY = Keys.uniqueKey(this, this.ID);
            UNIQUE_LOCATION = Keys.uniqueKey(this, this.WORLD_ID, this.X, this.Y, this.Z);
            FOREIGN_WORLD = Keys.foreignKey(TABLE_WORLD.PRIMARY_KEY, this, this.WORLD_ID);
            FOREIGN_GUARD = Keys.foreignKey(TABLE_GUARD.PRIMARY_KEY, this, this.GUARD_ID);
        }

    public static TableGuardLocations initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_GUARD_LOCATION = new TableGuardLocations(config.tablePrefix);
        return TABLE_GUARD_LOCATION;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`id` int(10) unsigned NOT NULL,\n" +
                                        "`world_id` int(10) unsigned NOT NULL,\n" +
                                        "`x` int(11) NOT NULL,\n" +
                                        "`y` int(11) NOT NULL,\n" +
                                        "`z` int(11) NOT NULL,\n" +
                                        "`guard_id` int(10) unsigned NOT NULL,\n" +
                                        "PRIMARY KEY (`id`),\n" +
                                        "UNIQUE KEY (`world_id`, `x`, `y`, `z`),\n" +
                                        "FOREIGN KEY f_world (`world_id`) REFERENCES " + TABLE_WORLD.getName() + " (`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY f_guard (`guard_id`) REFERENCES " + TABLE_GUARD.getName() + " (`id`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<GuardLocationModel, UInteger> IDENTITY;
    public final UniqueKey<GuardLocationModel> PRIMARY_KEY;
    public final UniqueKey<GuardLocationModel> UNIQUE_LOCATION;
    public final ForeignKey<GuardLocationModel, WorldEntity> FOREIGN_WORLD;
    public final ForeignKey<GuardLocationModel, GuardModel> FOREIGN_GUARD;

    public final TableField<GuardLocationModel, UInteger> ID = createField("id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<GuardLocationModel, UInteger> WORLD_ID = createField("world_id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<GuardLocationModel, Integer> X = createField("x", SQLDataType.INTEGER, this);
    public final TableField<GuardLocationModel, Integer> Y = createField("y", SQLDataType.INTEGER, this);
    public final TableField<GuardLocationModel, Integer> Z = createField("z", SQLDataType.INTEGER, this);
    public final TableField<GuardLocationModel, UInteger> GUARD_ID = createField("guard_id", SQLDataType.INTEGERUNSIGNED, this);

    @Override
    public Identity<GuardLocationModel, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<GuardLocationModel> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<GuardLocationModel>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY, UNIQUE_LOCATION);
    }

    @Override
    public List<ForeignKey<GuardLocationModel, ?>> getReferences() {
        return Arrays.<ForeignKey<GuardLocationModel, ?>>asList(FOREIGN_WORLD, FOREIGN_GUARD);
    }

    @Override
    public Class<GuardLocationModel> getRecordType() {
        return GuardLocationModel.class;
    }
}
