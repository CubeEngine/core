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
import java.util.List;

/**
 *
 * @author Faithcaio
 */
public class BidStorage implements Storage<Integer, Bid>
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
                
                int auctionId = result.getInt("auctionid");//TODO Der Auktion zuordnen
                
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

    public boolean store(Bid... object)
    {
        try
        {
            for (Bid bid : object)
            {

                int id = bid.getId();
                Bidder bidder = bid.getBidder();
                double amount = bid.getAmount();
                Timestamp time = bid.getTimestamp();

                int auctionId = bid.getAuctionId();

                this.db.exec("INSERT INTO {{PREFIX}}bids (`id`, `auctionid`,`cubeuserid`, `amount`, `timestamp`)"+
                                    "VALUES (?, ?, ?, ?, ?)", id, auctionId, bidder.getId(), amount, time); 
            }
            return true;
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Bids !", e);
        }
    }

    public int delete(Bid... object)
    {
        List<Integer> keys = new ArrayList<Integer>();
        for (Bid bid : object)
        {
            keys.add(bid.getId());
        }
        return deleteByKey((Integer[])keys.toArray());
    }
    
    public void delete(int auctionId, int bidderId)
    {
        this.db.exec("DELETE FROM {{PREFIX}}bids WHERE auctionid=? && cubeuserid=?", auctionId, bidderId );
    }
    
    public void deleteByAuction(int auctionId)
    {
        this.db.exec("DELETE FROM {{PREFIX}}bids WHERE auctionid=?", auctionId );
    }

    public int deleteByKey(Integer... keys)
    {
        int dels = 0;
        for (int i : keys)
        {
            this.db.exec("DELETE FROM {{PREFIX}}bids WHERE id=?", i);
            ++dels;
        }
        return dels;
    }
    
    public int getNextBidId()
    {
        this.createStructure();
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
    
    public void updateBidder(Bid bid, Bidder bidder)
    {
        this.db.execUpdate("UPDATE {{PREFIX}}bids SET `cubeuserid`=? WHERE `id`=?", bidder.getId(), bid.getId());
    }
    
    public void createStructure()
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
}
