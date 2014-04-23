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
package de.cubeisland.engine.kits;

import de.cubeisland.engine.core.storage.database.Table;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableKitsGiven extends Table<KitsGiven>
{
    public static TableKitsGiven TABLE_KITS;

    public TableKitsGiven(String prefix)
    {
        super(prefix + "kits", new Version(1));
        this.setPrimaryKey(USERID);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), USERID);
        this.addFields(USERID, KITNAME, AMOUNT);
        TABLE_KITS = this;
    }

    public final TableField<KitsGiven, UInteger> USERID = createField("userId", U_INTEGER.nullable(false), this);
    public final TableField<KitsGiven, String> KITNAME = createField("kitName", SQLDataType.VARCHAR.length(50).nullable(false), this);
    public final TableField<KitsGiven, Integer> AMOUNT = createField("amount", SQLDataType.INTEGER.nullable(false), this);

    @Override
    public Class<KitsGiven> getRecordType() {
        return KitsGiven.class;
    }
}
