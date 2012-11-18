package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.basics.mail.Mail;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.ForeignKey;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "basicuser")
public class BasicUser implements Model<Integer>
{
    @Key
    @ForeignKey(table = "user", field = "key")
    @Attribute(type = AttrType.INT, unsigned = true)
    public final int key; // User Key
    @Attribute(type = AttrType.TIMESTAMP, notnull = false)
    public Timestamp muted;
    @Attribute(type = AttrType.BOOLEAN)
    public boolean godMode;
    public List<Mail> mailbox = new ArrayList<Mail>();

    @DatabaseConstructor
    public BasicUser(List<Object> args) throws ConversionException
    {
        this.key = Integer.valueOf(args.get(0).toString());
        this.muted = (Timestamp)args.get(1);
        this.godMode = (Boolean)args.get(2);
    }

    public BasicUser(User user)
    {
        this.key = user.getKey();
    }

    @Override
    public Integer getKey()
    {
        return key;
    }

    @Override
    public void setKey(Integer key)
    {
        throw new UnsupportedOperationException("Not supported. The BasicUserKey is final!");
    }
}
