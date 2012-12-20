package de.cubeisland.cubeengine.basics.mail;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;

@SingleKeyEntity(tableName = "mail", primaryKey = "key", autoIncrement = true)
public class Mail implements Model<Long>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long   key;
    @Attribute(type = AttrType.VARCHAR, length = 100)
    public String message;
    @Index(value = Index.IndexType.FOREIGN_KEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public long   userId;
    @Index(value = Index.IndexType.FOREIGN_KEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public long   senderId;

    public Mail(long userId, long senderId, String message)
    {
        this.message = message;
        this.userId = userId;
        this.senderId = senderId;
    }

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
        User user = CubeEngine.getUserManager().getUser(this.senderId);
        if (user == null)
        {
            return "&cCONSOLE&f: " + this.message;
        }
        return "&2" + user.getName() + "&f: " + this.message;
    }
}
