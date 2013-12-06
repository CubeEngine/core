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
package de.cubeisland.engine.powersigns.storage;

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;
import org.jooq.util.mysql.MySQLDataType;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public class TablePowerSign extends AutoIncrementTable<PowerSignModel, UInteger>
{
    public static TablePowerSign TABLE_POWER_SIGN;

    public TablePowerSign(String prefix)
    {
        super(prefix + "powersign", new Version(1));
        this.setAIKey(ID);
        this.addUniqueKey(WORLD, X, Y, Z);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), OWNER_ID);
        this.addForeignKey(TABLE_WORLD.getPrimaryKey(), WORLD);
        this.addFields(ID, OWNER_ID, PSID, WORLD, X,Y,Z, CHUNKX, CHUNKZ, DATA);
        TABLE_POWER_SIGN = this;
    }

    public final TableField<PowerSignModel, UInteger> ID = createField("id", U_INTEGER.nullable(false), this);
    public final TableField<PowerSignModel, UInteger> OWNER_ID = createField("owner_id", U_INTEGER, this);
    public final TableField<PowerSignModel, String> PSID = createField("psid", SQLDataType.VARCHAR.length(6), this);
    public final TableField<PowerSignModel, UInteger> WORLD = createField("world", U_INTEGER, this);
    public final TableField<PowerSignModel, Integer> X = createField("x", SQLDataType.INTEGER.nullable(false), this);
    public final TableField<PowerSignModel, Integer> Y = createField("y", SQLDataType.INTEGER.nullable(false), this);
    public final TableField<PowerSignModel, Integer> Z = createField("z", SQLDataType.INTEGER.nullable(false), this);

    public final TableField<PowerSignModel, Integer> CHUNKX = createField("chunkx", SQLDataType.INTEGER.nullable(false), this);
    public final TableField<PowerSignModel, Integer> CHUNKZ = createField("chunkz", SQLDataType.INTEGER.nullable(false), this);

    public final TableField<PowerSignModel, String> DATA = createField("data", MySQLDataType.TEXT, this);

    @Override
    public Class<PowerSignModel> getRecordType() {
        return PowerSignModel.class;
    }
}
