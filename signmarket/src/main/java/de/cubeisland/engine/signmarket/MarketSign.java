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
package de.cubeisland.engine.signmarket;

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
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.module.service.Economy;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.InventoryGuardFactory;
import de.cubeisland.engine.core.util.RomanNumbers;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.signmarket.exceptions.NoDemandException;
import de.cubeisland.engine.signmarket.exceptions.NoOwnerException;
import de.cubeisland.engine.signmarket.exceptions.NoStockException;
import de.cubeisland.engine.signmarket.exceptions.NoTypeException;
import de.cubeisland.engine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.engine.signmarket.storage.SignMarketItemModel;
import gnu.trove.map.hash.TLongLongHashMap;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

import static de.cubeisland.engine.core.util.InventoryUtil.*;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static de.cubeisland.engine.signmarket.storage.TableSignBlock.TABLE_SIGN_BLOCK;
import static de.cubeisland.engine.signmarket.storage.TableSignItem.TABLE_SIGN_ITEM;

// TODO http://git.cubeisland.de/cubeengine/cubeengine/issues/414 shift-click to edit multiple signs at the same time
// TODO http://git.cubeisland.de/cubeengine/cubeengine/issues/431 blacklist

public class MarketSign
{
    private final MarketSignFactory msFactory;
    private final Signmarket module;
    protected final Economy economy;
    private SignMarketItemModel itemInfo;
    private final SignMarketBlockModel blockInfo;
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
        DSLContext dsl = module.getCore().getDB().getDSL();
        this.module = module;
        this.economy = module.getCore().getModuleManager().getServiceManager().getServiceImplementation(Economy.class);
        this.blockInfo = dsl.newRecord(TABLE_SIGN_BLOCK).newBlockModel(location);
        this.setItemInfo(dsl.newRecord(TABLE_SIGN_ITEM));
        this.msFactory = module.getMarketSignFactory();

