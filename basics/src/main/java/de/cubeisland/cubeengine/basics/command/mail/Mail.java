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
package de.cubeisland.cubeengine.basics.command.mail;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;

import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;

@SingleKeyEntity(tableName = "mail", primaryKey = "key", autoIncrement = true, indices = {
    @Index(value = FOREIGN_KEY, fields = "userId", f_table = "user", f_field = "key"),
    @Index(value = FOREIGN_KEY, fields = "senderId", f_table = "user", f_field = "key")
})
public class Mail implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key;
    @Attribute(type = AttrType.VARCHAR, length = 100)
    public String message;
    @Attribute(type = AttrType.INT, unsigned = true)
    public long userId;
    @Attribute(type = AttrType.INT, unsigned = true, notnull = false)
    public Long senderId;

    public Mail(long userId, Long senderId, String message)
    {
        this.message = message;
        this.userId = userId;
        this.senderId = senderId;
    }

    public Mail() {}

    @Override
    public Long getKey()
    {
        return key;
    }

    @Override
    public void setKey(Long key)
    {
        this.key = key;
    }

    @Override
    public String toString()
    {
        if (this.senderId == null || this.senderId == 0)
        {
            return "&cCONSOLE&f: " + this.message;
        }
        User user = CubeEngine.getUserManager().getUser(this.senderId);
        return "&2" + user.getName() + "&f: " + this.message;
    }
}
