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
package de.cubeisland.engine.itemrepair.repair.storage;

import java.sql.Connection;
import java.sql.SQLException;

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public class TableRepairBlock extends AutoIncrementTable<RepairBlockModel, UInteger>
{
    public static TableRepairBlock TABLE_REPAIR_BLOCK;

    public TableRepairBlock(String prefix)
    {
        super(prefix + "repairblocks", new Version(1));
        this.setAIKey(ID);
        this.addUniqueKey(WORLD, X, Y, Z);
        this.addForeignKey(TABLE_WORLD.getPrimaryKey(), WORLD);
        TABLE_REPAIR_BLOCK = this;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`world` int(10) unsigned DEFAULT NULL,\n" +
                                        "`x` int(11) DEFAULT NULL,\n" +
                                        "`y` int(11) DEFAULT NULL,\n" +
                                        "`z` int(11) DEFAULT NULL,\n" +
                                        "`type` varchar(64) DEFAULT NULL,\n" +
                                        "PRIMARY KEY (`id`),\n" +
                                        "UNIQUE KEY `loc` (`world`,`x`,`y`,`z`),\n" +
                                        "FOREIGN KEY `f_world`(`world`) REFERENCES " + TABLE_WORLD.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    public final TableField<RepairBlockModel, UInteger> ID = createField("id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<RepairBlockModel, UInteger> WORLD = createField("world", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<RepairBlockModel, Integer> X = createField("x", SQLDataType.INTEGER, this);
    public final TableField<RepairBlockModel, Integer> Y = createField("y", SQLDataType.INTEGER, this);
    public final TableField<RepairBlockModel, Integer> Z = createField("z", SQLDataType.INTEGER, this);
    public final TableField<RepairBlockModel, String> TYPE = createField("type", SQLDataType.VARCHAR.length(64), this);

    @Override
    public Class<RepairBlockModel> getRecordType() {
        return RepairBlockModel.class;
    }
}
