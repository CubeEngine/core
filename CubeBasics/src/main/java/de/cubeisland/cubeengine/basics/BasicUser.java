package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import java.util.ArrayList;
import java.util.List;

public class BasicUser implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true)
    public final int key; // User Key
    @Attribute(type = AttrType.TEXT)
    public List<String> mailbox = new ArrayList<String>(); //PlayerName: message //TODO perhaps save this in a separate Table

    @DatabaseConstructor
    public BasicUser(List<Object> args) throws ConversionException
    {
        this.key = Convert.fromObject(Integer.class, args.get(0));
        this.mailbox = Convert.matchGenericConverter(List.class).
            fromObject(args.get(1), this.mailbox, String.class);
    }

    public BasicUser(User user)
    {
        this.key = user.getKey();
    }

    /**
     * Adds a mail to this users mailbox.
     * If the user the mail came from is null assume it was the console.
     * 
     * @param from the user the mail comes from
     * @param message the message
     */
    public void addMail(User from, String message)
    {
        if (from == null)
        {
            this.mailbox.add("CONSOLE: " + message);
        }
        else
        {
            this.mailbox.add(from.getName() + ": " + message);
        }
    }

    public String readMail()
    {
        return this.mailbox.remove(0);
    }

    public int countMail()
    {
        return this.mailbox.size();
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