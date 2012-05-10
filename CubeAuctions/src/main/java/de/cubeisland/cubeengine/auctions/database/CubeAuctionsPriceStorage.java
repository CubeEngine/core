/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cubeisland.cubeengine.auctions.database;

import de.cubeisland.cubeengine.auctions.auction.PricedItemStack;
import de.cubeisland.cubeengine.core.persistence.Database;
import de.cubeisland.cubeengine.core.persistence.Storage;
import java.util.Collection;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class CubeAuctionsPriceStorage implements Storage<ItemStack, PricedItemStack>
{

    public CubeAuctionsPriceStorage() 
    {
    
    }

    public Database getDatabase()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<PricedItemStack> getAll()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PricedItemStack getByKey(ItemStack key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean store(PricedItemStack... object)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int delete(PricedItemStack... object)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int deleteByKey(ItemStack... keys)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
