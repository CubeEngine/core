package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.inventory.ItemStack;



/**
 *
 * @author Faithcaio
 */
public class AuctionStorage implements Storage<Auction>{

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

    public Database getDatabase()
    {
        return this.db;
    }

    public void initialize()
    {
        try
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
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the Auction-Table !", ex);
        }
    }

    public Auction get(int key)
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

    public void store(Auction model)
    {
        this.initialize();
        try
        {
            int id = model.getId();
            int cubeUserId = model.getOwner().getId();
            String item = Util.convertItem(model.getItemStack());
            int amount = model.getItemStack().getAmount();
            Timestamp time = model.getTimestamp();
            this.db.exec("INSERT INTO {{PREFIX}}auctions (`id`, `cubeuserid`, `item`, `amount`, `timestamp`)"+
                                "VALUES (?, ?, ?, ?, ?)", id, cubeUserId, item, amount, time); 
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Auctions !", e);
        }
    }


    public boolean delete(Auction model)
    {
        return this.delete(model.getId());
    }

    public boolean delete(int id)
    {
        try
        {
            this.db.exec("DELETE FROM {{PREFIX}}auctions WHERE id=?", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Auctions !", ex);
        }
        return true;
    }

    public void clear()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void update(Auction model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(Auction model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
