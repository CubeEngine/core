package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.PricedItemStack;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import de.cubeisland.cubeengine.core.persistence.StorageException;
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
public class PriceStorage implements Storage<PricedItemStack>
{
    private final Database database = CubeAuctions.getDB();
    private final String TABLE = "priceditem";

    public PriceStorage()
    {
        try
        {
            this.database.prepareStatement("price_getall", "SELECT id,item,price,timessold FROM {{" + TABLE + "}}");
            this.database.prepareStatement("price_get", "SELECT id,item,price,timessold FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("price_store", "INSERT INTO {{" + TABLE + "}} (item,price,timessold) VALUES (?,?,?)");
            this.database.prepareStatement("price_delete", "DELETE FROM {{" + TABLE + "}} WHERE id=?");
            this.database.prepareStatement("price_clear", "DELETE FROM {{" + TABLE + "}}");
            this.database.prepareStatement("price_update", "UPDATE {{" + TABLE + "}} SET price=? timesold=? WHERE id=?");
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

    public Collection<PricedItemStack> getAll()
    {
        try
        {
            ResultSet result = this.database.preparedQuery("price_getall");

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

    public void initialize()
    {
        try
        {
            this.database.exec("CREATE TABLE IF NOT EXISTS `priceditem` ("
                + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                + "`item` varchar(42) NOT NULL,"
                + "`price` double(11)) NOT NULL,"
                + "`timessold` int(11) NOT NULL,"
                + "PRIMARY KEY (`id`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to initialize the PricedItems-Table!", ex);
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

            this.database.preparedExec("price_store", sItem, price, timessold);
        }
        catch (Exception ex)
        {
            throw new StorageException("Failed to store the Price !", ex);
        }
    }

    public boolean delete(PricedItemStack model)
    {
        return this.delete(model.getId());
    }

    public boolean delete(int id)
    {
        try
        {
            return this.database.preparedExec("price_delete", id);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Failed to delete the Price !", ex);
        }
    }

    public void clear()
    {
        try
        {
            this.database.preparedExec("price_clear");
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to clear the database!", e);
        }
    }

    public void update(PricedItemStack model)
    {
        try
        {
            this.database.preparedExec("price_update", model.getAvgPrice(), model.getTimesSold(), model.getId());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to update the Price!", e);
        }
    }

    public PricedItemStack get(int key)
    {
        try
        {
            ResultSet result = this.database.preparedQuery("price_get", key);

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

    public void merge(PricedItemStack model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
