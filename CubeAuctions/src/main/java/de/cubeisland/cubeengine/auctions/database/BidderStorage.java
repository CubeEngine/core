package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Faithcaio
 */
public class BidderStorage implements Storage<Bidder>
{

    private final Database db = CubeAuctions.getDB();;

    public BidderStorage()
    {
    }

    public Database getDatabase()
    {
        return this.db;
    }

    public Collection<Bidder> getAll()
    {
        try
        {
            ResultSet result = this.db.query("SELECT `cubeuserid`,`notifystate` FROM {{PREFIX}}bidder");

            Collection<Bidder> bidder = new ArrayList<Bidder>();
            while (result.next())
            {
                int cubeuserid = result.getInt("cubeuserid");
                byte notifyState = result.getByte("notifystate");
                bidder.add(new Bidder(cubeuserid , notifyState));
            }

            return bidder;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the bidder from the database!", e);
        }
    }


    public void initialize()
    {
        try
        {
            this.db.exec(   "CREATE TABLE IF NOT EXISTS `bidder` ("+
                            "`cubeuserid` int(11) NOT NULL,"+
                            "`notifystate` smallint(2) NOT NULL,"+
                            "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"+
                            ") ENGINE=MyISAM DEFAULT CHARSET=latin1;"
                        );
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the Bidder-Table !", ex);
        }
    }

    public Bidder get(int key)
    {
        try
        {
            ResultSet result = this.db.query("SELECT `cubeuserid`,`notifystate` FROM {{PREFIX}}bidder WHERE cubeuserid=? LIMIT 1", key);

            if (!result.next())
            {
                return null;
            } 
            
            int cubeuserid = result.getInt("cubeuserid");
            byte notifyState = result.getByte("notifystate");
            return new Bidder(cubeuserid , notifyState);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the Bidder '" + key + "'!", e);
        }
    }

    public void store(Bidder model)
    {
        this.initialize();
        try
        {
            int cubeuserid = model.getId();
            byte notifyState = model.getNotifyState();

            this.db.exec("INSERT INTO {{PREFIX}}bidder (`cubeuserid`, `notifystate`)"+
                                "VALUES (?, ?)", cubeuserid, notifyState); 
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Bidder !", e);
        }
    }

    public void update(Bidder model)
    {
        //TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(Bidder model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean delete(Bidder model)
    {
        return this.delete(model.getId());
    }

    public boolean delete(int id)
    {
        try
        {
            this.db.exec("DELETE FROM {{PREFIX}}bids WHERE cubeuserid=?", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Bidder !", ex);
        }
        
        return true;
    }

    public void clear()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
