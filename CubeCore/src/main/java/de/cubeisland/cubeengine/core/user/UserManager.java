package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.user.event.UserCreatedEvent;
import gnu.trove.map.hash.THashMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class UserManager
{
    private final Core core;
    private final THashMap<String, User> users;
    private UserStorage storage;
    private final Server server;
        
    public UserManager(Core core, Server server)
    {
        this.core = core;
        this.storage = new UserStorage(core.getDatabase(), server);
        this.storage.initialize();
        this.server = server;

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
        UserCreatedEvent event = new UserCreatedEvent(this.core, user);
        server.getPluginManager().callEvent(event);
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
    
    public User getUser(Player player)
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
        this.storage.clear();
        this.storage = null;
    }
}
