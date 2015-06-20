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
package de.cubeisland.engine.module.authorization.storage;

import de.cubeisland.engine.module.core.util.Version;
import de.cubeisland.engine.module.service.database.AutoIncrementTable;
import de.cubeisland.engine.module.service.database.Database;
import org.jooq.TableField;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.module.service.user.TableUser.TABLE_USER;
import static org.jooq.impl.SQLDataType.VARBINARY;

public class TableAuth extends AutoIncrementTable<Auth, UInteger>
{
    public static TableAuth TABLE_AUTH;
    public final TableField<Auth, UInteger> ID = createField("key", U_INTEGER.nullable(false), this);
    public final TableField<Auth, byte[]> PASSWD = createField("passwd", VARBINARY.length(128), this);

    public TableAuth(String prefix, Database db)
    {
        super(prefix + "mail", new Version(1), db);
        setAIKey(ID);
        addForeignKey(TABLE_USER.getPrimaryKey(), ID);
        addFields(ID, PASSWD);
        TABLE_AUTH = this;
    }

    @Override
    public Class<Auth> getRecordType()
    {
        return Auth.class;
    }
}
