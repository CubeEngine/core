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
package de.cubeisland.engine.roles.storage;

import de.cubeisland.engine.core.storage.database.Table;
import de.cubeisland.engine.core.util.Version;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public class TableRole extends Table<AssignedRole>
{
    public static TableRole TABLE_ROLE;

    public TableRole(String prefix)
    {
        super(prefix + "roles", new Version(1));
        this.setPrimaryKey(USERID, WORLDID, ROLENAME);
        this.addForeignKey(TABLE_USER.getPrimaryKey(), USERID);
        this.addForeignKey(TABLE_WORLD.getPrimaryKey(), WORLDID);
        this.addFields(USERID, WORLDID, ROLENAME);
        TABLE_ROLE = this;
    }

    public final TableField<AssignedRole, UInteger> USERID = createField("userId", U_INTEGER.nullable(false), this);
    public final TableField<AssignedRole, UInteger> WORLDID = createField("worldId", U_INTEGER.nullable(false), this);
    public final TableField<AssignedRole, String> ROLENAME = createField("roleName", SQLDataType.VARCHAR.length(255).nullable(false), this);

    @Override
    public Class<AssignedRole> getRecordType() {
        return AssignedRole.class;
    }
}
