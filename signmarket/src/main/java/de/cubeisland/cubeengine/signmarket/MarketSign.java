/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.signmarket;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.service.Economy;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.InventoryGuardFactory;
import de.cubeisland.cubeengine.core.util.RomanNumbers;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketItemModel;

import gnu.trove.map.hash.TLongLongHashMap;

import static de.cubeisland.cubeengine.core.util.InventoryUtil.*;

public class MarketSign
{
    private final MarketSignFactory msFactory;
    private final Signmarket module;
    protected Economy economy;
    private SignMarketItemModel itemInfo;
    private SignMarketBlockModel blockInfo;

    private TLongLongHashMap breakingSign = new TLongLongHashMap();

    private boolean editMode;
    public boolean syncOnMe = false;

    public MarketSign(Signmarket module, Location location)
    {
        this(module, location, null);
    }

    public MarketSign(Signmarket module, Location location, User owner)
    {
        this(module, new SignMarketItemModel(), new SignMarketBlockModel(location));
        this.blockInfo.setOwner(owner);
    }

    public MarketSign(Signmarket module, SignMarketItemModel itemModel, SignMarketBlockModel blockModel)
    {
        this.module = module;
        this.economy = module.getCore().getServiceManager().getServiceProvider(Economy.class);
        this.blockInfo = blockModel;
        this.setItemInfo(itemModel);
        this.msFactory = module.getMarketSignFactory();
    }

    /**
     * Saves all MarketSignData into the database if the sign is valid
     */
    public void saveToDatabase()
    {
        if (this.isValidSign(null))
        {
            msFactory.syncAndSaveSign(this);
        }
        this.updateSignText();
    }

    public void breakSign()
    {
        this.dropContents();
        msFactory.delete(this);
    }

    public void dropContents()
    {
        if (this.isAdminSign() || !this.itemInfo.hasStock() || this.editMode || this.itemInfo.stock <= 0 || this.itemInfo.sharesStock()) // no stock / edit mode / shared stock
        {
            return;
        }
        ItemStack item = this.itemInfo.getItem().clone();
        item.setAmount(this.itemInfo.stock);
        this.itemInfo.stock = 0; // just to be sure no items are duped
        if (this.module.getConfig().allowOverStackedOutOfSign)
        {
            this.getLocation().getWorld().dropItemNaturally( this.getLocation(), item);
            return;
        }
        for (ItemStack itemStack : splitIntoMaxItems(item, item.getMaxStackSize()))
        {
            this.getLocation().getWorld().dropItemNaturally(this.getLocation(), itemStack);
        }
    }

    /**
     * Sets the itemstack to buy/sell
     *
     * @param itemStack
     */
    public void setItemStack(ItemStack itemStack, boolean setAmount)
    {
        this.itemInfo.setItem(itemStack);
        if (setAmount)
        {
            this.blockInfo.amount = itemStack.getAmount();
        }
    }

    /**
     * Changes this market-sign to be a BUY-sign
     */
    public void setBuy()
    {
        this.blockInfo.signType = true;
        this.blockInfo.demand = null;
    }

    /**
     * Changes this market-sign to be a SELL-sign
     */
    public void setSell()
    {
        this.blockInfo.signType = false;
    }

