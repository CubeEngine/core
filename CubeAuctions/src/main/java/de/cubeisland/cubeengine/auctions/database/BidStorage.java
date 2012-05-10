package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.auction.Bid;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.user.CubeUser;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import de.cubeisland.libMinecraft.bitmask.LongBitMask;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

/**
 *
 * @author Faithcaio
 */
public class BidStorage implements Storage<Integer, Bid>
{

    private final Database database;
    private final Server server;
    private CubeUserManager cuManager;
    
    public BidStorage(Database db, Server server)
    {
        this.database = db;
        this.server = server;
    }

    public Database getDatabase()
    {
        return this.database;
    }

    public Collection<Bid> getAll()
    {
        try
        {
            ResultSet result = this.database.query("SELECT `id` FROM {{PREFIX}}bids");

            Collection<Bid> bids = new ArrayList<Bid>();
            while (result.next())
            {
                int id = result.getInt("id");
                CubeUser bidder = cuManager.getCubeUser(result.getInt("cubeuserid"));
                double amount = result.getDouble("amount");
                Timestamp time = result.getTimestamp("timestamp");
                
                int auctionId = result.getInt("auctionid");//TODO Der Auktion zuordnen
                
                bids.add(new Bid(id, (Bidder)bidder, amount, time));
                
                //Constructor:
                    //public Bid(int id,Bidder bidder, double amount, Timestamp timestamp)
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
            ResultSet result = this.database.query("SELECT `id` FROM {{PREFIX}}bids WHERE id=? LIMIT 1", key);

            if (!result.next())
            {
                return null;
            }
            int id = result.getInt("id");
            CubeUser bidder = cuManager.getCubeUser(result.getInt("cubeuserid"));
            double amount = result.getDouble("amount");
            Timestamp time = result.getTimestamp("timestamp");

            int auctionId = result.getInt("auctionid");//TODO Der Auktion zuordnen

            return new Bid(id, (Bidder)bidder, amount, time);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the AuctionBoxItem '" + key + "'!", e);
        }
    }

    public boolean store(Bid... object)
    {
        for (Bid bid : object)
        {
            
            int id = bid.getId();
            CubeUser bidder = bid.getBidder();
            double amount = bid.getAmount();
            Timestamp time = bid.getTimestamp();

            int auctionId = 0;//TODO Der Auktion zuordnen

            this.database.query("INSERT INTO {{PREFIX}}bids (`id`, `auctionid`,`cubeuserid`, `amount`, `timestamp`)"+
                                "VALUES (?, ?, ?, ?, ?)", id, auctionId, bidder.getId(), amount, time); 
        }
        return true; //TODO
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

    public int deleteByKey(Integer... keys)
    {
        int dels = 0;
        for (int i : keys)
        {
            this.database.query("DELETE FROM {{PREFIX}}bids WHERE id=?", i);
            ++dels;
        }
        return dels;
    }
}
