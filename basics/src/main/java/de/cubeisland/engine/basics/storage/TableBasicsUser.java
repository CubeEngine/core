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
package de.cubeisland.engine.basics.storage;

import java.sql.Timestamp;

import de.cubeisland.engine.core.storage.database.Table;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.types.UInteger;
import org.jooq.util.mysql.MySQLDataType;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableBasicsUser extends Table<BasicsUserEntity>
{
    public static TableBasicsUser TABLE_BASIC_USER;

    public TableBasicsUser(String prefix)
    {
        super(prefix + "basicuser", new Version(1));
        this.setPrimaryKey(KEY);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), KEY);
        this.addFields(KEY, MUTED, GODMODE);
        TABLE_BASIC_USER = this;
    }

    public final TableField<BasicsUserEntity, UInteger> KEY = createField("key", U_INTEGER.nullable(false), this);
    public final TableField<BasicsUserEntity, Timestamp> MUTED = createField("muted", MySQLDataType.DATETIME, this);
    public final TableField<BasicsUserEntity, Boolean> GODMODE = createField("godMode", BOOLEAN.nullable(false), this);

    @Override
    public Class<BasicsUserEntity> getRecordType() {
        return BasicsUserEntity.class;
    }
}
