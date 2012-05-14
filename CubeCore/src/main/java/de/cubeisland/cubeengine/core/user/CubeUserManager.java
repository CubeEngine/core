package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.persistence.Database;
import gnu.trove.map.hash.THashMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

/**
 *
 * @author Faithcaio
 */
public class CubeUserManager {

    private final THashMap<Integer,CubeUser> cubeUserList = new THashMap<Integer,CubeUser>();
    private final CubeUserStorage storage;
    private final Server server;
        
    public CubeUserManager(Database db, Server server)
    {
        this.storage = new CubeUserStorage(db, server);
        this.server = server;
    }
    
    public void addCubeUser(CubeUser user)
    {
        this.storage.store(user);
        cubeUserList.put(user.getId(), user);
    }
    
    public void remCubeUser(CubeUser user)
    {
        this.storage.delete(user);
        //TODO
    }
    
    public CubeUser getCubeServer()
    {
        return this.cubeUserList.get(0);
    }
    
    
    public CubeUser getCubeUser(Integer id)
    {
        if (id == 0) return this.getCubeServer();
        CubeUser user = this.cubeUserList.get(id);
        if (user==null)
        {
            user = new CubeUser(0,null, null);
            this.cubeUserList.put(id, user);
        }
        return user;
    }
    
    public CubeUser getCubeUser(String name)
    {
        
        OfflinePlayer player = server.getOfflinePlayer(name);
        return this.getCubeUser(player);
    }
    
    public CubeUser getCubeUser(OfflinePlayer player)
    {
        CubeUser user = new CubeUser(-1, player, null);
        int id = storage.getCubeUserId(player.getName());//TODO getByObject nimmer da
        user.setId(id);
        this.cubeUserList.put(id, user);
        if (user==null)
        {
            user = new CubeUser(0,null, null);
            this.cubeUserList.put(id, user);
        }
        return user;
    }
}
