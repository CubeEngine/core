package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.AuctionItem;
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
public class AuctionBoxStorage implements Storage<Integer, AuctionItem>
{

    private final Database database;
    private final Server server;
    private CubeUserManager cuManager;
    
    public AuctionBoxStorage(Database db, Server server)
    {
        this.database = db;
        this.server = server;
    }

    public Database getDatabase()
    {
        return this.database;
    }

    public Collection<AuctionItem> getAll()
    {
        try
        {
            ResultSet result = this.database.query("SELECT `id` FROM {{PREFIX}}boxes");

            Collection<AuctionItem> auctionItems = new ArrayList<AuctionItem>();
            while (result.next())
            {
                int id = result.getInt("id");
                int cubeUserId = result.getInt("cubeuserid");
                ItemStack item = Util.convertItem(result.getString("item"), result.getShort("amount"));
                Timestamp time = result.getTimestamp("timestamp");
                int ownerId = result.getInt("oldownerid");
                double price = result.getDouble("price");
                auctionItems.add(new AuctionItem(id, cubeUserId, item, time, ownerId, price));
                //Constructor:
                    //public AuctionItem(int id, Bidder bidder, ItemStack item, Timestamp time,Bidder owner, double price)
            }

            return auctionItems;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the AuctionBoxes from the database!", e);
        }
    }

    public AuctionItem getByKey(Integer key)
    {
        try
        {
            ResultSet result = this.database.query("SELECT `id` FROM {{PREFIX}}boxes WHERE id=? LIMIT 1", key);

            if (!result.next())
            {
                return null;
            }
            int id = result.getInt("id");
            int cubeUserId = result.getInt("cubeuserid");
            ItemStack item = Util.convertItem(result.getString("item"), result.getShort("amount"));
            Timestamp time = result.getTimestamp("timestamp");
            int ownerId = result.getInt("oldownerid");
            double price = result.getDouble("price");
            return new AuctionItem(id, cubeUserId, item, time, ownerId, price);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the AuctionBoxItem '" + key + "'!", e);
        }
    }

    public boolean store(AuctionItem... object)
    {
        for (AuctionItem auctionItem : object)
        {
            int id = auctionItem.getId();
            int cubeUserId = auctionItem.getBidder().getId();
            String item = Util.convertItem(auctionItem.getItemStack());
            int amount = auctionItem.getItemStack().getAmount();
            double price = auctionItem.getPrice();
            int oldownerid = auctionItem.getOwner().getId();
            Timestamp time = auctionItem.getTimestamp();
            this.database.query("INSERT INTO {{PREFIX}}boxes (`id`, `cubeuserid`, `item`, `amount`, `price`, `oldownerid`, `timestamp`)"+
                                "VALUES (?, ?, ?, ?, ?)", id, cubeUserId, item, amount, price, oldownerid, time); 
        }
        return true; //TODO
    }

    public int delete(AuctionItem... object)
    {
        List<Integer> keys = new ArrayList<Integer>();
        for (AuctionItem auctionItem : object)
        {
            keys.add(auctionItem.getId());
        }
        return deleteByKey((Integer[])keys.toArray());
    }

    public int deleteByKey(Integer... keys)
    {
        int dels = 0;
        for (int i : keys)
        {
            this.database.query("DELETE FROM {{PREFIX}}boxes WHERE id=?", i);
            ++dels;
        }
        return dels;
    }
}
