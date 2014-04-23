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

import de.cubeisland.engine.core.storage.database.AutoIncrementTable;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.locker.storage.TableLocks.TABLE_LOCK;

public class TableAccessList extends AutoIncrementTable<AccessListModel, UInteger>
{
    public static TableAccessList TABLE_ACCESS_LIST;

    public TableAccessList(String prefix)
    {
        super(prefix + "lockaccesslist", new Version(1));
        this.setAIKey(ID);
        this.addUniqueKey(USER_ID, LOCK_ID);
        this.addUniqueKey(USER_ID, OWNER_ID);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), USER_ID);
        this.addForeignKey(TABLE_LOCK.getPrimaryKey(), LOCK_ID);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), OWNER_ID);
        this.addFields(ID, USER_ID, LOCK_ID, LEVEL, OWNER_ID);
        TABLE_ACCESS_LIST = this;
    }

    public final TableField<AccessListModel, UInteger> ID = createField("id", U_INTEGER.nullable(false), this);
    public final TableField<AccessListModel, UInteger> USER_ID = createField("user_id", U_INTEGER.nullable(false), this);
    public final TableField<AccessListModel, UInteger> LOCK_ID = createField("lock_id", U_INTEGER, this);
    // BitMask granting the user access to a protection (this is NOT restricting) (if ACCESS_PUT is not set on a donation chest it does not matter)
    public final TableField<AccessListModel, Short> LEVEL = createField("level", SQLDataType.SMALLINT.nullable(false), this);

    public final TableField<AccessListModel, UInteger> OWNER_ID = createField("owner_id", U_INTEGER, this);

    @Override
    public Class<AccessListModel> getRecordType() {
        return AccessListModel.class;
    }
}
