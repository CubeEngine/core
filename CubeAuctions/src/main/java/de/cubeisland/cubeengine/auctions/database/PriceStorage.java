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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class PriceStorage implements Storage<PricedItemStack>
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
                short data = item.getDurability();
                double price = result.getDouble("price");
                int timessold = result.getInt("timessold");

                pricedItems.add(new PricedItemStack(mat, data, price, timessold));
            }

            return pricedItems;
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the PricedItems from the database!", e);
        }
    }

    public PricedItemStack getByItem(PricedItemStack key)
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
            short data = item.getDurability();
            double price = result.getDouble("price");
            int timessold = result.getInt("timessold");

            return new PricedItemStack(mat, data, price, timessold);
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to load the PricedItem '" + key + "'!", e);
        }
    }




    


    public void initialize()
    {
        try
        {
            this.db.exec(   "CREATE TABLE IF NOT EXISTS `subscription` ("+
                            "`id` int(11) NOT NULL AUTO_INCREMENT,"+    
                            "`cubeuserid` int(11) NOT NULL,"+
                            "`sub` varchar(42) NOT NULL,"+
                            "FOREIGN KEY (`cubeuserid`) REFERENCES bidder(id)"+
                            ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;"
                        );
        }
        catch (SQLException ex)
        {
            Logger.getLogger(PriceStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    

    public void store(PricedItemStack model)
    {
        this.initialize();
        try
        {
            String sItem = Util.convertItem(model);
            double price = model.getAvgPrice();
            int timessold = model.getTimesSold();

            this.db.exec("INSERT INTO {{PREFIX}}bids (`item`, `price`,`timessold``)"+
                                "VALUES (?, ?, ?)", sItem, price, timessold); 
        }
        catch (Exception ex)
        {
            throw new StorageException("Failed to store the Price !", ex);
        }
    }

    public boolean delete(PricedItemStack model)
    {
        String sItem = Util.convertItem(model);
        try
        {
            this.db.exec("DELETE FROM {{PREFIX}}bids WHERE item=?", sItem);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Price !", ex);
        }
        return true;
    }

    public boolean delete(int id)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void update(PricedItemStack model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(PricedItemStack model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public PricedItemStack get(int key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
