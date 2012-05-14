package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.auction.Bid;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Faithcaio
 */
public class BidStorage implements Storage<Bid>
{

    private final Database db = CubeAuctions.getDB();
    private CubeUserManager cuManager;
    
    public BidStorage()
    {
    }

    public Database getDatabase()
    {
        return this.db;
    }

    public Collection<Bid> getAll()
    {
        try
        {
            ResultSet result = this.db.query("SELECT `id` FROM {{PREFIX}}bids");

            Collection<Bid> bids = new ArrayList<Bid>();
            while (result.next())
            {
                int id = result.getInt("id");
                int cubeUserId =result.getInt("cubeuserid");
                double amount = result.getDouble("amount");
                Timestamp time = result.getTimestamp("timestamp");
                
                int auctionId = result.getInt("auctionid");
                
                bids.add(new Bid(id, cubeUserId, auctionId, amount, time));
            }

            return bids;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the Bids from the database!", e);
        }
    }

    public Bid getByKey(Integer key)
    {
        try
        {
            ResultSet result = this.db.query("SELECT `id` FROM {{PREFIX}}bids WHERE id=? LIMIT 1", key);
            if (!result.next())
            {
                return null;
            }
            int id = result.getInt("id");
            int cubeUserId = result.getInt("cubeuserid");
            double amount = result.getDouble("amount");
            Timestamp time = result.getTimestamp("timestamp");

            int auctionId = result.getInt("auctionid");//TODO Der Auktion zuordnen

            return new Bid(id, cubeUserId, auctionId, amount, time);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the AuctionBoxItem '" + key + "'!", e);
        }
    }


    
   //TODO################################## das muss weg
    public int getNextBidId()
    {
        this.initialize();
        try
        {
            ResultSet result = this.db.query("SELECT `id` FROM {{PREFIX}}bids ORDER BY id DESC LIMIT 1");
            if (!result.next())
            {
                return 1;
            }
            return result.getInt("id")+1;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to get next BidId !", e);
        }
    }
    
//##################################
    


    public void initialize()
    {
        try
        {
            this.db.exec(   "CREATE TABLE IF NOT EXISTS `bids` ("+
                            "`id` int(11) NOT NULL AUTO_INCREMENT,"+    
                            "`auctionid` int(11) NOT NULL,"+
                            "`cubeuserid` int(11) NOT NULL,"+
                            "`amount` int(11) NOT NULL,"+
                            "`timestamp` timestamp NOT NULL,"+
                            "PRIMARY KEY (`id`),"+
                            "FOREIGN KEY (auctionid) REFERENCES auctions(id),"+        
                            "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"+
                            ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
                        );
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the Bid-Table !", ex);
        }
    }

    

    public void store(Bid model)
    {
        try
        {
            int id = model.getId();
            Bidder bidder = model.getBidder();
            double amount = model.getAmount();
            Timestamp time = model.getTimestamp();

            int auctionId = model.getAuctionId();

            this.db.exec("INSERT INTO {{PREFIX}}bids (`id`, `auctionid`,`cubeuserid`, `amount`, `timestamp`)"+
                                "VALUES (?, ?, ?, ?, ?)", id, auctionId, bidder.getId(), amount, time); 
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Bids !", e);
        }
    }

    public void update(Bid model)
    {
        try
        {
            this.db.exec("UPDATE {{PREFIX}}bids SET `cubeuserid`=? WHERE `id`=?", model.getBidder().getId(), model.getId());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to update the Bid !", ex);
        }
    }

    public boolean delete(Bid model)
    {
        return this.delete(model.getId());
    }
    
    
    public void deleteByAuctionByUser(int auctionId, int bidderId)
    {
        try
        {
            this.db.exec("DELETE FROM {{PREFIX}}bids WHERE auctionid=? && cubeuserid=?", auctionId, bidderId );
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Bids of Auction "+ auctionId +" of Bidder " + bidderId +"!", ex);
        }
    }
    
    public void deleteByAuction(int auctionId)
    {
        try
        {
            this.db.exec("DELETE FROM {{PREFIX}}bids WHERE auctionid=?", auctionId );
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Bids of Auction "+ auctionId +" !", ex);
        }
    }

    public boolean delete(int id)
    {
        try
        {
            this.db.exec("DELETE FROM {{PREFIX}}bids WHERE id=?", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Bid !", ex);
        }
        return true;
    }

    public void clear()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void merge(Bid model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Bid get(int key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
