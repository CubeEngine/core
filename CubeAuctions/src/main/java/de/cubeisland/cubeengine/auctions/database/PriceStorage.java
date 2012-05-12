package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.PricedItemStack;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class PriceStorage implements Storage<ItemStack, PricedItemStack>//TODO vlt anders machen?
{
    private final Database db = CubeAuctions.getDB();
    private CubeUserManager cuManager;

    public PriceStorage()
    {
    }

    public Database getDatabase()
    {
        return this.db;
    }

    public Collection<PricedItemStack> getAll()
    {
        try
        {
            ResultSet result = this.db.query("SELECT `item`,`price`,`timessold` FROM {{PREFIX}}priceditem");

            Collection<PricedItemStack> pricedItems = new ArrayList<PricedItemStack>();
            while (result.next())
            {
                ItemStack item = Util.convertItem(result.getString("item"));
                Material mat = item.getType();
                int amount = item.getAmount();
                short data = item.getDurability();
                double price = result.getDouble("price");
                int timessold = result.getInt("timessold");

                pricedItems.add(new PricedItemStack(mat, amount, data, price, timessold));
            }

            return pricedItems;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the PricedItems from the database!", e);
        }
    }

    public PricedItemStack getByKey(ItemStack key)
    {
        try
        {
            String skey = Util.convertItem(key);
            ResultSet result = this.db.query("SELECT `item`,`price`,`timessold` FROM {{PREFIX}}priceditem WHERE item=? LIMIT 1", skey);

            if (!result.next())
            {
                return null;
            }
            ItemStack item = Util.convertItem(result.getString("item"));
            Material mat = item.getType();
            int amount = item.getAmount();
            short data = item.getDurability();
            double price = result.getDouble("price");
            int timessold = result.getInt("timessold");

            return new PricedItemStack(mat, amount, data, price, timessold);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the PricedItem '" + key + "'!", e);
        }
    }

    
    public boolean store(PricedItemStack... object)
    {
        this.createStructure();
        try
        {
            for (PricedItemStack item : object)
            {
                String sItem = Util.convertItem(item);
                double price = item.getAvgPrice();
                int timessold = item.getTimesSold();

                this.db.query("INSERT INTO {{PREFIX}}bids (`item`, `price`,`timessold``)"+
                                    "VALUES (?, ?, ?)", sItem, price, timessold); 
            }
            return true;
        }
        catch (Exception e)
        {
            throw new StorageException("Failed to store the Prices !", e);
        }
    }

    public int delete(PricedItemStack... object)
    {
        return this.deleteByKey(object);
    }

    public int deleteByKey(ItemStack... keys)
    {
        int dels = 0;
        for (ItemStack item : keys)
        {
            String sItem = Util.convertItem(item);
            this.db.query("DELETE FROM {{PREFIX}}bids WHERE item=?", sItem);
            ++dels;
        }
        return dels;
    }
    
    public void createStructure()
    {
        this.db.exec(   "CREATE TABLE IF NOT EXISTS `subscription` ("+
                        "`id` int(11) NOT NULL AUTO_INCREMENT"+    
                        "`cubeuserid` int(11) NOT NULL,"+
                        "`sub` varchar(42) NOT NULL,"+
                        "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"+
                        ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
                    );
    }
}
