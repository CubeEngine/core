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
package de.cubeisland.cubeengine.conomy.account.storage;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;

import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.UNIQUE;

@SingleKeyEntity(tableName = "accounts", primaryKey = "key", autoIncrement = true, indices = {
    @Index(value = FOREIGN_KEY, fields = "user_id", f_table = "user", f_field = "key"),
    @Index(value = UNIQUE, fields = { "user_id", "name"})
})
public class AccountModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long key;
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    public Long user_id;
    @Attribute(type = AttrType.VARCHAR, length = 64, notnull = false)
    public String name;
    @Attribute(type = AttrType.BIGINT)
    public long value;
    @Attribute(type = AttrType.TINYINT)
    public int mask = 0;

    @Override
    public Long getId()
    {
        return key;
    }

    @Override
    public void setId(Long id)
    {
        this.key = id;
    }

    public AccountModel()
    {}

    public AccountModel(Long user_id, String name, long balance, boolean hidden, boolean needsInvite)
    {
        this.user_id = user_id;
        this.name = name;
        this.value = balance;
        this.mask = (byte)((hidden ? 1 : 0) + (needsInvite ? 2 : 0));
    }

    public AccountModel(Long user_id, String name, long balance, boolean hidden)
    {
        this(user_id, name, balance, hidden, false);
    }

    public boolean needsInvite()
    {
        return (this.mask & 2) == 2;
    }

    public boolean isHidden()
    {
        return (this.mask & 1) == 1;
    }

    public void setNeedsInvite(boolean set)
    {
        if (set)
        {
            this.mask |= 2;
        }
        else
        {
            this.mask &= ~2;
        }
    }

    public void setHidden(boolean set)
    {
        if (set)
        {
            this.mask |= 1;
        }
        else
        {
            this.mask &= ~1;
        }
    }
}
