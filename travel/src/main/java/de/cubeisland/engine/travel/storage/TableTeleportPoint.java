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

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public class TableTeleportPoint extends AutoIncrementTable<TeleportPointModel, UInteger>
{
    public static TableTeleportPoint TABLE_TP_POINT;

    public TableTeleportPoint(String prefix)
    {
        super(prefix + "teleportpoints", new Version(1));
        this.setAIKey(KEY);
        this.addUniqueKey(OWNER, NAME, TYPE);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), OWNER);
        this.addForeignKey(TABLE_WORLD.getPrimaryKey(), WORLD);
        this.addFields(KEY, OWNER, TYPE, VISIBILITY, WORLD, X, Y, Z, YAW, PITCH, NAME, WELCOMEMSG);
        TABLE_TP_POINT = this;
    }

    public final TableField<TeleportPointModel, UInteger> KEY = createField("key", U_INTEGER.nullable(false), this);
    public final TableField<TeleportPointModel, UInteger> OWNER = createField("owner", U_INTEGER.nullable(false), this);
    public final TableField<TeleportPointModel, Short> TYPE = createField("type", SQLDataType.SMALLINT.nullable(false), this);
    public final TableField<TeleportPointModel, Short> VISIBILITY = createField("visibility", SQLDataType.SMALLINT.nullable(false), this);
    public final TableField<TeleportPointModel, UInteger> WORLD = createField("world", U_INTEGER.nullable(false), this);
    public final TableField<TeleportPointModel, Double> X = createField("x", SQLDataType.DOUBLE.nullable(false), this);
    public final TableField<TeleportPointModel, Double> Y = createField("y", SQLDataType.DOUBLE.nullable(false), this);
    public final TableField<TeleportPointModel, Double> Z = createField("z", SQLDataType.DOUBLE.nullable(false), this);
    public final TableField<TeleportPointModel, Double> YAW = createField("yaw", SQLDataType.FLOAT.nullable(false), this);
    public final TableField<TeleportPointModel, Double> PITCH = createField("pitch", SQLDataType.FLOAT.nullable(false), this);
    public final TableField<TeleportPointModel, String> NAME = createField("name", SQLDataType.VARCHAR.length(32).nullable(false), this);
    public final TableField<TeleportPointModel, String> WELCOMEMSG = createField("welcomemsg", LONGTEXT, this);

    @Override
    public Class<TeleportPointModel> getRecordType()
    {
        return TeleportPointModel.class;
    }
}
