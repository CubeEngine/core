package de.cubeisland.cubeengine.auctions.auction;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class PricedItemStack extends ItemStack {

    private double price;
    private int timessold; 
    
    public PricedItemStack(Material mat, int amount, short data) 
    {
        super(mat,amount,data);
        this.price = 0;
        this.timessold = 0;
    }
    
    public PricedItemStack(Material mat, int amount, short data, double price, int timessold) 
    {
        super(mat,amount,data);
        this.price = price;
        this.timessold = timessold;
    }
    //TODO Price.java ablösen... PricedItemStack könnte man auch ähnlich für CubeMarket nutzen
    
    public void setPrice(double price, int timessold)
    {
        this.price = price;
        this.timessold = timessold;
    }
    
    public void resetPrice()
    {
        this.price = 0;
        this.timessold = 0;
    }
    
    public double getAvgPrice(ItemStack item)
    {
        if (this.timessold==0)
            return -1;
        return this.price;
    }
    
    public double adjustPrice(double price)
    {
        double t_price = this.price;
        int t_timessold =this.timessold;
        t_price *= t_timessold;
        t_price += price;
        t_price /= ++t_timessold;
        
        this.price = t_price;
        this.timessold = t_timessold;
        return this.price;
    }
}
