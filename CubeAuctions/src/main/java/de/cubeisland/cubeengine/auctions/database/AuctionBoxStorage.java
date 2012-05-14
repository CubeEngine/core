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
public class AuctionBoxStorage implements Storage<AuctionItem>
{

    private final Database db = CubeAuctions.getDB();
    private CubeUserManager cuManager;
    
    public AuctionBoxStorage()
    {
    }

    public Database getDatabase()
    {
        return this.db;
    }

    public Collection<AuctionItem> getAll()
    {
        try
        {
            ResultSet result = this.db.query("SELECT `cubeuserid` FROM {{PREFIX}}boxes");

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
            ResultSet result = this.db.query("SELECT `cubeuserid` FROM {{PREFIX}}boxes WHERE cubeuserid=? LIMIT 1", key);

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
            this.db.exec(   "CREATE TABLE IF NOT EXISTS `boxes` ("+
                            "`id` int(11) NOT NULL AUTO_INCREMENT,"+    
                            "`cubeuserid` int(11) NOT NULL,"+
                            "`item` varchar(42) NOT NULL,"+
                            "`amount` int(11) NOT NULL,"+
                            "`price` decimal(11,2) NOT NULL,"+
                            "`timestamp` timestamp NOT NULL,"+
                            "`oldownerid` int(11) NOT NULL,"+
                            "PRIMARY KEY (`id`),"+    
                            "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"+
                            ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
                        );
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the AuctionBox-Table !", ex);
        }
    }

    

    public void store(AuctionItem model)
    {
        this.initialize();
        try
        {
            int cubeUserId = model.getBidder().getId();
            String item = Util.convertItem(model.getItemStack());
            int amount = model.getItemStack().getAmount();
            double price = model.getPrice();
            int oldownerid = model.getOwner().getId();
            Timestamp time = model.getTimestamp();
            this.db.exec("INSERT INTO {{PREFIX}}boxes (`cubeuserid`, `item`, `amount`, `price`, `oldownerid`, `timestamp`)"+
                                "VALUES (?, ?, ?, ?)", cubeUserId, item, amount, price, oldownerid, time); 
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the AuctionBoxItems !", e);
        }
    }

    public void update(AuctionItem model)
    {
        String item = Util.convertItem(model.getItemStack());
        int amount = model.getItemStack().getAmount();
        try
        {
            this.db.exec("UPDATE {{PREFIX}}boxes SET `amount`=? WHERE cubeuserid=? && item=? && amount=? && timestamp=?", 
                    model.getItemStack().getAmount(),model.getBidder().getId(),item,amount,model.getTimestamp());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to update the AuctionBoxItem !", ex);
        }
    }

    

    public boolean delete(AuctionItem model)
    {
        String item = Util.convertItem(model.getItemStack());
        int amount = model.getItemStack().getAmount();
        try
        {
            this.db.exec("DELETE FROM {{PREFIX}}boxes WHERE cubeuserid=? && item=? && amount=? && timestamp=?", 
                    model.getBidder().getId(),item,amount,model.getTimestamp());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the AuctionBoxItem !", ex);
        }
        return true;
    }

    public boolean delete(int id)
    {
        try
        {
            this.db.exec("DELETE FROM {{PREFIX}}boxes WHERE cubeuserid=?", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the AuctionBoxItem !", ex);
        }
        return true;
    }

    public void clear()
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
