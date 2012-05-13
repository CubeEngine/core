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

    private THashMap<Integer,CubeUser> cubeUserList = new THashMap<Integer,CubeUser>();
    private static CubeUserStorage storage;
        
    public CubeUserManager(Database db, Server server) 
    {
        storage = new CubeUserStorage(db, server);
    }
    
    public void addCubeUser(CubeUser... user)
    {
        storage.store(user);
        for (CubeUser cu : user)
        {
            cubeUserList.put(cu.getId(), cu);
        }
    }
    
    public void remCubeUser(CubeUser... user)
    {
        storage.delete(user);
        //TODO
    }
    
    public CubeUser getCubeUser(Integer id)
    {
        CubeUser user = this.cubeUserList.get(id);
        if (user==null)
        {
            user = new CubeUser(0,null, null);
            this.cubeUserList.put(id, user);
        }
        return user;
    }
    
    public CubeUser getCubeUser(OfflinePlayer player)
    {
        CubeUser user = storage.getByKey(player.getName());
        if (user == null)
        {
            storage.store(new CubeUser(player));
            return getCubeUser(player);
        }
        return user;
    }
    
    public CubeUser getCubeUser(String name)
    {
        return storage.getByKey(name);
    }
}
