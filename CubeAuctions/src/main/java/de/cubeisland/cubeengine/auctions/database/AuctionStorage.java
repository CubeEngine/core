package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.persistence.database.Database;
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
public class AuctionStorage implements Storage<Integer, Auction>
{
    private final Database database = CubeAuctions.getDB();
    private final String TABLE = "auctions";

    public AuctionStorage()
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("auction_get", "SELECT id,cubeuserid,item,amount,timestamp FROM {{" + TABLE + "}} WHERE id=? LIMIT 1");
            this.database.prepareStatement("auction_getall", "SELECT id,cubeuserid,item,amount,timestamp FROM {{" + TABLE + "}}");
            this.database.prepareStatement("auction_store", "INSERT INTO {{" + TABLE + "}} (id,cubeuserid,item,amount,timestamp) VALUES (?,?,?,?,?)");
            this.database.prepareStatement("auction_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("auction_clear", "DELETE FROM {{" + TABLE + "}}");
            //this.database.prepareStatement("auction_update",   "UPDATE {{"+TABLE+"}} SET flags=? WHERE id=?");
            //this.database.prepareStatement("auction_merge",    "INSERT INTO {{"+TABLE+"}} (name,flags) VALUES (?,?) ON DUPLICATE KEY UPDATE flags=values(flags)");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to prepare the statements!", e);
        }
    }

    public Collection<Auction> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("auction_getall");

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

    public void initialize()
    {
        try
        {
            this.database.exec("CREATE TABLE IF NOT EXISTS `auctions` ("
                + "`id` int(10) unsigned NOT NULL,"
                + "`cubeuserid` int(11) NOT NULL,"
                + "`item` varchar(42) NOT NULL,"
                + "`amount` int(11) NOT NULL,"
                + "`timestamp` timestamp NOT NULL,"
                + "PRIMARY KEY (`id`),"
                + "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the Auction-Table !", ex);
        }
    }

    public Auction get(Integer key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("auction_get", key);

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
        try
        {
            int id = model.getKey();
            int cubeUserId = model.getOwner().getKey().getKey();
            String item = Util.convertItem(model.getItemStack());
            int amount = model.getItemStack().getAmount();
            Timestamp time = model.getTimestamp();
            this.database.preparedExec("auction_store", id, cubeUserId, item, amount, time);
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Auction !", e);
        }
    }

    public boolean delete(Auction model)
    {
        return this.delete(model.getKey());
    }

    public boolean delete(Integer id)
    {
        try
        {
            return this.database.preparedExec("auction_delete", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Auction !", ex);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("auction_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
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
