package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.auctions.database.PriceStorage;
import de.cubeisland.cubeengine.core.persistence.Model;
import gnu.trove.map.hash.THashMap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class PricedItemStack extends ItemStack implements Model<Integer>
{
//TODO insert into DB / update
    private int key;
    private double price;
    private int timessold; 
    private static final THashMap<ItemStack,PricedItemStack> pricedItems = new THashMap<ItemStack,PricedItemStack>();
    private PriceStorage priceDB = new PriceStorage();
    
    public PricedItemStack(ItemStack item) 
    {
        super(item.getType(), 1,item.getDurability());
        this.price = 0;
        this.timessold = 0;
        PricedItemStack.pricedItems.put(new ItemStack(item.getType(),1,item.getDurability()),this);
        priceDB.store(this);
    }
    
    
    public PricedItemStack(Material mat, short data) 
    {
        super(mat,1,data);
        this.price = 0;
        this.timessold = 0;
        PricedItemStack.pricedItems.put(new ItemStack(mat,1,data),this);
        priceDB.store(this);
    }
    
    public PricedItemStack(Material mat, short data, double price, int timessold) 
    {
        super(mat,1,data);
        this.price = price;
        this.timessold = timessold;
        PricedItemStack.pricedItems.put(new ItemStack(mat,1,data),this);
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
    
    public double getAvgPrice()
    {
        if (this.timessold==0)
            return -1;
        return this.price;
    }
    
    public double getAvgPrice(ItemStack item)
    {
        ItemStack key = item.clone();
        key.setAmount(1);
        if (pricedItems.get(key) != null)
            return pricedItems.get(key).getAvgPrice();
        else
            return (new PricedItemStack(key)).getAvgPrice();
    }
    
    public double getPrice()
    {
        if (this.timessold==0)
            return -1;
        return this.price*this.getAmount();
    }
    
    public int getTimesSold()
    {
        return this.timessold;
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

    public Integer getKey()
    {
        return this.key;
    }
    
    public void setKey(Integer key)
    {
        this.key = key;
    }
}
