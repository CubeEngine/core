package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.CubeCore;
import gnu.trove.map.hash.THashMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Faithcaio
 */
public class CubeUserManager
{
    private final THashMap<String, CubeUser> users;
    private CubeUserStorage storage;
        
    public CubeUserManager(CubeCore core)
    {
        this.storage = new CubeUserStorage(core.getDB(), core.getServer());
        this.storage.initialize();

        this.users = new THashMap<String, CubeUser>();
        for (CubeUser user : storage.getAll())
        {
            this.users.put(user.getName(), user);
        }
    }
    
    public CubeUserManager addUser(CubeUser user)
    {
        this.storage.store(user);
        this.users.put(user.getName(), user);

        return this;
    }
    
    public CubeUserManager removeUser(CubeUser user)
    {
        this.storage.delete(user);
        this.users.remove(user.getName());

        return this;
    }
    
    public CubeUser getUser(String name)
    {
        return this.getUser(name);
    }
    
    public CubeUser getUser(OfflinePlayer player)
    {
        return this.getUser(player.getName());
    }
    
    public CubeUser getUser(CommandSender sender)
    {
        return this.getUser(sender.getName());
    }
    
    public CubeUser getUser(int id)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clean()
    {
        this.users.clear();
        this.storage = null;
    }
}
