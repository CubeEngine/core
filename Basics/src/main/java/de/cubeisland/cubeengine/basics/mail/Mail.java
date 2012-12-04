package de.cubeisland.cubeengine.basics.mail;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleIntKeyEntity;
import de.cubeisland.cubeengine.core.user.User;

@SingleIntKeyEntity(tableName = "mail", primaryKey = "key")
public class Mail implements Model<Integer>
{
    @Attribute(type = AttrType.INT, unsigned = true)
    public int key;
    @Attribute(type = AttrType.VARCHAR, length = 100)
    public String message;
    @Index(value = Index.IndexType.FOREIGNKEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public int userId;
    @Index(value = Index.IndexType.FOREIGNKEY, f_table = "user", f_field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public int senderId;

    public Mail(int userId, int senderId, String message)
    {
        this.message = message;
        this.userId = userId;
        this.senderId = senderId;
    }

    @Override
    public Integer getKey()
    {
        return key;
    }

    @Override
    public void setKey(Integer key)
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
