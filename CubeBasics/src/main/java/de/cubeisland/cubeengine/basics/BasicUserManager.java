package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.user.User;
import java.util.HashMap;

/**
 *
 * @author Anselm Brehme
 */
public class BasicUserManager
{
    private HashMap<User,BasicUser> basicusers = new HashMap<User,BasicUser>();
    
    public BasicUser getBasicUser(User user)
    {
        return this.basicusers.get(user);
    }
    
    public void addBasicUser(User user)
    {
        this.basicusers.put(user, new BasicUser(user));
    }
         
    public void removeBasicUser(User user)
    {
        basicusers.remove(user);
    }
}
