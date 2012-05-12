package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Faithcaio
 */
public class SubscriptionStorage implements Storage<Integer, String>//Integer = CubeUserID - String = AuctionsID oder MATERIAL-Name von Bukkit
{

    private final Database db = CubeAuctions.getDB();
    private CubeUserManager cuManager;
    
    public SubscriptionStorage()
    {
    }

    public Database getDatabase()
    {
        return this.db;
    }

    public List<String> getListByKey(Integer key)
    {
        try
        {
            ResultSet result = this.db.query("SELECT `id` FROM {{PREFIX}}subscription WHERE cubeuserid=?", key);
            List<String> list = new ArrayList<String>();
            while (result.next())
            {
                list.add(result.getString("sub"));
            }
            return list;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the Subscriptions for CubeUser '" + key + "'!", e);
        }
    }

    public boolean store(Integer cuId, String... object)
    {
        this.createStructure();
        try
        {
            for (String s : object)
            {
                this.db.exec("INSERT INTO {{PREFIX}}subscription (`cubeuserid`, `sub`)"+
                                    "VALUES (?, ?)", cuId, s); 
            }
            return true;   
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Subscriptions !", e);
        }
    }

    public int delete(String... object)
    {//macht nur Sinn bei Löschung von AuktionsSubs zb bei Ablauf
        int dels = 0;
        List<Integer> keys = new ArrayList<Integer>();
        for (String s : object)
        {
            this.db.query("DELETE FROM {{PREFIX}}subscription WHERE sub=?", s);
            ++dels;
        }
        return dels;
    }
    
    public int deleteByKeyAndValue(Integer key, String... object)
    {//Löschen von Sub von einem Spieler
        int dels = 0;
        for (String s : object)
        {
            this.db.query("DELETE FROM {{PREFIX}}subscription WHERE sub=? && cubeuserid=?", s, key);
            ++dels;//TODO Zählung falsch?
        }
        return dels;
    }

    public int deleteByKey(Integer... keys)
    {//macht nur Sinn bei Löschung von allen Subs von einem User
        int dels = 0;
        for (int i : keys)
        {
            this.db.query("DELETE FROM {{PREFIX}}subscription WHERE cubeuserid=?", i);
            ++dels;
        }
        return dels;
    }
    
    public void createStructure()
    {
        this.db.exec(   "CREATE TABLE IF NOT EXISTS `subscription` ("+
                        "`id` int(11) NOT NULL AUTO_INCREMENT,"+    
                        "`cubeuserid` int(11) NOT NULL,"+
                        "`sub` varchar(42) NOT NULL,"+
                        "PRIMARY KEY (`id`),"+
                        "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"+
                        ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
                    );
    }

    public Collection<String> getAll(){throw new UnsupportedOperationException("No Need.");}//macht keinen Sinn
    public String getByKey(Integer key){throw new UnsupportedOperationException("No Need.");}//macht keinen Sinn
    public boolean store(String... object){throw new UnsupportedOperationException("No Need.");}//macht keinen Sinn
}