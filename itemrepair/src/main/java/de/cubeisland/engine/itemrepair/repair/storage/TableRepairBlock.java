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
        this.addFields(ID, WORLD, X,Y,Z, TYPE);
        TABLE_REPAIR_BLOCK = this;
    }

    public final TableField<RepairBlockModel, UInteger> ID = createField("id", U_INTEGER.nullable(false), this);
    public final TableField<RepairBlockModel, UInteger> WORLD = createField("world", U_INTEGER.nullable(false), this);
    public final TableField<RepairBlockModel, Integer> X = createField("x", SQLDataType.INTEGER.nullable(false), this);
    public final TableField<RepairBlockModel, Integer> Y = createField("y", SQLDataType.INTEGER.nullable(false), this);
    public final TableField<RepairBlockModel, Integer> Z = createField("z", SQLDataType.INTEGER.nullable(false), this);
    public final TableField<RepairBlockModel, String> TYPE = createField("type", SQLDataType.VARCHAR.length(64).nullable(false), this);

    @Override
    public Class<RepairBlockModel> getRecordType() {
        return RepairBlockModel.class;
    }
}
