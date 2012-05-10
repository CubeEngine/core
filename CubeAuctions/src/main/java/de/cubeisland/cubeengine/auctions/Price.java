package de.cubeisland.cubeengine.auctions;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class Price {
    
    private Map<ItemStack, Double> price = new HashMap<ItemStack, Double>();
    private Map<ItemStack, Integer> amount = new HashMap<ItemStack, Integer>();
    
    public Price(){}
    
/**
 * @return average Price of item
 */
    public double getPrice(ItemStack item)
    {
        if (this.price.get(item)==null)
            return 0;
        return this.price.get(item);
    }
    
/**
 * adjust average Price for item
 */
    public double adjustPrice(ItemStack item, double price)
    {
        double t_price;
        int t_amount;
        if (this.price.get(item)==null)
        {
            t_price = price;
            t_amount = 1;
        }
        else
        {
            t_price = this.price.get(item);
            t_amount = this.amount.get(item);
            t_price *= t_amount;
            t_price += price;
            t_price /= ++t_amount;
        }
        String item2 = Util.convertItem(item);
        CubeAuctions.getInstance().getDB().execUpdate(
            "DELETE FROM `price` WHERE `item`=?", item2);
        CubeAuctions.getInstance().getDB().execUpdate(
            "INSERT INTO `price` (`item` ,`price` ,`amount` ) VALUES ( ?, ? ,?);", item2, t_price, t_amount);
        return this.setPrice(item, t_price, t_amount);
    }
    
/**
 * set average Price for item
 */
    public double setPrice(ItemStack item, double price, int amount)
    {
        this.price.remove(item);
        this.price.put(item, price);
        this.amount.remove(item);
        this.amount.put(item, amount);
        return this.price.get(item); 
    }
    
/**
 * remove average Price for item
 */
    public void resetPrice(ItemStack item)
    {
        this.price.remove(item);
    }
}
