package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.basics.general.Mail;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity(name="basicuser")
public class BasicUser implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true)
    public final int key; // User Key
    
    @Attribute(type = AttrType.TIMESTAMP, notnull=false)
    public Timestamp muted;
    
    public List<Mail> mailbox = new ArrayList<Mail>();
    
    @DatabaseConstructor
    public BasicUser(List<Object> args) throws ConversionException
    {
        this.key = Convert.fromObject(Integer.class, args.get(0));
        this.muted = Convert.fromObject(Timestamp.class, args.get(1));
    }

    public BasicUser(User user)
    {
        this.key = user.getKey();
    }

    public Integer getKey()
    {
        return key;
    }

    public void setKey(Integer key)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}