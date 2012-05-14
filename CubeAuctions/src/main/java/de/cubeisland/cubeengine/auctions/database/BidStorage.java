package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.auction.Bid;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
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
    private final Database database = CubeAuctions.getDB();
    private final String TABLE = "bids";

    public BidStorage()
    {
        try
        {
            this.database.prepareStatement("bid_get", "SELECT id,auctionid,cubeuserid,amount,timestamp FROM {{" + TABLE + "}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("bid_getall", "SELECT id,auctionid,cubeuserid,amount,timestamp FROM {{" + TABLE + "}}");
            this.database.prepareStatement("bid_store", "INSERT INTO {{" + TABLE + "}} (auctionid,cubeuserid,amount,timestamp) VALUES (?,?,?,?)");
            this.database.prepareStatement("bid_get_exact", "SELECT id FROM {{" + TABLE + "}}"
                + " WHERE auctionid=? && cubeuserid=? && amount=? && timestamp=? LIMIT 1");
            this.database.prepareStatement("bid_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("bid_delete_auction", "DELETE FROM {{" + TABLE + "}} WHERE auctionid=?");
            this.database.prepareStatement("bid_delete_auction_user", "DELETE FROM {{" + TABLE + "}} WHERE auctionid=? && cubeuserid=?");
            this.database.prepareStatement("bid_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("bid_update", "UPDATE {{" + TABLE + "}} SET cubeuserid=? WHERE id=?");
            this.database.prepareStatement("bid_giveId", "UPDATE {{" + TABLE + "}} SET id=? WHERE id=-1");//TODO!!!
            //this.database.prepareStatement("auction_merge",    "INSERT INTO {{"+TABLE+"}} (name,flags) VALUES (?,?) ON DUPLICATE KEY UPDATE flags=values(flags)");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to prepare the statements!", e);
        }
    }

    public Collection<Bid> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("bid_getall");

            Collection<Bid> bids = new ArrayList<Bid>();
            while (result.next())
            {
                int id = result.getInt("id");
                int cubeUserId = result.getInt("cubeuserid");
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

    public Bid get(int key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("bid_get", key);
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
            throw new StorageException("Failed to load the Bid '" + key + "'!", e);
        }
    }

    public void giveId(Bid model)
    {
        if (model.getId() == -1)
        {
            int newId = -1;
            try
            {
                int auctionId = model.getAuctionId();
                Bidder bidder = model.getBidder();
                double amount = model.getAmount();
                Timestamp timestamp = model.getTimestamp();

                this.database.preparedExec("bid_store", auctionId, bidder.getId(), amount, timestamp);
                ResultSet result = this.database.preparedQuery("bid_get_exact", auctionId, bidder.getId(), amount, timestamp);

                if (result.next())
                {
                    newId = result.getInt("id");
                }
                else
                {
                    return;
                }
            }
            catch (SQLException ex)
            {
                throw new StorageException("Failed to give the Bid an Id !", ex);
            }
            model.setId(newId);
        }
    }

    public void initialize()
    {
        try
        {
            this.database.exec("CREATE TABLE IF NOT EXISTS `bids` ("
                + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                + "`auctionid` int(11) NOT NULL,"
                + "`cubeuserid` int(11) NOT NULL,"
                + "`amount` int(11) NOT NULL,"
                + "`timestamp` timestamp NOT NULL,"
                + "PRIMARY KEY (`id`),"
                + "FOREIGN KEY (auctionid) REFERENCES auctions(id),"
                + "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
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
            this.giveId(model);
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
            this.database.preparedUpdate("bid_update", model.getBidder().getId(), model.getId());
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
            this.database.preparedExec("bid_delete_auction_user", auctionId, bidderId);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Bids of Auction " + auctionId + " of Bidder " + bidderId + "!", ex);
        }
    }

    public void deleteByAuction(int auctionId)
    {
        try
        {
            this.database.preparedExec("bid_delete_auction", auctionId);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Bids of Auction " + auctionId + " !", ex);
        }
    }

    public boolean delete(int id)
    {
        try
        {
            return this.database.preparedExec("bid_delete", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Bid !", ex);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("bid_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }

    public void merge(Bid model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
