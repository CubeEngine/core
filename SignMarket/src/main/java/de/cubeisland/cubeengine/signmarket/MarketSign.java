package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketInfoModel;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class MarketSign
{
    private final Location location;
    private SignMarketInfoModel info;
    private SignMarketBlockModel blockInfo;

    public MarketSign(Location location)
    {
        this(location, null);
    }

    public MarketSign(Location location, User owner)
    {
        //TODO get info from database if it already exists
        this.location = location;
        this.info = new SignMarketInfoModel();
        if (owner != null)
            this.info.owner = owner.key;
    }

    /**
     * Saves all MarketSignData into the database
     *
     * @return false if the sign is incomplete and cannot be saved
     */
    public boolean saveToDatabase()
    {
        //TODO save/update/delete in database
        //delete on sign destroy
        //delete on invalid sign
        //save on new sign
        //update on changed sign
        return false;
    }

    /**
     * Sets the itemstack to buy/sell
     *
     * @param itemStack
     */
    public void setItemStack(ItemStack itemStack)
    {}

    /**
     * Changes this market-sign to be a BUY-sign
     */
    public void setBuy()
    {}

    /**
     * Changes this market-sign to be a SELL-sign
     */
    public void setSell()
    {}

    /**
     * Changes this market-sign to be a not finished EDIT-sign
     */
    public void setEdit()
    {}

    /**
     * Sets the owner of this market-sign to given user.
     * If the user is null this is equivalent too {@link #setAdminSign()}
     *
     * @param user
     */
    public void setOwner(User user)
    {}

    /**
     * Sets this market-sign to be an admin sign with infinite money or items
     */
    public void setAdminSign()
    {}

    /**
     * Sets the amount to buy/sell with each click
     *
     * @param amount
     */
    public void setAmount(int amount)
    {}

    /**
     * Sets the price to buy/sell the specified amount of items with each click
     *
     * @param price
     */
    public void setPrice(long price)
    {}

    /**
     * Adds items to the stock (only works with user-market-signs)
     *
     * @param amount
     */
    private void addToStock(int amount)//interaction update imediatly
    {}

    /**
     * Removes items from the stock (only works with user-market-signs)
     *
     * @param amount
     */
    private void takeFromStock(int amount)//interaction update imediatly
    {}

    private boolean executeAction(User user) //TODO return enum of possible results:
            // not enough money
            // sign disabled
            //etc..
            //OR string with error/success-message
    {
        return false;
    }
}


