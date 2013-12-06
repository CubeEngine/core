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
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;
import static de.cubeisland.engine.locker.storage.TableLocks.TABLE_LOCK;

public class TableLockLocations extends AutoIncrementTable<LockLocationModel, UInteger>
{
    public static TableLockLocations TABLE_LOCK_LOCATION;

    public TableLockLocations(String prefix)
    {
        super(prefix + "locklocation", new Version(1));
        this.setAIKey(ID);
        this.addUniqueKey(WORLD_ID, X, Y, Z);
        this.addForeignKey(TABLE_WORLD.getPrimaryKey(), WORLD_ID);
        this.addForeignKey(TABLE_LOCK.getPrimaryKey(), GUARD_ID);
        TABLE_LOCK_LOCATION = this;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`world_id` int(10) unsigned NOT NULL,\n" +
                                        "`x` int(11) NOT NULL,\n" +
                                        "`y` int(11) NOT NULL,\n" +
                                        "`z` int(11) NOT NULL,\n" +
                                        "`chunkX` int(11) NOT NULL,\n" +
                                        "`chunkZ` int(11) NOT NULL,\n" +
                                        "`lock_id` int(10) unsigned NOT NULL,\n" +
                                        "PRIMARY KEY (`id`),\n" +
                                        "KEY `i_chunk` (`chunkX`, `chunkZ`),\n" +
                                        "UNIQUE KEY (`world_id`, `x`, `y`, `z`),\n" +
                                        "FOREIGN KEY f_world (`world_id`) REFERENCES " + TABLE_WORLD.getName() + " (`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY f_guard (`lock_id`) REFERENCES " + TABLE_LOCK.getName() + " (`id`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    public final TableField<LockLocationModel, UInteger> ID = createField("id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<LockLocationModel, UInteger> WORLD_ID = createField("world_id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<LockLocationModel, Integer> X = createField("x", SQLDataType.INTEGER, this);
    public final TableField<LockLocationModel, Integer> Y = createField("y", SQLDataType.INTEGER, this);
    public final TableField<LockLocationModel, Integer> Z = createField("z", SQLDataType.INTEGER, this);
    public final TableField<LockLocationModel, Integer> CHUNKX = createField("chunkX", SQLDataType.INTEGER, this);
    public final TableField<LockLocationModel, Integer> CHUNKZ = createField("chunkZ", SQLDataType.INTEGER, this);
    public final TableField<LockLocationModel, UInteger> GUARD_ID = createField("lock_id", SQLDataType.INTEGERUNSIGNED, this);

    @Override
    public Class<LockLocationModel> getRecordType() {
        return LockLocationModel.class;
    }
}
