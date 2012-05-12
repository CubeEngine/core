package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
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
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class AuctionBoxStorage implements Storage<Integer, AuctionItem>
{

    private final Database database = CubeAuctions.getDB();
    private CubeUserManager cuManager;
    
    public AuctionBoxStorage()
    {
    }

    public Database getDatabase()
    {
        return this.database;
    }

    public Collection<AuctionItem> getAll()
    {
        try
        {
            ResultSet result = this.database.query("SELECT `cubeuserid` FROM {{PREFIX}}boxes");

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

    public Collection<AuctionItem> getAllByKey(Integer key)
    {
        try
        {
            ResultSet result = this.database.query("SELECT `cubeuserid` FROM {{PREFIX}}boxes WHERE cubeuserid=? LIMIT 1", key);

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

    public boolean store(AuctionItem... object)
    {
        try
        {
            for (AuctionItem auctionItem : object)
            {
                int cubeUserId = auctionItem.getBidder().getId();
                String item = Util.convertItem(auctionItem.getItemStack());
                int amount = auctionItem.getItemStack().getAmount();
                double price = auctionItem.getPrice();
                int oldownerid = auctionItem.getOwner().getId();
                Timestamp time = auctionItem.getTimestamp();
                this.database.exec("INSERT INTO {{PREFIX}}boxes (`cubeuserid`, `item`, `amount`, `price`, `oldownerid`, `timestamp`)"+
                                    "VALUES (?, ?, ?, ?)", cubeUserId, item, amount, price, oldownerid, time); 
            }
            return true;
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the AuctionBoxItems !", e);
        }
    }

    public void delete(AuctionItem auctionItem)
    {
        String item = Util.convertItem(auctionItem.getItemStack());
        int amount = auctionItem.getItemStack().getAmount();

        this.database.exec("DELETE FROM {{PREFIX}}boxes WHERE cubeuserid=? && item=? && amount=? && timestamp=?", 
                auctionItem.getBidder().getId(),item,amount,auctionItem.getTimestamp());
    }

    public int deleteByKey(Integer... keys)
    {
        int dels = 0;
        for (int i : keys)
        {
            this.database.exec("DELETE FROM {{PREFIX}}boxes WHERE cubeuserid=?", i);
            ++dels;
        }
        return dels;
    }
    
    public void update(AuctionItem auctionItem, int newamount)
    {
        String item = Util.convertItem(auctionItem.getItemStack());
        int amount = auctionItem.getItemStack().getAmount();

        this.database.execUpdate("UPDATE {{PREFIX}}boxes SET `amount`=? WHERE cubeuserid=? && item=? && amount=? && timestamp=?", 
                newamount,auctionItem.getBidder().getId(),item,amount,auctionItem.getTimestamp());
    }

    public AuctionItem getByKey(Integer key) {
        throw new UnsupportedOperationException("No Need.");
    }

    public int delete(AuctionItem... object) {
        throw new UnsupportedOperationException("No Need.");
    }
}
