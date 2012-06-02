package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.auctions.Util.Util;
import de.cubeisland.cubeengine.core.persistence.Model;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class ItemPrice implements Model<String>
{
    private final ItemStack item;
    private double price;
    private int timessold; 
    
    public ItemPrice(String item, double price, int timessold)
    {
        this.item = Util.convertItem(item);
        this.price = price;
        this.timessold = timessold;
    }
    
    public ItemPrice(ItemStack item, double price)
    {
        this.item = item.clone();
        this.item.setAmount(1);
        this.price = price / item.getAmount();
        this.timessold = 1;
    }
    
    public void setPrice(double price, int timessold)
    {
        this.price = price;
        this.timessold = timessold;
    }
    
    public double getPrice(int amount)
    {
        return this.price * amount;
    }
    
    public double adjustPrice(double price, int amount)
    {
        double t_price = this.price / amount;
        int t_timessold =this.timessold;
        t_price *= t_timessold;
        t_price += price;
        t_price /= ++t_timessold;
        
        this.price = t_price;
        this.timessold = t_timessold;
        return this.price;
    }
    
    public ItemStack getItem()
    {
        return this.item;
    }
    
    public String getKey()
    {
        return Util.convertItem(item);
    }

    public void setKey(String key)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
