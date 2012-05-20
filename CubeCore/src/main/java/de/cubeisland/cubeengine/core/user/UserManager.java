package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.CubeCore;
import gnu.trove.map.hash.THashMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Faithcaio
 */
public class UserManager
{
    private final THashMap<String, User> users;
    private UserStorage storage;
        
    public UserManager(CubeCore core)
    {
        this.storage = new UserStorage(core.getDB(), core.getServer());
        this.storage.initialize();

        this.users = new THashMap<String, User>();
        for (User user : storage.getAll())
        {
            this.users.put(user.getName(), user);
        }
    }
    
    public UserManager addUser(User user)
    {
        this.storage.store(user);
        this.users.put(user.getName(), user);

        return this;
    }
    
    public UserManager removeUser(User user)
    {
        this.storage.delete(user);
        this.users.remove(user.getName());

        return this;
    }
    
    public User getUser(String name)
    {
        return this.users.get(name);
    }
    
    public User getUser(OfflinePlayer player)
    {
        return this.getUser(player.getName());
    }
    
    public User getUser(CommandSender sender)
    {
        return this.getUser(sender.getName());
    }
    
    public User getUser(int id)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clean()
    {
        this.users.clear();
        this.storage = null;
    }
}