    /**
     * Sets the owner of this market-sign to given user.
     * <p>Sets stock to 0 if null before
     *
     * @param user
     */
    public void setOwner(User user)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("Use setAdminSign() instead!");
        }
        this.blockInfo.setOwner(user);
        if (this.getStock() == null)
        {
            this.setStock(0);
        }
    }

    /**
     * Sets this market-sign to be an admin sign
     * <p>owner = null
     * <p>demand = null
     */
    public void setAdminSign()
    {
       this.blockInfo.setOwner(null);
    }

    /**
     * Sets the amount to buy/sell with each click
     *
     * @param amount
     */
    public void setAmount(int amount)
    {
        if (amount < 0)
            throw new IllegalArgumentException("The amount has to be greater than 0!");
        this.blockInfo.amount = amount;
    }

    /**
     * Sets the price to buy/sell the specified amount of items with each click
     *
     * @param price
     */
    public void setPrice(long price)
    {
        this.blockInfo.price = price;
    }
    private int inventoryStock;
    private Inventory displayInventory;

    public boolean openInventory(User user)
    {
        if (this.isOwner(user) || (!this.isAdminSign() && MarketSignPerm.SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user)))
        {
            if (this.itemInfo.inventory == null || this.getInventory().getViewers().isEmpty())
            {
                this.itemInfo.inventory = null;
                this.inventoryStock = getAmountOf(this.getInventory(),this.getItem());
            }
            final Inventory inventory = this.getInventory();
            Runnable onClose = new Runnable() {
                @Override
                public void run()
                {
                    if (!MarketSign.this.isAdminSign())
                    {
                        int newStock = getAmountOf(inventory, MarketSign.this.itemInfo.getItem());
                        if (newStock != MarketSign.this.inventoryStock)
                        {
                            MarketSign.this.setStock(MarketSign.this.getStock() - MarketSign.this.inventoryStock + newStock);
                            MarketSign.this.inventoryStock = newStock;
                        }
                    }
                    MarketSign.this.saveToDatabase();
                }
            };
            Runnable onChange = new Runnable() {
                @Override
                public void run()
                {
                    if (!MarketSign.this.isAdminSign())
                    {
                        int newStock = getAmountOf(inventory, MarketSign.this.itemInfo.getItem());
                        if (newStock != MarketSign.this.inventoryStock)
                        {
                            MarketSign.this.setStock(MarketSign.this.getStock() - MarketSign.this.inventoryStock + newStock);
                            MarketSign.this.inventoryStock = newStock;
                            MarketSign.this.updateSignText();
                        }
                    }
                }
            };
            InventoryGuardFactory guard = InventoryGuardFactory.prepareInventory(inventory, user)
                    .blockPutInAll().blockTakeOutAll()
                    .onClose(onClose).onChange(onChange);
            ItemStack itemInSign = this.itemInfo.getItem();
            if (this.isBuySign())
            {
                guard.notBlockPutIn(itemInSign).notBlockTakeOut(itemInSign);
            }
            else
            {
                guard.notBlockTakeOut(itemInSign);
            }
            guard.submitInventory(this.module, true);
            return true;
        }
        if (MarketSignPerm.SIGN_INVENTORY_SHOW.isAuthorized(user))
        {
            if (this.displayInventory == null)
            {
                this.displayInventory = Bukkit.createInventory(null,InventoryType.DISPENSER);
                this.displayInventory.setItem(4,this.getItem());
            }
            InventoryGuardFactory.prepareInventory(this.displayInventory, user)
                    .blockPutInAll().blockTakeOutAll().submitInventory(this.module, true);
            return true;
        }
        return false;
    }

    /**
     * Tries to execute the appropriate action
     * <p>on right-click: use the sign (buy/sell) / if owner take out of stock
     * <p>on left-click: BUY-sign: if correct item in hand & owner of sign -> refill stock
     * <p>on shift left-click: open sign-inventory OR if correct item in hand & owner put all in stock
     * <p>on shift right-click: inspect the sign, shows all information saved
     *
     * @param user
     * @return true if the event shall be canceled
     */
    public void executeAction(User user, Action type)
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
                if (this.editMode)
                {
                    user.sendTranslated("&cThis sign is being edited right now!");
                    return;
                }
                if (sneaking)
                {
                    if (!this.isAdminSign() && (this.isOwner(user) || MarketSignPerm.SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user)))
                    {
                        if (this.isBuySign() && this.itemInfo.matchesItem(itemInHand))
                        {
                            if (!this.getInventory().getViewers().isEmpty())
                            {
                                user.sendTranslated("&cThis signs inventory is being edited right now!");
                                return;
                            }
                            int amount = this.putItems(user, true);
                            if (amount != 0)
                                user.sendTranslated("&aAdded all (&6%d&a) &6%s &ato the stock!", amount, Match.material().getNameFor(this.itemInfo.getItem()));
                            return;
                        }
                    }
                    if (!this.openInventory(user))
                    {
                        user.sendTranslated("&cYou are not allowed to see the market-signs inventories");
                    }
                    return;
                }
                else
                // no sneak -> empty & break signs
                {
                    if (!this.getInventory().getViewers().isEmpty())
                    {
                        user.sendTranslated("&cThis signs inventory is being edited right now!");
                        return;
                    }
                    if (this.isValidSign(null))
                    {
                        if (this.isOwner(user) || MarketSignPerm.SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user))
                        {
                            if (!this.editMode && this.blockInfo.isBuyOrSell() && this.isBuySign() && this.hasStock() && this.itemInfo.matchesItem(itemInHand))
                            {
                                if (!this.getInventory().getViewers().isEmpty())
                                {
                                    user.sendTranslated("&cThis signs inventory is being edited right now!");
                                    return;
                                }
                                int amount = this.putItems(user, false);
                                if (amount != 0)
                                    user.sendTranslated("&aAdded &6%d&ax &6%s &ato the stock!", amount, Match.material().getNameFor(this.itemInfo.getItem()));
                                return;
                            }
                            else if (itemInHand != null && itemInHand.getTypeId() != 0)
                            {
                                user.sendTranslated("&cUse bare hands to break the sign!");
                                return;
                            }
                        }
                    }
                    if (user.getGameMode().equals(GameMode.CREATIVE)) // instabreak items
                    {
                        if (this.isOwner(user))
                        {
                            if (MarketSignPerm.SIGN_DESTROY_OWN.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated("&cYou are not allowed to break your own market-signs!");
                            }
                        }
                        else if (this.isAdminSign())
                        {
                            if (MarketSignPerm.SIGN_DESTROY_ADMIN.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated("&cYou are not allowed to break admin-market-signs!");
                            }
                        }
                        else
                        {
                            if (MarketSignPerm.SIGN_DESTROY_OTHER.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated("&cYou are not allowed to break others market-signs!");
                            }
                        }
                    }
                    else
                    // first empty items then break
                    {
                        if (this.isAdminSign())
                        {
                            if (MarketSignPerm.SIGN_DESTROY_ADMIN.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated("&cYou are not allowed to break admin-signs!");
                            }
                        }
                        else if (this.isOwner(user))
                        {
                            if (MarketSignPerm.SIGN_DESTROY_OWN.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated("&cYou are not allowed to break your own market-signs!");
                            }
                        }
                        else
                        // not owner / not admin
                        {
                            if (MarketSignPerm.SIGN_DESTROY_OTHER.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated("&cYou are not allowed to destroy others market-signs!");
                            }
                        }
                    }
                }
                return;
            case RIGHT_CLICK_BLOCK:
                if (sneaking)
                {
                    this.showInfo(user);
                    return;
                }
                else
                {
                    if (this.editMode)
                    {
                        user.sendTranslated("&cThis sign is being edited right now!");
                        return;
                    }
                    if (!this.getInventory().getViewers().isEmpty())
                    {
                        user.sendTranslated("&cThis signs inventory is being edited right now!");
                        return;
                    }
                    if (this.isOwner(user))
                    {
                        this.takeItems(user);
                        return;
                    }
                    this.useSign(user);
                }
        }
        return;
    }

    public void showInfo(User user)
    {
        if (this.editMode)
        {
            user.sendTranslated("\n-- &5Sign Market - Edit Mode &f--");
        }
        else
        {
            user.sendTranslated("\n--------- &6Sign Market &f---------");
        }
        if (!this.blockInfo.isBuyOrSell())
        {
            user.sendMessage("&5new Sign");
            return;
        }
        if (this.isBuySign())
        {
            if (this.isAdminSign())
            {
                user.sendTranslated("&3Buy: &6%d &ffor &6%s &ffrom &6%s", this.getAmount(), this.parsePrice(), "Server");
            }
            else
            {
                user.sendTranslated("&3Buy: &6%d &ffor &6%s &ffrom &2%s", this.getAmount(), this.parsePrice(),
                                    this.blockInfo.getOwner().getName());
            }
        }
        else
        {
            if (this.isAdminSign())
            {
                user.sendTranslated("&3Sell: &6%d &ffor &6%s &fto &6%s", this.getAmount(), this.parsePrice(), "Server");
            }
            else
            {
                user.sendTranslated("&3Sell: &6%d &ffor &6%s &fto &2%s", this.getAmount(), this.parsePrice(),
                                    this.blockInfo.getOwner().getName());
            }
        }
        if (this.getItem() == null)
        {
            if (this.isInEditMode())
            {
                user.sendTranslated("&5No Item");
            }
            else
            {
                user.sendTranslated("&4No Item");
            }
        }
        else if (this.itemInfo.getItem().getItemMeta().hasDisplayName() || this.getItem().getItemMeta().hasLore() || !this.getItem().getEnchantments().isEmpty())
        {
            if (this.getItem().getItemMeta().hasDisplayName())
            {
                user.sendMessage("&e" + Match.material().getNameFor(this.getItem()) + " &f(&6" + this.getItem().getItemMeta().getDisplayName() + "&f)");
                if (this.getItem().getItemMeta().hasLore())
                {
                    for (String loreLine : this.getItem().getItemMeta().getLore())
                    {
                        user.sendMessage(" &e-&f " + loreLine);
                    }
                }
                if (!this.getItem().getEnchantments().isEmpty())
                {
                    user.sendTranslated("&6Enchantments:");
                }
                for (Map.Entry<Enchantment, Integer> entry : this.getItem().getEnchantments().entrySet())
                {
                    user.sendMessage(" &e-&6 " + Match.enchant().nameFor(entry.getKey()) + " &e" + RomanNumbers.intToRoman(entry.getValue()));
                }
            }
            else
            {
                user.sendMessage("&e" + Match.material().getNameFor(this.getItem()));
            }
        }
        else
        {
            user.sendMessage("&6" + Match.material().getNameFor(this.getItem()));
        }
        if (this.hasStock())
        {
            if (!this.hasDemand() && this.hasInfiniteSize())
            {
                user.sendTranslated("&3In stock: &6%d&f/&6Infinite", this.itemInfo.stock);
            }
            else if (this.getItem() == null || this.getAmount() == 0)
            {
                user.sendTranslated("&3In stock: &6%d&f/&cUnkown", this.itemInfo.stock);
            }
            else
            {
                user.sendTranslated("&3In stock: &6%d&f/&6%d", this.itemInfo.stock, this.getMaxItemAmount());
            }
        }
    }

    public boolean hasStock() {
        return this.itemInfo.hasStock();
    }

    public boolean hasDemand()
    {
        return this.getDemand() != null;
    }

    private String parsePrice()
    {
        if (this.blockInfo.price == 0)
        {
            if (this.isInEditMode())
            {
                return "&5No Price";
            }
            else
            {
                return "&4No Price";
            }
        }
        if (this.allowBuyIfEmpty())
        {
            return "&o"+this.economy.format(this.getPrice());
        }
        return this.economy.format(this.getPrice());
    }

    @SuppressWarnings("deprecation")
    private int putItems(User user, boolean all)
    {
        int amount;
        if (all)
        {
            amount = getAmountOf(user.getInventory(), user.getItemInHand());
        }
        else
        {
            amount = user.getItemInHand().getAmount();
        }
        if (this.getMaxItemAmount() != -1)
        {
            if (this.getStock() + amount > this.getMaxItemAmount())
            {
                amount = this.getMaxItemAmount() - this.getStock();
                if (amount <= 0)
                {
                    user.sendMessage("&cThe market-sign inventory is full!");
                    return 0;
                }
                user.sendMessage("&cThe market-sign cannot hold all your items!");
            }
        }
        this.setStock(this.getStock() + amount);
        ItemStack item = this.getItem().clone();
        item.setAmount(amount);
        user.getInventory().removeItem(item);
        user.updateInventory();
        this.saveToDatabase();
        return amount;
    }

    private Map<Integer, ItemStack> addToInventory(Inventory inventory, ItemStack item)
    {
        if (this.module.getConfig().allowOverStackedInSign)
        {
            return inventory.addItem(splitIntoMaxItems(item, 64));
        }
        else
        {
            return inventory.addItem(splitIntoMaxItems(item, item.getMaxStackSize()));
        }
    }

    private Map<Integer, ItemStack> addToUserInventory(User user, ItemStack item)
    {
        if (this.module.getConfig().allowOverStackedOutOfSign)
        {
            return user.getInventory().addItem(splitIntoMaxItems(item, 64));
        }
        else
        {
            return user.getInventory().addItem(splitIntoMaxItems(item, item.getMaxStackSize()));
        }
    }

    @SuppressWarnings("deprecation")
    private void takeItems(User user)
    {
        int amountToTake = this.getAmount();
        if (this.getStock() < amountToTake)
        {
            amountToTake = this.getStock();
        }
        if (amountToTake <= 0)
        {
            user.sendTranslated("&cThere are no more items stored in the sign!");
            return;
        }
        ItemStack item = this.getItem().clone();
        item.setAmount(amountToTake);
        Map<Integer, ItemStack> additional = this.addToUserInventory(user, item);
        int amountGivenBack = 0;
        for (ItemStack itemStack : additional.values())
        {
            amountGivenBack += itemStack.getAmount();
        }
        this.setStock(this.getStock() - amountToTake + amountGivenBack);
        if (amountGivenBack != 0 && (amountGivenBack == this.getAmount() || amountGivenBack == this.getStock()))
        {
            user.sendMessage("&cYour inventory is full!");
        }
        user.updateInventory();
        this.saveToDatabase();
    }

    public boolean tryBreak(User user)
    {
        if (this.breakingSign.containsKey(user.key) && System.currentTimeMillis() - this.breakingSign.get(user.key) <= 500)//0.5 sec
        {
            Location location = this.getLocation();
            if (this.getStock() != null && this.getStock() == 1337) //pssst i am not here
            {
                location.getWorld().strikeLightningEffect(location);
            }
            this.breakSign();
            location.getWorld().getBlockAt(location).breakNaturally();
            this.breakingSign.remove(user.key);
            user.sendTranslated("&aMarketSign destroyed!");
            return true;
        }
        this.breakingSign.put(user.key, System.currentTimeMillis());
        user.sendTranslated("&eDoubleclick to break the sign!");
        return false;
    }

    public boolean isFull()
    {
        if (!this.hasInfiniteSize() && this.hasStock())
        {
            if (this.getMaxItemAmount() >= this.getStock()+ this.getAmount())
            {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isSatisfied()
    {
        if (this.hasDemand())
        {
            return this.getStock() >= this.getDemand();
        }
        return false;
    }

    public Integer getMaxItemAmount()
    {
        if (this.hasDemand())
        {
            return this.getDemand();
        }
        if (this.hasInfiniteSize())
        {
            return -1;
        }
        Integer maxAmount;
        int maxSizeInStacks = this.itemInfo.size * 9;
        if (this.module.getConfig().allowOverStackedInSign)
        {
            maxAmount = maxSizeInStacks * 64;
        }
        else
        {
            maxAmount = maxSizeInStacks*this.getItem().getMaxStackSize();
        }
        return maxAmount;
    }

    public boolean hasInfiniteSize() {
        return this.itemInfo.size == -1;
    }

    @SuppressWarnings("deprecation")
    private void useSign(User user)
    {
        if (this.isValidSign(user))
        {
            if (this.isBuySign())
            {
                if (this.isSoldOut())
                {
                    if (!this.allowBuyIfEmpty())
                    {
                        user.sendTranslated("&cThis market-sign is &4&lSold Out&c!");
                        return;
                    }
                }
                if (!this.canAfford(user))
                {
                    user.sendTranslated("&cYou cannot afford the price of these items!");
                    return;
                }
                //Account userAccount = this.economy.getUserAccount(user, true);
                //Account ownerAccount = this.getOwner() != null ? this.economy.getUserAccount(this.getOwner(), true) : null;
                ItemStack item = this.getItem().clone();
                item.setAmount(this.getAmount());
                if (checkForPlace(user.getInventory(), item.clone()))
                {
                    String price = this.parsePrice();
                    this.economy.withdraw(user.getName(), this.getPrice());
                    if (this.getOwner() != null)
                    {
                        this.economy.deposit(this.getOwner().getName(), this.getPrice());
                    }
                    if (this.hasStock())
                    {
                        this.setStock(this.getStock() - this.getAmount());
                        if (this.getStock() < 0)
                        {
                            this.setStock(0);
                        }
                        this.saveToDatabase();
                    }
                    user.getInventory().addItem(item);
                    user.updateInventory();
                    user.sendTranslated("&aYou bought &6%dx %s &afor &6%s&a.", this.getAmount(), Match.material().getNameFor(this.getItem()), price);
                    return;
                }
                user.sendTranslated("&cYou do not have enough space for these items!");
                return;
            } // else Sell
            if (this.isSatisfied())
            {
                user.sendTranslated("&cThis market-sign is &4&lsatisfied&c! You can no longer sell items to it.");
                return;
            }
            if (this.isFull())
            {
                user.sendTranslated("&cThis market-sign is &4&lfull&c! You can no longer sell items to it.");
                return;
            }
            if (!this.canAfford(this.getOwner()))
            {
                user.sendTranslated("&cThe owner cannot afford the money to aquire your items!");
                return;
            }
            if (getAmountOf(user.getInventory(), this.getItem()) < this.getAmount())
            {
                user.sendTranslated("&cYou do not have enough items to sell!");
                return;
            }
            ItemStack item = this.getItem().clone();
            item.setAmount(this.getAmount());

            this.economy.deposit(user.getName(), this.getPrice());
            if (this.getOwner() != null)
            {
                this.economy.withdraw(this.getOwner().getName(), this.getPrice());
            }
            user.getInventory().removeItem(item);
            if (this.hasStock())
            {
                this.setStock(this.getStock() + this.getAmount());
                this.saveToDatabase();
            } // else admin sign -> no change
            user.updateInventory();
            user.sendTranslated("&aYou sold &6%dx %s &afor &6%s&a.", this.getAmount(), Match.material().getNameFor(this.getItem()), this.parsePrice());
        }
    }

    private boolean allowBuyIfEmpty()
    {
        return this.isSoldOut() && this.isAdminSign() && this.module.getConfig().allowBuyIfAdminSignIsEmpty;
    }

    public User getOwner() {
        return this.blockInfo.getOwner();
    }

    public boolean isValidSign(User user)
    {
        boolean result = true;
        if (!this.blockInfo.isBuyOrSell())
        {
            if (user != null)
                user.sendTranslated("&cNo sign-type given!");
            result = false;
        }
        if (this.blockInfo.amount <= 0)
        {
            if (user != null)
                user.sendTranslated("&cInvalid amount!");
            result = false;
        }
        if (this.blockInfo.price <= 0)
        {
            if (user != null)
                user.sendTranslated("&cInvalid price!");
            result = false;
        }
        if (this.itemInfo.getItem() == null)
        {
            if (user != null)
                user.sendTranslated("&cNo item given!");
            result = false;
        }
        return result;
    }

    public boolean isOwner(User user)
    {
        return this.blockInfo.isOwner(user);
    }

    public void updateSignText()
    {
        Block block = this.getLocation().getWorld().getBlockAt(this.getLocation());
        if (block.getState() instanceof Sign)
        {
            Sign blockState = (Sign)block.getState();
            String[] lines = new String[4];
            boolean isValid = this.isValidSign(null);
            if (this.isInEditMode())
            {
                if (this.isAdminSign())
                {
                    lines[0] = "&5&lAdmin-";
                }
                else
                {
                    lines[0] = "&5&l";
                }
            }
            else if (!isValid ||(this.isBuySign() && this.isSoldOut()) || (!this.isBuySign() && this.isSatisfied()))
            {
                lines[0] = "&4&l";
            }
            else if (this.isAdminSign())
            {
                lines[0] = "&9&lAdmin-";
            }
            else
            {
                lines[0] = "&1&l";
            }
            if (this.blockInfo.isBuyOrSell())
            {
                if (this.isBuySign())
                {
                    if (!this.isInEditMode() && this.isSoldOut())
                    {
                        if (this.allowBuyIfEmpty())
                        {
                            lines[0] = "&9&l&oAdmin-Buy";
                        }
                        else
                        {
                            lines[0] += "Sold Out";
                        }
                    }
                    else
                    {
                        lines[0] += "Buy";
                    }
                }
                else
                {
                    if (!this.isInEditMode() && this.isSatisfied())
                    {
                        lines[0] += "satisfied";
                    }
                    else
                    {
                        lines[0] += "Sell";
                    }
                }
            }
            else
            {
                if (this.isInEditMode())
                {
                    lines[0] += "Edit";
                }
                else
                {
                    lines[0] += "Invalid";
                }
            }
            ItemStack item = this.getItem();
            if (item == null)
            {
                if (this.isInEditMode())
                {
                    lines[1] = "&5No Item";
                }
                else
                {
                    lines[1] = "&4No Item";
                }
            }
            else if (item.getItemMeta().hasDisplayName() || item.getItemMeta().hasLore() || !item.getEnchantments().isEmpty())
            {
                if (item.getItemMeta().hasDisplayName())
                {
                    lines[1] = "&e" + item.getItemMeta().getDisplayName();
                }
                else
                {
                    lines[1] = "&e" + Match.material().getNameFor(this.getItem());
                }
            }
            else
            {
                lines[1] = Match.material().getNameFor(this.getItem());
            }
            if (this.getAmount() == 0)
            {
                if (this.isInEditMode())
                {
                    lines[2] = "&5No amount";
                }
                else
                {
                    lines[2] = "&4No amount";
                }
            }
            else
            {
                lines[2] = String.valueOf(this.getAmount());
                if (this.isBuySign() == null)
                {
                    lines[2] = "&4" + lines[2];
                }
                else if (this.isBuySign())
                {
                    if (this.isSoldOut())
                    {
                        if (this.allowBuyIfEmpty())
                        {
                            lines[2] += " &1&ox" + this.getStock();
                        }
                        else
                        {
                            lines[2] += " &4x" + this.getStock();
                        }
                    }
                    else if (this.hasStock())
                    {
                        lines[2] += " &1x" + this.getStock();
                    }
                }
                else if (this.hasStock())
                {
                    if (this.canAfford(this.getOwner()) && !this.isSatisfied() && !this.isFull())
                    {
                        if (this.hasDemand())
                        {
                            lines[2] += " &bx" + (this.getDemand() - this.getStock());
                        }
                        else
                        {
                            lines[2] += " &bx?";
                        }
                    }
                    else if (this.hasDemand())
                    {
                        lines[2] += " &4x" + (this.getDemand() - this.getStock());
                    }
                    else
                    {
                        lines[2] += " &4x?";
                    }
                }
            }
            lines[3] = this.parsePrice();

            lines[0] = ChatFormat.parseFormats(lines[0]);
            lines[1] = ChatFormat.parseFormats(lines[1]);
            lines[2] = ChatFormat.parseFormats(lines[2]);
            lines[3] = ChatFormat.parseFormats(lines[3]);
            for (int i = 0; i < 4; ++i)
            {
                blockState.setLine(i, lines[i]);
            }
            blockState.update();
        }
        else
        {
            this.module.getLog().warning("No sign found where a market-sign was expected! " + this.getLocation());
        }
    }

    private boolean isSoldOut()
    {
        if (this.blockInfo.isBuyOrSell() && this.isBuySign())
        {
            if (this.hasStock() && (this.getStock() < this.getAmount() || this.getStock() == 0))
            {
                return true;
            }
        }
        return false;
    }

    private boolean canAfford(User user)
    {
        if (user == null || this.getPrice() == 0)
        {
            return true;
        }
        return this.economy.has(user.getName(), this.getPrice());
    }

    public Inventory getInventory()
    {
        Inventory inventory = this.itemInfo.getInventory();
        if (inventory == null)
        {
            if (this.isAdminSign())
            {
                inventory = Bukkit.getServer().createInventory(this.itemInfo, InventoryType.DISPENSER);
            }
            else
            {
                String signString;
                if (this.isBuySign())
                {
                    signString = "MarketSign - Buy";
                }
                else
                {
                    signString = "MarketSign - Sell";
                }
                inventory = Bukkit.getServer().createInventory(this.itemInfo, this.itemInfo.getSize(), signString); // DOUBLE-CHEST
                ItemStack item = this.getItem().clone();
                item.setAmount(this.itemInfo.stock);
                if (this.itemInfo.stock > 0)
                    this.addToInventory(inventory,item);
            }
            this.itemInfo.initInventory(inventory);
        }
        if (this.isAdminSign())
        {
            inventory.setItem(4, this.getItem());
        }
        return inventory;
    }

    public int getAmount()
    {
        return this.blockInfo.amount;
    }

    /**
     * Sets the new Item-Info and returns the replaced infoModel
     * The item-infos and block-info will be updated accordingly
     *
     * @param itemInfo
     * @return
     */
    public SignMarketItemModel setItemInfo(SignMarketItemModel itemInfo)
    {
        SignMarketItemModel old = this.itemInfo;
        if (old != null)
        {
            old.removeSign(this);
        }
        this.itemInfo = itemInfo;
        itemInfo.addSign(this);
        this.blockInfo.itemKey = itemInfo.key;
        return old;
    }

    public void setBlockInfo(SignMarketBlockModel blockModel)
    {
        this.blockInfo = blockModel;
    }

    public Integer getStock()
    {
        return this.itemInfo.stock;
    }

    public void setStock(Integer stock)
    {
        this.itemInfo.stock = stock;
    }

    public Boolean isBuySign()
    {
        return this.blockInfo.signType;
    }

    public Integer getDemand()
    {
        return this.blockInfo.demand;
    }

    public double getPrice()
    {
        if (this.allowBuyIfEmpty())
        {
            return (double)(this.module.getConfig().factorIfAdminSignIsEmpty * this.blockInfo.price) / this.economy.fractionalDigitsFactor();
        }
        return (double) this.blockInfo.price / this.economy.fractionalDigitsFactor();
    }

    public boolean isAdminSign() {
        return this.blockInfo.owner == null;
    }

    public void enterEditMode()
    {
        if (this.editMode) return;
        if (this.itemInfo.getReferenced().size() > 1) // ItemInfo is synced with other signs
        {
            SignMarketItemModel newItemInfo = this.itemInfo.clone();
            this.setItemInfo(newItemInfo); // de-sync to prevent changing other signs
        }
        this.editMode = true;
        this.updateSignText();
    }

    public void exitEditMode(User user)
    {
        this.editMode = false;
        this.updateSignText();
        if (this.isValidSign(user))
        {
            this.saveToDatabase(); // re-sync item-info in here
        }
    }

    public void setDemand(Integer demand)
    {
        this.blockInfo.demand = demand;
    }

    public boolean isInEditMode()
    {
        return this.editMode;
    }

    public Location getLocation()
    {
        return this.blockInfo.getLocation();
    }

    public Integer getRemainingDemand()
    {
        if (this.getDemand() != null)
        {
            return this.getDemand() - this.getStock();
        }
        return null;
    }

    public SignMarketBlockModel getBlockInfo()
    {
        return this.blockInfo;
    }

    public SignMarketItemModel getItemInfo()
    {
        return itemInfo;
    }

    public ItemStack getItem()
    {
        return this.itemInfo.getItem();
    }

    public void copyValuesFrom(MarketSign prevMarketSign)
    {
        this.blockInfo.copyValuesFrom(prevMarketSign.blockInfo);
        this.itemInfo.copyValuesFrom(prevMarketSign.itemInfo);
    }

    public void setSize(Integer size)
    {
        if (size == 0 || size < -1 || size > 6)
        {
            throw new IllegalArgumentException("Invalid inventory-size!");
        }
        this.itemInfo.size = size;
    }
}
