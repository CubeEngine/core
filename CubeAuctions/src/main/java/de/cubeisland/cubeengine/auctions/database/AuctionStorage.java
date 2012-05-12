package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.Auction;
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
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;



/**
 *
 * @author Faithcaio
 */
public class AuctionStorage implements Storage<Integer, Auction>{

    private final Database db = CubeAuctions.getDB();

    public AuctionStorage()
    {
    }

    public Collection<Auction> getAll()
    {
        try
        {
            ResultSet result = this.db.query("SELECT `id`,`item`,`amount`,`cubeuserid`,`timestamp` FROM {{PREFIX}}auctions");

            Collection<Auction> auctions = new ArrayList<Auction>();
            while (result.next())
            {
                int id = result.getInt("id");
                ItemStack item = Util.convertItem(result.getString("item"), result.getShort("amount"));
                int cubeUserId = result.getInt("cubeuserid");
                long auctionEnd = result.getTimestamp("timestamp").getTime();
                auctions.add(new Auction(id, item, cubeUserId, auctionEnd));
            }

            return auctions;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the auctions from the database!", e);
        }
    }

    public Auction getByKey(Integer key)
    {
        try
        {
            ResultSet result = this.db.query("SELECT `id`,`item`,`amount`,`cubeuserid`,`timestamp` FROM {{PREFIX}}auctions WHERE id=? LIMIT 1", key);

            if (!result.next())
            {
                return null;
            }
            int id = result.getInt("id");
            ItemStack item = Util.convertItem(result.getString("item"), result.getShort("amount"));
            int cubeUserId = result.getInt("cubeuserid");
            long auctionEnd = result.getTimestamp("timestamp").getTime();
            return new Auction(id, item, cubeUserId, auctionEnd);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the auction '" + key + "'!", e);
        }
    }

    public boolean store(Auction... object)
    {
        this.createStructure();
        try
        {
            for (Auction auction : object)
            {
                int id = auction.getId();
                int cubeUserId = auction.getOwner().getId();
                String item = Util.convertItem(auction.getItemStack());
                int amount = auction.getItemStack().getAmount();
                Timestamp time = auction.getTimestamp();
                this.db.query("INSERT INTO {{PREFIX}}auctions (`id`, `cubeuserid`, `item`, `amount`, `timestamp`)"+
                                    "VALUES (?, ?, ?, ?, ?)", id, cubeUserId, item, amount, time); 
            }
            return true;
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Auctions !", e);
        }
    }

    public int delete(Auction... object)
    {
        List<Integer> keys = new ArrayList<Integer>();
        for (Auction auction : object)
        {
            keys.add(auction.getId());
        }
        return deleteByKey((Integer[])keys.toArray());
    }

    public int deleteByKey(Integer... keys)
    {
        int dels = 0;
        for (int i : keys)
        {
            this.db.query("DELETE FROM {{PREFIX}}auctions WHERE id=?", i);
            ++dels;
        }
        return dels;
    }
    
    public void createStructure()
    {
        this.db.exec( "CREATE TABLE IF NOT EXISTS `auctions` ("+
                            "`id` int(10) unsigned NOT NULL,"+
                            "`cubeuserid` int(11) NOT NULL,"+
                            "`item` varchar(42) NOT NULL,"+
                            "`amount` int(11) NOT NULL,"+
                            "`timestamp` timestamp NOT NULL,"+
                            "PRIMARY KEY (`id`),"+
                            "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"+
                            ") ENGINE=MyISAM DEFAULT CHARSET=latin1;"
                          );
    }

    public Database getDatabase()
    {
        return this.db;
    }
}
