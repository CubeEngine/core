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

import de.cubeisland.engine.core.storage.database.Table;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class TableIgnorelist extends Table<IgnoreList>
{
    public static TableIgnorelist TABLE_IGNORE_LIST;

    public TableIgnorelist(String prefix)
    {
        super(prefix + "ignorelist", new Version(1));
        this.addForeignKey(TABLE_USER.getPrimaryKey(), KEY);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), IGNORE);
        this.setPrimaryKey(KEY, IGNORE);
        this.addFields(KEY, IGNORE);
        TABLE_IGNORE_LIST = this;
    }

    public final TableField<IgnoreList, UInteger> KEY = createField("key", U_INTEGER.nullable(false), this);
    public final TableField<IgnoreList, UInteger> IGNORE = createField("ignore", U_INTEGER.nullable(false), this);

    @Override
    public Class<IgnoreList> getRecordType() {
        return IgnoreList.class;
    }
}
