package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.AuctionItem;
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
public class AuctionBoxStorage implements Storage<AuctionItem>
{
    private final Database database = CubeAuctions.getDB();
    private final String TABLE = "boxes";

    public AuctionBoxStorage()
    {
        this.initialize();
        try
        {
            this.database.prepareStatement("box_getall", "SELECT id,cubeuserid,item,amount,price,timestamp,oldownerid FROM {{" + TABLE + "}}");
            this.database.prepareStatement("box_getall_user", "SELECT id,cubeuserid,item,amount,price,timestamp,oldownerid FROM {{" + TABLE + "}} WHERE cubeuserid=?");
            this.database.prepareStatement("box_store", "INSERT INTO {{" + TABLE + "}} (cubeuserid,item,amount,price,timestamp,oldownerid) VALUES (?,?,?,?,?,?)");
            this.database.prepareStatement("box_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("box_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("box_update", "UPDATE {{" + TABLE + "}} SET amount=? WHERE id=?");
            //this.database.prepareStatement("auction_merge",    "INSERT INTO {{"+TABLE+"}} (name,flags) VALUES (?,?) ON DUPLICATE KEY UPDATE flags=values(flags)");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to prepare the statements!", e);
        }
    }

    public Database getDatabase()
    {
        return this.database;
    }

    public Collection<AuctionItem> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("box_getall");

            Collection<AuctionItem> auctionItems = new ArrayList<AuctionItem>();
            while (result.next())
            {
                int cubeUserId = result.getInt("cubeuserid");
                ItemStack item = Util.convertItem(result.getString("item"), result.getShort("amount"));
                Timestamp time = result.getTimestamp("timestamp");
                int ownerId = result.getInt("oldownerid");
                double price = result.getDouble("price");
                auctionItems.add(new AuctionItem(cubeUserId, item, time, ownerId, price));
            }

            return auctionItems;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the AuctionBoxes from the database!", e);
        }
    }

    public Collection<AuctionItem> getAllByUser(Integer key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("box_getall_user", key);

            Collection<AuctionItem> auctionItems = new ArrayList<AuctionItem>();
            while (result.next())
            {
                int cubeUserId = result.getInt("cubeuserid");
                ItemStack item = Util.convertItem(result.getString("item"), result.getShort("amount"));
                Timestamp time = result.getTimestamp("timestamp");
                int ownerId = result.getInt("oldownerid");
                double price = result.getDouble("price");
                auctionItems.add(new AuctionItem(cubeUserId, item, time, ownerId, price));
            }

            return auctionItems;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the AuctionBoxItem '" + key + "'!", e);
        }
    }

    public void initialize()
    {
        try
        {
            this.database.exec("CREATE TABLE IF NOT EXISTS `boxes` ("
                + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                + "`cubeuserid` int(11) NOT NULL,"
                + "`item` varchar(42) NOT NULL,"
                + "`amount` int(11) NOT NULL,"
                + "`price` decimal(11,2) NOT NULL,"
                + "`timestamp` timestamp NOT NULL,"
                + "`oldownerid` int(11) NOT NULL,"
                + "PRIMARY KEY (`id`),"
                + "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the AuctionBox-Table !", ex);
        }
    }

    public void store(AuctionItem model)
    {
        try
        {
            int cubeUserId = model.getBidder().getId();
            String item = Util.convertItem(model.getItemStack());
            int amount = model.getItemStack().getAmount();
            double price = model.getPrice();
            int oldownerid = model.getOwner().getId();
            Timestamp time = model.getTimestamp();
            this.database.preparedExec("box_store", cubeUserId, item, amount, price, time, oldownerid);
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the AuctionBoxItems !", e);
        }
    }

    public void update(AuctionItem model)
    {
        int amount = model.getItemStack().getAmount();
        try
        {
            this.database.preparedExec("box_update", amount, model.getId());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to update the AuctionBoxItem !", ex);
        }
    }

    public boolean delete(AuctionItem model)
    {
        return this.delete(model.getId());
    }

    public boolean delete(int id)
    {
        try
        {
            return this.database.preparedExec("box_delete", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the AuctionBoxItem !", ex);
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

    public void merge(AuctionItem model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AuctionItem get(int key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
