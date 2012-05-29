package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.core.user.User;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Faithcaio
 */
public class BasicUser implements Model<User>
{
    User user;
    private boolean unlimitedItems = false; //no need to safe in DB -> reset on restart
    //TODO storage
    private List<String> mailbox = new ArrayList<String>(); //PlayerName: message

    public BasicUser(User user)
    {
        this.user = user;
    }
    
    public BasicUser(User user, List<String> mailbox)
    {
        this.user = user;
        this.mailbox = mailbox;
    }
    
    public User getKey()
    {
        return this.user;
    }

    public boolean hasUnlimitedItems()
    {
        return unlimitedItems;
    }

    public void setUnlimitedItems(boolean unlimitedItems)
    {
        this.unlimitedItems = unlimitedItems;
    }

    public void setKey(User key)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    public void addMail(User user, String message)
    {
        this.mailbox.add(user.getName()+": "+message);
    }
    
    public String readMail()
    {
        return this.mailbox.remove(0);
    }
    
    public int countMail()
    {
        return this.mailbox.size();
    }
    
}
