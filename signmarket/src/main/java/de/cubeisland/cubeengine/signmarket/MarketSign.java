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

import java.lang.ref.WeakReference;
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
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.service.Economy;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.InventoryGuardFactory;
import de.cubeisland.cubeengine.core.util.RomanNumbers;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.signmarket.exceptions.NoDemandException;
import de.cubeisland.cubeengine.signmarket.exceptions.NoOwnerException;
import de.cubeisland.cubeengine.signmarket.exceptions.NoStockException;
import de.cubeisland.cubeengine.signmarket.exceptions.NoTypeException;
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
    private WeakReference<User> userOwner;

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
        if (owner != null)
        {
            this.userOwner = new WeakReference<User>(owner);
        }
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

    public void breakSign(User user)
    {
        if (user.getGameMode().equals(GameMode.CREATIVE))
        {
            this.getLocation().getBlock().setType(Material.AIR);
        }
        else
        {
            this.getLocation().getBlock().breakNaturally();
        }
        this.dropContents();
        this.msFactory.delete(this);
    }

    public void dropContents()
    {
        if (this.isAdminSign() || !this.hasStock() || this.itemInfo.sharesStock() || this.getStock() <= 0) return;
        ItemStack item = this.itemInfo.getItem().clone();
        item.setAmount(this.itemInfo.stock);
        this.itemInfo.stock = 0; // just to be sure no items are duped
        if (this.module.getConfig().allowOverStackedOutOfSign)
        {
            this.getLocation().getWorld().dropItemNaturally(this.getLocation(), item);
            return;
        }
        for (ItemStack itemStack : splitIntoMaxItems(item, item.getMaxStackSize()))
        {
            // TODO this could possibly cause lag when breaking user-signs with A LOT of items
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
     * Returns whether this sign already has a sign-type set.
     *
     * @return true if the sign is a buy or a sell sign
     */
    public boolean hasType()
    {
        return this.blockInfo.signType != null;
    }

    /**
     * Changes this market-sign to be a BUY-sign
     */
    public void setTypeBuy()
    {
        this.blockInfo.signType = true;
        this.setNoDemand();
    }

    /**
     * Changes this market-sign to be a SELL-sign
     */
    public void setTypeSell()
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
        if (!this.hasStock())
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
            if (this.isTypeBuy())
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
                if (this.isInEditMode())
                {
                    user.sendTranslated("&cThis sign is being edited right now!");
                    return;
                }
                if (sneaking)
                {
                    if (!this.isAdminSign() && (this.isOwner(user) || MarketSignPerm.SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user)))
                    {
                        if (this.isTypeBuy() && this.itemInfo.matchesItem(itemInHand))
                        {
                            if (!this.getInventory().getViewers().isEmpty())
                            {
                                user.sendTranslated("&cThis signs inventory is being edited right now!");
                                return;
                            }
                            int amount = this.putItems(user, true);
                            if (amount != 0)
                            {
                                user.sendTranslated("&aAdded all (&6%d&a) &6%s&a to the stock!", amount, Match.material().getNameFor(this.itemInfo.getItem()));
                            }
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
                            if (!this.isInEditMode() && this.hasType() && this.isTypeBuy() && this.hasStock() && this.itemInfo.matchesItem(itemInHand))
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
                            else if (itemInHand.getTypeId() != 0)
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
                }
                else
                {
                    if (this.isInEditMode())
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
    }

    public void showInfo(User user)
    {
        if (this.isInEditMode())
        {
            user.sendTranslated("\n-- &5Sign Market - Edit Mode &f--");
        }
        else
        {
            user.sendTranslated("\n--------- &6Sign Market &f---------");
        }
        if (!this.hasType())
        {
            user.sendMessage("&5new Sign");
            return;
        }
        if (this.isTypeBuy())
        {
            if (this.isAdminSign())
            {
                user.sendTranslated("&3Buy: &6%d &ffor &6%s &ffrom &6%s", this.getAmount(), this.parsePrice(), "Server");
            }
            else
            {
                user.sendTranslated("&3Buy: &6%d &ffor &6%s &ffrom &2%s", this.getAmount(), this.parsePrice(), this.getOwner().getName());
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
                user.sendTranslated("&3Sell: &6%d &ffor &6%s &fto &2%s", this.getAmount(), this.parsePrice(), this.getOwner().getName());
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
        else
        {
            ItemMeta meta = this.getItem().getItemMeta();
            if (meta.hasDisplayName())
            {
                user.sendMessage("&e" + Match.material().getNameFor(this.getItem()) + " &f(&6" + this.getItem().getItemMeta().getDisplayName() + "&f)");
            }
            else
            {
                if (meta.hasLore() || !meta.getEnchants().isEmpty())
                {
                    user.sendMessage("&e" + Match.material().getNameFor(this.getItem()));
                }
                else
                {
                    user.sendMessage("&6" + Match.material().getNameFor(this.getItem()));
                }
            }
            if (meta.hasLore())
            {
                for (String loreLine : this.getItem().getItemMeta().getLore())
                {
                    user.sendMessage(" &e-&f " + loreLine);
                }
            }
            if (!meta.getEnchants().isEmpty())
            {
                user.sendTranslated("&6Enchantments:");
            }
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet())
            {
                user.sendMessage(" &e-&6 " + Match.enchant().nameFor(entry.getKey()) + " &e" + RomanNumbers.intToRoman(entry.getValue()));
            }
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

    /**
     * Returns true if both item-models share the same item and are not infinite item-sources
     * <p>in addition to this the market-signs have to share their owner too!
     *
     * @param model the model to compare to
     * @return
     */
    public boolean canSync(MarketSign model)
    {
        return this.hasStock() == model.hasStock()
            && this.getItem().isSimilar(model.getItem())
            && this.itemInfo.size == model.itemInfo.size;
    }

    /**
     * Returns the size of the display-chest
     *
     * @return
     */
    public int getChestSize()
    {
        if (this.itemInfo.size == -1)
        {
            return 54;
        }
        return this.itemInfo.size * 9;
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
            if (this.hasStock() && this.getStock() == 1337) //pssst i am not here
            {
                location.getWorld().strikeLightningEffect(location);
            }
            this.breakSign(user);
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
            if (this.getMaxItemAmount() >= this.getStock() + this.getAmount())
            {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isSatisfied() throws NoStockException, NoDemandException
    {
        if (!this.hasStock()) throw new NoStockException();
        if (!this.hasDemand()) throw new NoDemandException();
        return this.getStock() >= this.getDemand();
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

    public boolean hasInfiniteSize()
    {
        return this.itemInfo.size == -1;
    }

    @SuppressWarnings("deprecation")
    private void useSign(User user)
    {
        if (this.isValidSign(user))
        {
            if (this.isTypeBuy())
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

    public User getOwner() throws NoOwnerException
    {
        if (this.blockInfo.owner == null) throw new NoOwnerException();
        if (userOwner == null || userOwner.get() == null)
        {
            userOwner = new WeakReference<User>(CubeEngine.getUserManager().getUser(this.blockInfo.owner));
        }
        return userOwner.get();
    }

    public boolean isValidSign(User user)
    {
        boolean result = true;
        if (!this.hasType())
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
            else if (!isValid ||(this.isTypeBuy() && this.isSoldOut()) || (!this.isTypeBuy() && this.hasDemand() && this.isSatisfied()))
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
            if (this.hasType())
            {
                if (this.isTypeBuy())
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
                    if (!this.isInEditMode() && this.hasDemand() && this.isSatisfied())
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
                if (!this.hasType())
                {
                    lines[2] = "&4" + lines[2];
                }
                else if (this.isTypeBuy())
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
                    if (this.isAdminSign() || (this.canAfford(this.getOwner()) && !this.isFull() && !(this.hasDemand() && this.isSatisfied())))
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
            this.module.getLog().warn("No sign found where a market-sign was expected! {}" , this.getLocation());
        }
    }

    private boolean isSoldOut()
    {
        if (this.hasType() && this.isTypeBuy())
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
                if (this.isTypeBuy())
                {
                    signString = "MarketSign - Buy";
                }
                else
                {
                    signString = "MarketSign - Sell";
                }
                inventory = Bukkit.getServer().createInventory(this.itemInfo, this.getChestSize(), signString); // DOUBLE-CHEST
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
     * @param itemInfo the new item-info to set
     * @return the old item-info
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

    /**
     * Returns whether this sign does have a stock or not
     *
     * @return true if this sign has a stock
     */
    public boolean hasStock()
    {
        return this.itemInfo.stock != null;
    }

    /**
     * Returns the amount of items in stock in this sign
     *
     * @return the amount of items in stock
     * @throws NoStockException when this sign has no stock
     */
    public int getStock() throws NoStockException
    {
        if (this.itemInfo.stock == null) throw new NoStockException();
        return this.itemInfo.stock;
    }

    /**
     * Sets the stock of this sign to the specified amount
     *
     * @param amount the amount to set the stock to
     */
    public void setStock(int amount)
    {
        this.itemInfo.stock = amount;
    }

    /**
     * Sets this sign having no stock
     */
    public void setNoStock()
    {
        this.itemInfo.stock = null;
    }

    /**
     * Returns whether this sign does have a demand or not
     * <p>Only sell signs can have a demand
     *
     * @return true if this sign has a demand set
     */
    public boolean hasDemand()
    {
        return this.blockInfo.demand != null;
    }

    /**
     * Returns the total amount of items that can be sold to this sign
     *
     * @return the total demand
     * @throws NoDemandException when this sign is a buy-sign or has no demand
     */
    public int getDemand() throws NoDemandException
    {
        if (this.isTypeBuy() || this.blockInfo.demand == null) throw new NoDemandException();
        return this.blockInfo.demand;
    }

    /**
     * Sets the demand of this sign to given amount
     *
     * @param amount the new demand
     * @throws NoDemandException when this sign is a buy-sign and therefore does not allow demand to be set
     */
    public void setDemand(int amount)
    {
        if (this.isTypeBuy()) throw new NoDemandException();
        this.blockInfo.demand = amount;
    }

    /**
     * Sets this sign having no demand
     * <p>Buy-signs do this automatically
     */
    public void setNoDemand()
    {
        this.blockInfo.demand = null;
    }

    /**
     * Gets the price for items of this sign
     *
     * @return the price
     */
    public double getPrice()
    {
        if (this.allowBuyIfEmpty())
        {
            return this.economy.convertLongToDouble((long)(this.blockInfo.price * this.module.getConfig().factorIfAdminSignIsEmpty));
        }
        return this.economy.convertLongToDouble(this.blockInfo.price);
    }

    /**
     * Returns whether this sign a is a buy sign
     *
     * @return true if this is a buy sign
     * @throws NoTypeException if no sign-type is set
     */
    public Boolean isTypeBuy() throws NoTypeException
    {
        if (!this.hasType()) throw new NoTypeException();
        return this.blockInfo.signType;
    }

    /**
     * Returns whether this sign a is a admin sign
     *
     * @return true if this is a admin sign
     */
    public boolean isAdminSign()
    {
        return this.blockInfo.owner == null;
    }

    public void enterEditMode()
    {
        if (this.isInEditMode()) return;
        if (this.itemInfo.getReferenced().size() > 1) // ItemInfo is synced with other signs
        {
            this.module.getLog().debug("block-model #{} de-synced from item-model #{} (size:{}-1)",
                     this.blockInfo.key, this.itemInfo.key, this.itemInfo.getReferenced().size());
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

    public boolean isInEditMode()
    {
        return this.editMode;
    }

    public Location getLocation()
    {
        return this.blockInfo.getLocation();
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

    /**
     * Returns the UserOwner OR null if this is an admin sign
     *
     * @return the owner or null
     */
    public User getRawOwner()
    {
        return this.isAdminSign() ? null : this.getOwner();
    }
}
