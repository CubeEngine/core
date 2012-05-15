package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.CubeCore;
import gnu.trove.map.hash.THashMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class CubeUserManager {

    private final THashMap<Integer,CubeUser> cubeUserList = new THashMap<Integer,CubeUser>();
    private final CubeUserStorage storage = new CubeUserStorage();
    private final Server server = CubeCore.getInstance().getServer();
    private static CubeUserManager instance = null;
        
    public CubeUserManager()
    {
    }
    
    public void addCubeUser(CubeUser user)
    {
        this.storage.store(user);
        cubeUserList.put(user.getId(), user);
    }
    
    public static CubeUserManager getInstance()
    {
        if (instance==null)
            instance = new CubeUserManager();
        return instance;
    }
    
    public void remCubeUser(CubeUser user)
    {
        cubeUserList.remove(user.getId());
        this.storage.delete(user);
    }
    
    public CubeUser getCubeServer()
    {
        return this.cubeUserList.get(1);
    }
    
    
    public CubeUser getCubeUser(Integer id)
    {
        CubeUser user;
        if (id == 1) 
        {
            user = this.getCubeServer();
            if (user==null)
            {
                user = new CubeUser(1,null, null);
                this.cubeUserList.put(id, user);
                storage.store(user);
            }
            return user;
        }
        else
            return this.cubeUserList.get(id);

    }
    
    public CubeUser getCubeUser(String name)
    {
        
        OfflinePlayer player = server.getOfflinePlayer(name);
        return this.getCubeUser(player);
    }
    
    public CubeUser getCubeUser(CommandSender sender)
    {
        if (sender instanceof Player)
        {
            OfflinePlayer player = (Player)sender;
            return this.getCubeUser(player);
        }
        return this.getCubeServer();
        
    }
    
    public CubeUser getCubeUser(OfflinePlayer player)
    {
        int id = storage.getCubeUserId(player.getName());
        if (id==-1)
        {
            CubeUser user = new CubeUser(player);
            this.storage.store(user);
            id = storage.getCubeUserId(player.getName());
            user.setId(id);
            this.cubeUserList.put(id, user);
        }
        return cubeUserList.get(id);
    }
    
    public void loadDatabase()
    {
        for (CubeUser user : storage.getAll())
        {
            int id = user.getId();
            this.cubeUserList.put(id, user);
        }
        
    }
}