        this.blockInfo.setOwner(owner == null ? null : owner.getEntity().getKey());
        if (owner != null)
        {
            this.userOwner = new WeakReference<>(owner);
        }
    }

    public MarketSign(Signmarket module, SignMarketItemModel itemModel, SignMarketBlockModel blockModel)
    {
        this.module = module;
        this.economy = module.getCore().getModuleManager().getServiceManager().getServiceImplementation(Economy.class);
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
        this.msFactory.syncAndSaveSign(this);
        if (!this.getItemInfo().sharesStock())
        {
            this.dropContents();
        }
        this.msFactory.delete(this);
    }

    public void dropContents()
    {
        if (this.isAdminSign() || !this.hasStock() || this.itemInfo.sharesStock() || this.getStock() <= 0) return;
        ItemStack item = this.itemInfo.getItemStack().clone();
        item.setAmount(this.itemInfo.getStock().intValue());
        this.itemInfo.setStock(UInteger.valueOf(0)); // just to be sure no items are duped
        if (item.getAmount() > item.getMaxStackSize() * 5400 // prevent lag from throwing huge amount of items out of the sign
                                                             // amount of 100 DoubleChest full with given item
            || this.module.getConfig().allowOverStackedOutOfSign)
        {
            this.getLocation().getWorld().dropItemNaturally(this.getLocation(), item);
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
        this.itemInfo.setItemStack(itemStack);
        if (setAmount)
        {
            this.setAmount(itemStack.getAmount());
        }
    }

    /**
     * Returns whether this sign already has a sign-type set.
     *
     * @return true if the sign is a buy or a sell sign
     */
    public boolean hasType()
    {
        return this.blockInfo.getSigntype() != null;
    }

    /**
     * Changes this market-sign to be a BUY-sign
     */
    public void setTypeBuy()
    {
        this.blockInfo.setSigntype(BUY_SIGN);
        this.setNoDemand();
    }

    /**
     * Changes this market-sign to be a SELL-sign
     */
    public void setTypeSell()
    {
        this.blockInfo.setSigntype(SELL_SIGN);
    }

    public static final Byte SELL_SIGN = 0;
    public static final Byte BUY_SIGN = 1;

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
        this.blockInfo.setOwner(user.getEntity().getKey());
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
        this.blockInfo.setAmount(UShort.valueOf(amount));
    }

    /**
     * Sets the price to buy/sell the specified amount of items with each click
     *
     * @param price
     */
    public void setPrice(long price)
    {
        this.blockInfo.setPrice(UInteger.valueOf(price));
    }

    private int inventoryStock;
    private Inventory displayInventory;

    public boolean openInventory(User user)
    {
        if (this.isOwner(user) || (!this.isAdminSign() && module.perms().SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user)))
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
                        int newStock = getAmountOf(inventory, MarketSign.this.itemInfo.getItemStack());
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
                        int newStock = getAmountOf(inventory, MarketSign.this.itemInfo.getItemStack());
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
            ItemStack itemInSign = this.itemInfo.getItemStack();
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
        if (module.perms().SIGN_INVENTORY_SHOW.isAuthorized(user) || this.isOwner(user))
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
        this.updateSignText();
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
                    user.sendTranslated(NEGATIVE, "This sign is being edited right now!");
                    return;
                }
                if (sneaking)
                {
                    if (this.isValidSign(null))
                    {
                        if (!this.isAdminSign() && (this.isOwner(user) || module.perms().SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user)))
                        {
                            if (this.isTypeBuy() && this.itemInfo.matchesItem(itemInHand))
                            {
                                if (!this.getInventory().getViewers().isEmpty())
                                {
                                    user.sendTranslated(NEGATIVE, "This signs inventory is being edited right now!");
                                    return;
                                }
                                int amount = this.putItems(user, true);
                                if (amount != 0)
                                {
                                    user.sendTranslated(POSITIVE, "Added all ({amount}) {name#material} to the stock!", amount, Match.material().getNameFor(this.itemInfo.getItemStack()));
                                }
                                return;
                            }
                        }
                        if (!this.openInventory(user))
                        {
                            user.sendTranslated(NEGATIVE, "You are not allowed to see the market signs inventories");
                        }
                    }
                    else
                    {
                        user.sendTranslated(NEGATIVE, "Invalid sign!");
                    }
                    return;
                }
                else
                // no sneak -> empty & break signs
                {
                    if (this.isValidSign(null))
                    {
                        if (!this.getInventory().getViewers().isEmpty())
                        {
                            user.sendTranslated(NEGATIVE, "This signs inventory is being edited right now!");
                            return;
                        }
                        if (this.isOwner(user) || module.perms().SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user))
                        {
                            if (!this.isInEditMode() && this.hasType() && this.isTypeBuy() && this.hasStock() && this.itemInfo.matchesItem(itemInHand))
                            {
                                if (!this.getInventory().getViewers().isEmpty())
                                {
                                    user.sendTranslated(NEGATIVE, "This signs inventory is being edited right now!");
                                    return;
                                }
                                int amount = this.putItems(user, false);
                                if (amount != 0)
                                    user.sendTranslated(POSITIVE, "Added {amount}x {input#material} to the stock!", amount, Match.material().getNameFor(this.itemInfo.getItemStack()));
                                return;
                            }
                            else if (itemInHand.getTypeId() != 0)
                            {
                                user.sendTranslated(NEGATIVE, "Use your bare hands to break the sign!");
                                return;
                            }
                        }
                    }
                    if (user.getGameMode().equals(GameMode.CREATIVE)) // instabreak items
                    {
                        if (this.isOwner(user))
                        {
                            if (module.perms().SIGN_DESTROY_OWN.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated(NEGATIVE, "You are not allowed to break your own market signs!");
                            }
                        }
                        else if (this.isAdminSign())
                        {
                            if (module.perms().SIGN_DESTROY_ADMIN.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated(NEGATIVE, "You are not allowed to break admin market signs!");
                            }
                        }
                        else
                        {
                            if (module.perms().SIGN_DESTROY_OTHER.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated(NEGATIVE, "You are not allowed to break others market signs!");
                            }
                        }
                    }
                    else
                    // first empty items then break
                    {
                        if (this.isAdminSign())
                        {
                            if (module.perms().SIGN_DESTROY_ADMIN.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated(NEGATIVE, "You are not allowed to break admin signs!");
                            }
                        }
                        else if (this.isOwner(user))
                        {
                            if (module.perms().SIGN_DESTROY_OWN.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated(NEGATIVE, "You are not allowed to break your own market signs!");
                            }
                        }
                        else
                        // not owner / not admin
                        {
                            if (module.perms().SIGN_DESTROY_OTHER.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendTranslated(NEGATIVE, "You are not allowed to destroy others market signs!");
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
                        user.sendTranslated(NEGATIVE, "This sign is being edited right now!");
                        return;
                    }
                    if (this.isValidSign(null))
                    {
                        if (!this.getInventory().getViewers().isEmpty())
                        {
                            user.sendTranslated(NEGATIVE, "This signs inventory is being edited right now!");
                            return;
                        }
                        if (this.isOwner(user))
                        {
                            this.takeItems(user);
                            return;
                        }
                    }
                    this.useSign(user);
                }
        }
    }

    public void showInfo(User user)
    {
        if (this.isInEditMode())
        {
            user.sendMessage("");
            user.sendTranslated(NONE, "-- {text:Sign Market:color=PURPLE} - {text:Edit Mode:color=PURPLE} --");
        }
        else
        {
            user.sendMessage("");
            user.sendTranslated(NONE, "--------- {text:Sign Market} ---------");
        }
        if (!this.hasType())
        {
            user.sendMessage(ChatFormat.PURPLE + "new Sign");
            return;
        }
        if (this.isTypeBuy())
        {
            if (this.isAdminSign())
            {
                user.sendTranslated(NONE, "{text:Buy:color=DARK_BLUE}: {amount} for {input#price} from {input#owner}", this.getAmount(), this.parsePrice(), "Server");
            }
            else
            {
                user.sendTranslated(NONE, "{text:Buy:color=DARK_BLUE}: {amount} for {input#price} from {user#owner}", this.getAmount(), this.parsePrice(), this.getOwner());
            }
        }
        else
        {
            if (this.isAdminSign())
            {
                user.sendTranslated(NONE, "{text:Sell:color=DARK_BLUE}: {amount} for {input#price} to {input#owner}", this.getAmount(), this.parsePrice(), "Server");
            }
            else
            {
                user.sendTranslated(NONE, "{text:Sell:color=DARK_BLUE}: {amount} for {input#price} to {user#owner}", this.getAmount(), this.parsePrice(), this.getOwner());
            }
        }
        if (this.getItem() == null)
        {
            if (this.isInEditMode())
            {
                user.sendTranslated(NONE, ChatFormat.PURPLE + "No Item");
            }
            else
            {
                user.sendTranslated(NONE, ChatFormat.DARK_RED + "No Item");
            }
        }
        else
        {
            ItemMeta meta = this.getItem().getItemMeta();
            if (meta.hasDisplayName())
            {
                user.sendMessage(ChatFormat.YELLOW + Match.material().getNameFor(this.getItem()) + ChatFormat.WHITE +
                                     " ("  + ChatFormat.GOLD + this.getItem().getItemMeta().getDisplayName() + ChatFormat.WHITE + ")");
            }
            else
            {
                if (meta.hasLore() || !meta.getEnchants().isEmpty())
                {
                    user.sendMessage(ChatFormat.YELLOW + Match.material().getNameFor(this.getItem()));
                }
                else
                {
                    user.sendMessage(ChatFormat.GOLD + Match.material().getNameFor(this.getItem()));
                }
            }
            if (meta.hasLore())
            {
                for (String loreLine : this.getItem().getItemMeta().getLore())
                {
                    user.sendMessage(ChatFormat.YELLOW + " - " + ChatFormat.WHITE + loreLine);
                }
            }
            if (!meta.getEnchants().isEmpty())
            {
                user.sendTranslated(NEUTRAL, "Enchantments:");
            }
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet())
            {
                user.sendMessage(ChatFormat.YELLOW + " - " + ChatFormat.GOLD + Match.enchant().nameFor(entry.getKey()) + " " + ChatFormat.YELLOW + RomanNumbers.intToRoman(entry.getValue()));
            }
            if (meta instanceof EnchantmentStorageMeta)
            {
                if (!((EnchantmentStorageMeta)meta).getStoredEnchants().isEmpty())
                {
                    user.sendTranslated(NEUTRAL, "Book-Enchantments:");
                    for (Map.Entry<Enchantment, Integer> entry : ((EnchantmentStorageMeta)meta).getStoredEnchants().entrySet())
                    {
                        user.sendMessage(ChatFormat.YELLOW + " - " + ChatFormat.GOLD + Match.enchant().nameFor(entry.getKey()) + " " + ChatFormat.YELLOW + RomanNumbers.intToRoman(entry.getValue()));
                    }
                }
            }
        }
        if (this.hasStock())
        {
            if (!this.hasDemand() && this.hasInfiniteSize())
            {
                user.sendTranslated(NEUTRAL, "In stock: {amount}/{text:Infinite}", this.getStock());
            }
            else if (this.getItem() == null || this.getAmount() == 0)
            {
                user.sendTranslated(NEUTRAL, "In stock: {amount}/{text:Unknown:color=RED}", this.getStock());
            }
            else
            {
                user.sendTranslated(NEUTRAL, "In stock: {amount}/{amount#max}", this.getStock(), this.getMaxItemAmount());
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
        return this.isValidSign(null)
            && this.hasStock() == model.hasStock()
            && this.getItem().isSimilar(model.getItem())
            && this.itemInfo.getSize() == model.itemInfo.getSize()
            && this.module.getConfig().canSync(this.module.getCore().getWorldManager(), this.blockInfo.getWorld(), model.blockInfo.getWorld());
    }

    /**
     * Returns the size of the display-chest
     *
     * @return
     */
    public int getChestSize()
    {
        if (this.itemInfo.getSize() == -1)
        {
            return 54;
        }
        return this.itemInfo.getSize() * 9;
    }

    private String parsePrice()
    {
        if (this.blockInfo.getPrice() == null || this.blockInfo.getPrice().longValue() == 0)
        {
            if (this.isInEditMode())
            {
                return ChatFormat.PURPLE + "No Price";
            }
            else
            {
                return ChatFormat.DARK_RED + "No Price";
            }
        }
        if (this.allowBuyIfEmpty())
        {
            return ChatFormat.ITALIC + this.economy.format(this.getPrice());
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
                    user.sendTranslated(NEGATIVE, "The market sign inventory is full!");
                    return 0;
                }
                user.sendTranslated(NEGATIVE, "The market sign cannot hold all your items!");
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
            user.sendTranslated(NEGATIVE, "There are no more items stored in the sign!");
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
            user.sendTranslated(NEGATIVE, "Your inventory is full!");
        }
        user.updateInventory();
        this.saveToDatabase();
    }

    public boolean tryBreak(User user)
    {
        if (this.breakingSign.containsKey(user.getId()) && System.currentTimeMillis() - this.breakingSign.get(user.getId()) <= 500)//0.5 sec
        {
            Location location = this.getLocation();
            if (this.hasStock() && this.getStock() == 1337) //pssst i am not here
            {
                location.getWorld().strikeLightningEffect(location);
            }
            this.breakSign(user);
            this.breakingSign.remove(user.getId());
            user.sendTranslated(POSITIVE, "MarketSign destroyed!");
            return true;
        }
        this.breakingSign.put(user.getId(), System.currentTimeMillis());
        user.sendTranslated(NEUTRAL, "Double click to break the sign!");
        return false;
    }

    public boolean isFull()
    {
        if (!this.hasInfiniteSize() && this.hasStock())
        {
            return this.getMaxItemAmount() < this.getStock() + this.getAmount();
        }
        return false;
    }

    public boolean isSatisfied() throws NoStockException, NoDemandException
    {
        if (!this.hasStock()) throw new NoStockException();
        if (!this.hasDemand()) throw new NoDemandException();
        return this.isFull() || this.getStock() >= this.getDemand();
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
        int maxSizeInStacks = this.itemInfo.getSize() * 9;
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
        return this.itemInfo.getSize() == -1;
    }

    @SuppressWarnings("deprecation")
    private void useSign(User user)
    {
        if (this.hasType())
        {
            if (this.isTypeBuy())
            {
                if (!module.perms().USE_BUY.isAuthorized(user))
                {
                    user.sendTranslated(NEGATIVE, "You are not allowed to use buy market signs!");
                    return;
                }
            }
            else
            {
                if (!module.perms().USE_SELL.isAuthorized(user))
                {
                    user.sendTranslated(NEGATIVE, "You are not allowed to use sell market signs!");
                    return;
                }
            }
        }
        this.economy.createAccount(user.getUniqueId());
        if (this.isValidSign(user))
        {
            if (this.isTypeBuy())
            {
                if (this.isSoldOut())
                {
                    if (!this.allowBuyIfEmpty())
                    {
                        user.sendTranslated(NEGATIVE, "This market sign is {text:Sold Out:color=DARK_RED}!");
                        return;
                    }
                }
                if (!this.canAfford(user))
                {
                    user.sendTranslated(NEGATIVE, "You can't afford these items!");
                    return;
                }
                //Account userAccount = this.economy.getUserAccount(user, true);
                //Account ownerAccount = this.getOwner() != null ? this.economy.getUserAccount(this.getOwner(), true) : null;
                ItemStack item = this.getItem().clone();
                item.setAmount(this.getAmount());
                if (checkForPlace(user.getInventory(), item.clone()))
                {
                    String price = this.parsePrice();
                    this.economy.withdraw(user.getUniqueId(), this.getPrice());
                    if (!this.isAdminSign())
                    {
                        this.economy.deposit(this.getOwner().getUniqueId(), this.getPrice());
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
                    user.sendTranslated(POSITIVE, "You bought {amount}x {input#item} for {input#price}.", this.getAmount(), Match.material().getNameFor(this.getItem()), price);
                    return;
                }
                user.sendTranslated(NEGATIVE, "You don't have enough space in your inventory for these items!");
                return;
            } // else Sell
            if (this.hasDemand() && this.isSatisfied())
            {
                user.sendTranslated(NEGATIVE, "This market sign is {text:satisfied:color=DARK_RED}! You can no longer sell items to it.");
                return;
            }
            if (this.isFull())
            {
                user.sendTranslated(NEGATIVE, "This market sign is {text:full:color=DARK_RED}! You can no longer sell items to it.");
                return;
            }
            if (!this.isAdminSign() && !this.canAfford(this.getOwner()))
            {
                user.sendTranslated(NEGATIVE, "The owner cannot afford the money to acquire your items!");
                return;
            }
            this.itemInfo.matchesItem(user.getItemInHand()); // adapt to item in hand (if it has repair-cost)
            if (getAmountOf(user.getInventory(), this.getItem()) < this.getAmount())
            {
                user.sendTranslated(NEGATIVE, "You do not have enough items to sell!");
                return;
            }
            ItemStack item = this.getItem().clone();
            item.setAmount(this.getAmount());

            this.economy.deposit(user.getUniqueId(), this.getPrice());
            if (!this.isAdminSign())
            {
                this.economy.withdraw(this.getOwner().getUniqueId(), this.getPrice());
            }
            user.getInventory().removeItem(item);
            if (this.hasStock())
            {
                this.setStock(this.getStock() + this.getAmount());
                this.saveToDatabase();
            } // else admin sign -> no change
            user.updateInventory();
            user.sendTranslated(POSITIVE, "You sold {amount}x {input#item} for {input#price}.", this.getAmount(), Match.material().getNameFor(this.getItem()), this.parsePrice());
        }
    }

    private boolean allowBuyIfEmpty()
    {
        return this.isSoldOut() && this.isAdminSign() && this.module.getConfig().allowBuyIfAdminSignIsEmpty;
    }

    public User getOwner() throws NoOwnerException
    {
        if (this.isAdminSign()) throw new NoOwnerException();
        if (userOwner == null || userOwner.get() == null)
        {
            userOwner = new WeakReference<>(CubeEngine.getUserManager().getUser(this.blockInfo.getOwner()));
        }
        return userOwner.get();
    }

    public boolean isValidSign(User user)
    {
        boolean result = true;
        if (!this.hasType())
        {
            if (user != null)
            {
                user.sendTranslated(NEGATIVE, "No sign-type given!");
            }
            result = false;
        }
        if (this.blockInfo.getAmount() == null || this.blockInfo.getAmount().longValue() <= 0)
        {
            if (user != null)
            {
                user.sendTranslated(NEGATIVE, "Invalid amount!");
            }
            result = false;
        }
        if (this.blockInfo.getPrice() == null || this.blockInfo.getPrice().longValue() <= 0)
        {
            if (user != null)
            {
                user.sendTranslated(NEGATIVE, "Invalid price!");
            }
            result = false;
        }
        if (this.itemInfo.getItemStack() == null)
        {
            if (user != null)
            {
                user.sendTranslated(NEGATIVE, "No item given!");
            }
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
        if (!CubeEngine.isMainThread())
        {
            this.module.getCore().getTaskManager().runTask(this.module, new Runnable()
            {
                @Override
                public void run()
                {
                    updateSignText();
                }
            });
            return;
        }
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
                    lines[0] = ChatFormat.PURPLE.toString() + ChatFormat.BOLD + "Admin-";
                }
                else
                {
                    lines[0] = ChatFormat.PURPLE.toString() + ChatFormat.BOLD;
                }
            }
            else if (!isValid ||(this.isTypeBuy() && this.isSoldOut())
                || (!this.isTypeBuy() && ((this.hasDemand() && this.isSatisfied()) || isFull())))
            {
                lines[0] = ChatFormat.DARK_RED.toString();
                if (this.isAdminSign())
                {
                    lines[0] += "Admin-";
                }
            }
            else if (this.isAdminSign())
            {
                lines[0] = ChatFormat.INDIGO.toString() + ChatFormat.BOLD + "Admin-";
            }
            else
            {
                lines[0] = ChatFormat.DARK_BLUE.toString() + ChatFormat.BOLD;
            }
            if (this.hasType())
            {
                if (this.isTypeBuy())
                {
                    if (!this.isInEditMode() && this.isSoldOut())
                    {
                        if (this.allowBuyIfEmpty())
                        {
                            lines[0] = "" + ChatFormat.INDIGO + ChatFormat.BOLD + ChatFormat.ITALIC + "Admin-Buy";
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
                    lines[1] = ChatFormat.PURPLE + "No Item";
                }
                else
                {
                    lines[1] = ChatFormat.DARK_RED + "No Item";
                }
            }
            else if (item.getItemMeta().hasDisplayName() || item.getItemMeta().hasLore() || !item.getEnchantments().isEmpty())
            {
                if (item.getItemMeta().hasDisplayName())
                {
                    lines[1] = ChatFormat.YELLOW + item.getItemMeta().getDisplayName();
                }
                else
                {
                    lines[1] = ChatFormat.YELLOW + Match.material().getNameFor(this.getItem());
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
                    lines[2] = ChatFormat.PURPLE + "No amount";
                }
                else
                {
                    lines[2] = ChatFormat.DARK_RED + "No amount";
                }
            }
            else
            {
                lines[2] = String.valueOf(this.getAmount());
                if (!this.hasType())
                {
                    lines[2] = ChatFormat.DARK_RED + lines[2];
                }
                else if (this.isTypeBuy())
                {
                    if (this.isSoldOut())
                    {
                        if (this.allowBuyIfEmpty())
                        {
                            lines[2] += " " + ChatFormat.DARK_BLUE + ChatFormat.ITALIC + "x" + this.getStock();
                        }
                        else
                        {
                            lines[2] += " " + ChatFormat.DARK_RED + "x" + this.getStock();
                        }
                    }
                    else if (this.hasStock())
                    {
                        lines[2] += " " + ChatFormat.DARK_BLUE + "x" + this.getStock();
                    }
                }
                else if (this.hasStock())
                {
                    if (this.isAdminSign() || (this.canAfford(this.getOwner()) &&
                        !(this.getItem() != null && this.isFull()) && !(this.hasDemand() && this.isSatisfied())))
                    {
                        if (this.hasDemand())
                        {
                            lines[2] += " " + ChatFormat.AQUA + "x" + (this.getDemand() - this.getStock());
                        }
                        else
                        {
                            lines[2] += " " + ChatFormat.AQUA + "x?";
                        }
                    }
                    else if (this.hasDemand())
                    {
                        lines[2] += " " + ChatFormat.DARK_RED + "x" + (this.getDemand() - this.getStock());
                    }
                    else
                    {
                        lines[2] += " " + ChatFormat.DARK_RED + "x?";
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
            this.module.getLog().warn("No sign found where a market sign was expected! {}" , this.getLocation());
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
        if (this.economy.hasAccount(user.getUniqueId()))
        {
            return this.economy.has(user.getUniqueId(), this.getPrice());
        }
        return false;
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
                item.setAmount(this.itemInfo.getStock().intValue());
                if (this.itemInfo.getStock().longValue() > 0)
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
        return this.blockInfo.getAmount() == null ? 0 : this.blockInfo.getAmount().intValue();
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
        this.blockInfo.setItemkey(itemInfo.getKey());
        return old;
    }

    /**
     * Returns whether this sign does have a stock or not
     *
     * @return true if this sign has a stock
     */
    public boolean hasStock()
    {
        return this.itemInfo.getStock() != null;
    }

    /**
     * Returns the amount of items in stock in this sign
     *
     * @return the amount of items in stock
     * @throws NoStockException when this sign has no stock
     */
    public int getStock() throws NoStockException
    {
        if (!this.hasStock()) throw new NoStockException();
        return this.itemInfo.getStock().intValue();
    }

    /**
     * Sets the stock of this sign to the specified amount
     *
     * @param amount the amount to set the stock to
     */
    public void setStock(int amount)
    {
        this.itemInfo.setStock(UInteger.valueOf(amount));
    }

    /**
     * Sets this sign having no stock
     */
    public void setNoStock()
    {
        this.itemInfo.setStock(null);
    }

    /**
     * Returns whether this sign does have a demand or not
     * <p>Only sell signs can have a demand
     *
     * @return true if this sign has a demand set
     */
    public boolean hasDemand()
    {
        return this.blockInfo.getDemand() != null;
    }

    /**
     * Returns the total amount of items that can be sold to this sign
     *
     * @return the total demand
     * @throws NoDemandException when this sign is a buy-sign or has no demand
     */
    public int getDemand() throws NoDemandException
    {
        if (this.isTypeBuy() || !this.hasDemand()) throw new NoDemandException();
        return this.blockInfo.getDemand().intValue();
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
        this.blockInfo.setDemand(UInteger.valueOf(amount));
    }

    /**
     * Sets this sign having no demand
     * <p>Buy-signs do this automatically
     */
    public void setNoDemand()
    {
        this.blockInfo.setDemand(null);
    }

    /**
     * Gets the price for items of this sign
     *
     * @return the price
     */
    public double getPrice()
    {
        if (this.blockInfo.getPrice() == null)
        {
            return 0;
        }
        if (this.allowBuyIfEmpty())
        {
            return this.economy.convertLongToDouble((long)(this.blockInfo.getPrice().longValue() * this.module.getConfig().factorIfAdminSignIsEmpty));
        }
        return this.economy.convertLongToDouble(this.blockInfo.getPrice().longValue());
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
        return this.blockInfo.getSigntype().equals(BUY_SIGN);
    }

    /**
     * Returns whether this sign a is a admin sign
     *
     * @return true if this is a admin sign
     */
    public boolean isAdminSign()
    {
        return this.blockInfo.isOwner(null);
    }

    public void enterEditMode()
    {
        if (this.isInEditMode()) return;
        if (this.itemInfo.getReferenced().size() > 1) // ItemInfo is synced with other signs
        {
            this.module.getLog().debug("block-model #{} de-synced from item-model #{} (size:{}-1)",
                     this.blockInfo.getKey(), this.itemInfo.getKey(), this.itemInfo.getReferenced().size());
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
        return this.itemInfo.getItemStack();
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
            throw new IllegalArgumentException("Invalid inventory size!");
        }
        this.itemInfo.setSize(size.byteValue());
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
