package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.util.List;

@Entity(name = "mail")
public class Mail implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true, ai = true)
    public int key;
    @Attribute(type = AttrType.VARCHAR, length = 100)
    public String message;
    @Attribute(type = AttrType.INT, unsigned = true)
    public int userId;
    @Attribute(type = AttrType.INT, unsigned = true)
    public int senderId;

    public Mail(int userId, int senderId, String message)
    {
        this.message = message;
        this.userId = userId;
        this.senderId = senderId;
    }

    @DatabaseConstructor
    public Mail(List<Object> args) throws ConversionException
    {
        this.key = Convert.fromObject(Integer.class, args.get(0));
        this.message = args.get(1).toString();
        this.userId = Convert.fromObject(Integer.class, args.get(2));
        this.senderId = Convert.fromObject(Integer.class, args.get(3));
    }

    public Integer getKey()
    {
        return key;
    }

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
            return "&cCONSOLE%f: " + this.message;
        }
        return "&2" + user.getName() + "&f: " + this.message;
    }
}