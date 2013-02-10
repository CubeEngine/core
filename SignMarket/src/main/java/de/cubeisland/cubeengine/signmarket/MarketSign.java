package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.InventoryUtil;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketInfoModel;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class MarketSign implements InventoryHolder
{
    private final Signmarket module;
    private final Location location;
    private SignMarketInfoModel info;
    private SignMarketBlockModel blockInfo;

    private Inventory inventory = null;

    public MarketSign(Signmarket module, Location location)
    {
        this(module, location, null);
    }

    public MarketSign(Signmarket module, Location location, User owner)
    {
        //TODO get info from database if it already exists
        this.location = location;
        this.info = new SignMarketInfoModel();
        if (owner != null)
            this.info.owner = owner.key;
        this.module = module;
    }

    /**
     * Saves all MarketSignData into the database
     *
     * @return false if the sign is incomplete and cannot be saved
     */
    public boolean saveToDatabase()
    {
        //TODO update from current inventory if given
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

    /**
     * Tries to execute the appropriate action
     * <p>on right-click: use the sign (buy/sell) / if owner take out of stock
     * <p>on left-click: BUY-sign: if correct item in hand & owner of sign -> refill stock
     * <p>on shift left-click: open sign-inventory OR if correct item in hand & owner put all in stock
     * <p>on shift right-click: inspect the sign, shows all information saved
     *
     * @param user
     * @return
     */
    public boolean executeAction(User user, Action type) //TODO return enum of possible results:
    {
        boolean sneaking = user.isSneaking();
        ItemStack itemInHand = user.getItemInHand();
        if (itemInHand == null)
        {
            itemInHand = new ItemStack(Material.AIR);
        }
        switch (type)
        {
            case LEFT_CLICK_BLOCK:
                if (sneaking)
                {
                    if (MarketSignPerm.SIGN_INVENTORY_SHOW.isAuthorized(user))
                    {
                        if (this.isOwner(user)) // owner OR can access other
                        {
                            this.module.getInventoryListener().openInventoryCanEdit(user, this);
                            //TODO open sign inventory can edit inventory
                        }
                        else if (MarketSignPerm.SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user))
                        {
                            this.module.getInventoryListener().openInventoryCanEdit(user, this);
                            //TODO open sign inventory can edit inventory
                        }
                        else
                        {
                            this.module.getInventoryListener().openInventoryCannotEdit(user, this);
                            //TODO open sign inventory cannot edit inventory
                        }
                    }
                    else
                    {
                        user.sendMessage("signmarket","&cYou are not allowed to see the market-signs inventory");
                    }
                }
                else // no sneak -> empty & break signs
                {
                    if (user.getGameMode().equals(GameMode.CREATIVE)) // instabreak items
                    {
                        //TODO if amount == 1337 explosion easteregg
                        //TODO let drop all items ? in config
                        if (this.isOwner(user))
                        {
                            if (MarketSignPerm.SIGN_DESTROY_OWN.isAuthorized(user))
                            {
                                //allow
                            }
                            else
                            {
                                user.sendMessage("signmarket","&cYou are not allowed to break your own market-signs!");
                                //deny
                            }
                        }
                        else if (this.info.isAdminSign())
                        {
                            if (MarketSignPerm.SIGN_DESTROY_ADMIN.isAuthorized(user))
                            {
                                // allow
                            }
                            else
                            {
                                user.sendMessage("signmarket","&cYou are not allowed to break admin-market-signs!");
                               // deny
                            }
                        }
                        else
                        {
                            if (MarketSignPerm.SIGN_DESTROY_OTHER.isAuthorized(user))
                            {
                                // allow
                            }
                            else
                            {
                                user.sendMessage("signmarket","&cYou are not allowed to break others market-signs!");
                                //deny
                            }
                        }
                    }
                    else // first empty items then break
                    {
                        if (this.info.isAdminSign())
                        {
                            if (MarketSignPerm.SIGN_DESTROY_ADMIN.isAuthorized(user))
                            {
                                //allow destroying
                            }
                            else
                            {
                                user.sendMessage("signmarket","&cYou are not allowed to break admin-signs!");
                                //deny
                            }
                        }
                        else if (this.isOwner(user))
                        {
                            if (this.info.amount > 0)
                            {
                                //take
                            }
                            else if (this.info.amount == 0)
                            {
                                if (MarketSignPerm.SIGN_DESTROY_OWN.isAuthorized(user))
                                {
                                    // allow break
                                }
                                else
                                {
                                    user.sendMessage("signmarket","&cYou are not allowed to break your own market-signs!");
                                    //deny
                                }
                            }
                        }
                        else // not owner / not admin
                        {
                            if (this.info.amount > 0)
                            {
                                if (MarketSignPerm.SIGN_INVENTORY_TAKE_OTHER.isAuthorized(user))
                                {
                                    //take
                                }
                                else
                                {
                                    user.sendMessage("signmarket","&cYou are not allowed to destroy others market-signs!");
                                    //deny
                                }
                            }
                            else if (this.info.amount == 0)
                            {
                                if (MarketSignPerm.SIGN_DESTROY_OTHER.isAuthorized(user))
                                {
                                    //allow break
                                }
                                else
                                {
                                    user.sendMessage("signmarket","&cYou are not allowed to destroy others market-signs!");
                                    //deny
                                }
                            }
                        }
                    }
                }
                return true;
            case RIGHT_CLICK_BLOCK:
                if (sneaking)
                {
                    if (this.isOwner(user))
                    {
                        if (this.info.isItem(itemInHand))
                        {
                            int amount = InventoryUtil.getAmountOf(user.getInventory(),itemInHand);
                            this.info.amount = this.info.amount + amount;
                            user.getInventory().removeItem(itemInHand); // this should remove all items of this type
                            user.sendMessage("signmarket","&aAdded all (&6%d&a) &6%s &ato the stock!",amount, Match.material().getNameFor(this.info.getItem()));
                            user.updateInventory();
                            this.updateSign();
                            this.saveToDatabase();
                            return true;
                        }
                    }
                    //TODO show all info
                }
                else
                {
                    if (this.isOwner(user))
                    {
                        if (this.info.isItem(itemInHand))
                        {
                            int amount = itemInHand.getAmount();
                            this.info.amount = this.info.amount + amount;
                            user.setItemInHand(new ItemStack(Material.AIR));
                            user.sendMessage("signmarket","&aAdded &6%d&ax &6%s &ato the stock!",amount, Match.material().getNameFor(this.info.getItem()));
                            user.updateInventory();
                            this.updateSign();
                            this.saveToDatabase();
                        }
                        else
                        {
                            user.sendMessage("signmarket","&cWrong item to put into the market-sign!");
                        }
                        return true; // prevent placing etc
                    }
                    return this.useSign(user);
                }
        }
        return false;
    }

    private boolean useSign(User user)
    {
        return false; //TODO sell / buy
    }

    public boolean isOwner(User user)
    {
        return false;
    }

    private void updateSign()
    {

    }

    public ItemStack getItem() {
        return this.info.getItem();
    }

    public boolean isAdminSign() {
        return this.info.isAdminSign();
    }

    @Override
    public Inventory getInventory() {

        if (this.inventory == null)
        {
            if (this.isAdminSign())
            {
                this.inventory = Bukkit.getServer().createInventory(this, 9, "Market-Sign"); // Dispenser would be nice BUT cannot rename
            }
            else
            {
                this.inventory = Bukkit.getServer().createInventory(this, 54, "Market-Sign"); // DOUBLE-CHEST
            }
        }
        return this.inventory;
    }

    public void resetInventory()
    {
        this.inventory = null;
    }

    public int getAmount()
    {
        return this.info.amount;
    }

    public void setInfoModel(SignMarketInfoModel infoModel) {
        this.info = infoModel;
    }

    public void setBlockModel(SignMarketBlockModel blockModel) {
        this.blockInfo = blockModel;
    }
}


