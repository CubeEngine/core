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
package de.cubeisland.cubeengine.basics.storage;

import de.cubeisland.cubeengine.basics.command.mail.Mail;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;

@SingleKeyEntity(tableName = "basicuser", primaryKey = "key", autoIncrement = false, indices = {
    @Index(value = FOREIGN_KEY, fields = "key", f_table = "user", f_field = "key")
})
public class BasicUser implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key; // User Key
    @Attribute(type = AttrType.TIMESTAMP, notnull = false)
    public Timestamp muted;
    @Attribute(type = AttrType.BOOLEAN)
    public boolean godMode;

    public List<Mail> mailbox = new ArrayList<Mail>();

    public BasicUser()
    {}

    public BasicUser(User user)
    {
        this.key = user.getId();
    }

    @Override
    public Long getId()
    {
        return key;
    }

    @Override
    public void setId(Long id)
    {
        throw new UnsupportedOperationException("Not supported. The BasicUserKey is final!");
    }
}
