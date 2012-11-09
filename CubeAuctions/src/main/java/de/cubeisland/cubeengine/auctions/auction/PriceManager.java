package de.cubeisland.cubeengine.auctions.auction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Anselm Brehme
 */
public class PriceManager
{
    private Map<ItemStack, ItemPrice> prices;
//TODO use the prices

    public PriceManager()
    {
        this.prices = new HashMap<ItemStack, ItemPrice>();
    }

    public double getAvgPrice(ItemStack item)
    {
        ItemStack key = item.clone();
        key.setAmount(1);
        ItemPrice value = this.prices.get(key);
        if (value == null)
        {
            return 0;
        }
        return value.getPrice(item.getAmount());
    }

    public double adjustPrice(ItemStack item, double price)
    {
        ItemStack key = item.clone();
        key.setAmount(1);
        ItemPrice value = this.prices.get(key);
        if (value == null)
        {
            value = new ItemPrice(item, price);
            this.prices.put(key, value);
            return value.getPrice(item.getAmount());
            //TODO save in db
        }
        return value.adjustPrice(price, item.getAmount());
        //TODO adjust in db
    }

    public void loadPrices(Collection<ItemPrice> itemPrices)
    {
        for (ItemPrice itemPrice : itemPrices)
        {
            this.prices.put(itemPrice.getItem(), itemPrice);
        }
    }
}
