package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.auction.Bid;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.user.CubeUser;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Server;

/**
 *
 * @author Faithcaio
 */
public class CubeAuctionsSubsStorage implements Storage<Integer, String>//Integer = CubeUserID - String = AuctionsID oder MATERIAL-Name von Bukkit
{

    private final Database database;
    private final Server server;
    private CubeUserManager cuManager;
    
    public CubeAuctionsSubsStorage(Database db, Server server)
    {
        this.database = db;
        this.server = server;
    }

    public Database getDatabase()
    {
        return this.database;
    }

    public List<String> getListByKey(Integer key)
    {
        try
        {
            ResultSet result = this.database.query("SELECT `id` FROM {{PREFIX}}subscription WHERE cubeuserid=?", key);
            List<String> list = new ArrayList<String>();
            while (result.next())
                list.add(result.getString("sub"));
            return list;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the Subscriptions for CubeUser '" + key + "'!", e);
        }
    }

    public boolean store(Integer cuId, String... object)
    {//id is autoincrement
        for (String s : object)
        {
            this.database.query("INSERT INTO {{PREFIX}}subscription (`cubeuserid`, `sub`)"+
                                "VALUES (?, ?)", cuId, s); 
        }
        return false; //TODO
    }

    public int delete(String... object)
    {//macht nur Sinn bei Löschung von AuktionsSubs zb bei Ablauf
        int dels = 0;
        List<Integer> keys = new ArrayList<Integer>();
        for (String s : object)
        {
            this.database.query("DELETE FROM {{PREFIX}}subscription WHERE sub=?", s);
            ++dels;
        }
        return dels;
    }
    
    public int deleteByKeyAndValue(Integer key, String... object)
    {//Löschen von Sub von einem Spieler
        int dels = 0;
        for (String s : object)
        {
            this.database.query("DELETE FROM {{PREFIX}}subscription WHERE sub=? && cubeuserid=?", s, key);
            ++dels;//TODO Zählung falsch?
        }
        return dels;
    }

    public int deleteByKey(Integer... keys)
    {//macht nur Sinn bei Löschung von allen Subs von einem User
        int dels = 0;
        for (int i : keys)
        {
            this.database.query("DELETE FROM {{PREFIX}}subscription WHERE cubeuserid=?", i);
            ++dels;
        }
        return dels;
    }
    
    public Collection<String> getAll(){return null;}//macht keinen Sinn
    public String getByKey(Integer key){return null;}//macht keinen Sinn
    public boolean store(String... object){return false;}//macht keinen Sinn
}