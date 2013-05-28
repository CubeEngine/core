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
import de.cubeisland.cubeengine.core.user.User;

import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.UNIQUE;

@SingleKeyEntity(tableName = "account_access", primaryKey = "id", autoIncrement = true, indices = {
    @Index(value = FOREIGN_KEY, fields = "userId", f_table = "user", f_field = "key"),
    @Index(value = FOREIGN_KEY, fields = "accountId", f_table = "accounts", f_field = "key"),
    @Index(value = UNIQUE, fields = { "userId", "accountId" })
})
public class BankAccessModel implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public long id;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long userId;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long accountId;
    @Attribute(type = AttrType.TINYINT)
    public byte accessLevel;

    public String name;

    public static final byte OWNER = 1;
    public static final byte MEMBER = 2;

    public static final byte INVITED = 4;

    public BankAccessModel(long id, long userId, long accountId, byte accessLevel, String name)
    {
        this.id = id;
        this.userId = userId;
        this.accountId = accountId;
        this.accessLevel = accessLevel;
        this.name = name;
    }

    public BankAccessModel(AccountModel accountModel, User user, byte type)
    {
        this.userId = user.key;
        this.accountId = accountModel.key;
        this.accessLevel = type;
        this.name = accountModel.name;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public void setId(Long id)
    {
        this.id = id;
    }

    public BankAccessModel()
    {}
}
