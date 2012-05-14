package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Faithcaio
 */
public class SubscriptionStorage implements Storage<Bidder>
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

    public List<String> getListByUser(Integer key)
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

    public boolean store(Integer cuId, String sub)
    {
        this.initialize();
        try
        {
            this.db.exec("INSERT INTO {{PREFIX}}subscription (`cubeuserid`, `sub`)"+
                                "VALUES (?, ?)", cuId, sub); 

        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Subscriptions !", e);
        }
        return true;
    }

    public void deleteAllOfSub(String sub)
    {   try
        {
            //macht nur Sinn bei Löschung von AuktionsSubs zb bei Ablauf
               this.db.exec("DELETE FROM {{PREFIX}}subscription WHERE sub=?", sub);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Subscriptions !", ex);
        }

    }
    
    public void deleteByKeyAndValue(Integer key, String sub)
    {//Löschen von Sub von einem Spieler
        try
        {
            this.db.query("DELETE FROM {{PREFIX}}subscription WHERE sub=? && cubeuserid=?", sub, key);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Subscription !", ex);
        }

    }



    
    

    public void initialize()
    {
        try
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
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the Subscription-Table !", ex);
        }
    }

    public Bidder get(int key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Bidder> getAll()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void store(Bidder model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(Bidder model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(Bidder model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(Bidder model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(int id)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}