package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.persistence.Database;
import gnu.trove.map.hash.THashMap;
import org.bukkit.Server;

/**
 *
 * @author Faithcaio
 */
public class CubeUserManager {

    private final THashMap<Integer,CubeUser> cubeUserList = new THashMap<Integer,CubeUser>();
    private final CubeUserStorage storage;
        
    public CubeUserManager(Database db, Server server)
    {
        this.storage = new CubeUserStorage(db, server);
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
}
