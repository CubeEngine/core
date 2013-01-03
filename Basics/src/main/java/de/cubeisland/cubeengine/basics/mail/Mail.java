package de.cubeisland.cubeengine.basics.mail;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.FOREIGN_KEY;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.user.User;

@SingleKeyEntity(tableName = "mail", primaryKey = "key", autoIncrement = true,
                 indices =
{
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
    @Attribute(type = AttrType.INT, unsigned = true)
    public long senderId;

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
