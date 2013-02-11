package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.conomy.account.Account;
import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.RomanNumbers;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockModel;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketInfoModel;
import gnu.trove.map.hash.TLongLongHashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static de.cubeisland.cubeengine.core.util.InventoryUtil.*;

public class MarketSign implements InventoryHolder
{
    private final Signmarket module;
    private final Location location;
    private SignMarketInfoModel info;
    private SignMarketBlockModel blockInfo;
    
    private TLongLongHashMap breakingSign = new TLongLongHashMap();

    private Inventory inventory = null;
    private User owner;
    private Currency currency;

    public MarketSign(Signmarket module, Location location)
    {
        this(module, location, null);
    }

    public MarketSign(Signmarket module, Location location, User owner)
    {
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
        if (!this.isAdminSign() && this.inventory != null)
        {
            this.info.stock = getAmountOf(this.inventory, this.getItem());
        }
        this.info.updateFromItem();
        //TODO store new marketsigns
        this.module.getSminfoManager().update(this.info);
        this.updateSign();
        //delete on sign destroy
        //delete on invalid sign
        return false;
    }

    public void breakSign()
    {
        if (!this.isAdminSign() && this.module.getConfig().dropItemsInCreative)
        {
            if (this.getStock() > 0)
            {
                ItemStack item = this.getItem();
                item.setAmount(this.getStock());
                if (this.module.getConfig().allowOverStackedOutOfSign)
                {
                    this.location.getWorld().dropItemNaturally(location,item);
                }
                else
                {
                    for (ItemStack itemStack : splitIntoMaxItems(item, item.getMaxStackSize()))
                    {
                        this.location.getWorld().dropItemNaturally(location,itemStack);
                    }
                }
            }
        }
        //TODO delete data in db
    }

    /**
     * Sets the itemstack to buy/sell
     *
     * @param itemStack
     */
    public void setItemStack(ItemStack itemStack)
    {
        this.info.setItem(itemStack);
    }

    /**
     * Changes this market-sign to be a BUY-sign
     */
    public void setBuy()
    {
        this.info.isBuySign = true;
    }

    /**
     * Changes this market-sign to be a SELL-sign
     */
    public void setSell()
    {
        this.info.isBuySign = false;
    }

    /**
     * Changes this market-sign to be a not finished EDIT-sign
     */
    public void setEdit()
    {
        this.info.isBuySign = null;
    }

    /**
     * Sets the owner of this market-sign to given user.
     * If the user is null this is equivalent too {@link #setAdminSign()}
     *
     * @param user
     */
    public void setOwner(User user)
    {
        this.info.owner = user.key;
    }

    /**
     * Sets this market-sign to be an admin sign with infinite money or items
     */
    public void setAdminSign()
    {
        this.info.owner = null;
    }

    /**
     * Sets the amount to buy/sell with each click
     *
     * @param amount
     */
    public void setAmount(int amount)
    {
        this.info.amount = amount;
    }

    /**
     * Sets the price to buy/sell the specified amount of items with each click
     *
     * @param price
     */
    public void setPrice(long price)
    {
        this.info.price = price;
    }

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
                    if (this.isOwner(user) || MarketSignPerm.SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user))
                    {
                        if (this.info.isBuySign && this.info.matchesItem(itemInHand))
                        {
                            int amount = this.putItems(user, true);
                            if (amount != 0)
                                user.sendMessage("signmarket", "&aAdded all (&6%d&a) &6%s &ato the stock!", amount, Match.material().getNameFor(this.info.getItem()));
                            return true;
                        }
                    }
                    if (MarketSignPerm.SIGN_INVENTORY_SHOW.isAuthorized(user))
                    {
                        if (this.isAdminSign())
                        {
                            this.module.getInventoryListener().openInventoryCannotEdit(user, this);
                        }
                        else if (this.isOwner(user)) // owner OR can access other
                        {
                            this.module.getInventoryListener().openInventoryCanEdit(user, this);
                        }
                        else if (MarketSignPerm.SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user))
                        {
                            this.module.getInventoryListener().openInventoryCanEdit(user, this);
                        }
                        else
                        {
                            this.module.getInventoryListener().openInventoryCannotEdit(user, this);
                        }
                    }
                    else
                    {
                        user.sendMessage("signmarket","&cYou are not allowed to see the market-signs inventories");
                    }
                }
                else // no sneak -> empty & break signs
                {
                    if (this.isOwner(user) || MarketSignPerm.SIGN_INVENTORY_ACCESS_OTHER.isAuthorized(user))
                    {
                        if (this.info.isBuySign && this.info.matchesItem(itemInHand))
                        {
                            int amount = this.putItems(user, false);
                            if (amount != 0)
                                user.sendMessage("signmarket","&aAdded &6%d&ax &6%s &ato the stock!",amount, Match.material().getNameFor(this.info.getItem()));
                            return true;
                        }
                        else if (itemInHand != null && itemInHand.getTypeId() != 0)
                        {
                            user.sendMessage("signmarket","&cUse bare hands to break the sign!");
                            return true;
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
                                user.sendMessage("signmarket","&cYou are not allowed to break your own market-signs!");
                            }
                        }
                        else if (this.info.isAdminSign())
                        {
                            if (MarketSignPerm.SIGN_DESTROY_ADMIN.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendMessage("signmarket","&cYou are not allowed to break admin-market-signs!");
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
                                user.sendMessage("signmarket","&cYou are not allowed to break others market-signs!");
                            }
                        }
                    }
                    else // first empty items then break
                    {
                        if (this.info.isAdminSign())
                        {
                            if (MarketSignPerm.SIGN_DESTROY_ADMIN.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendMessage("signmarket", "&cYou are not allowed to break admin-signs!");
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
                                user.sendMessage("signmarket","&cYou are not allowed to break your own market-signs!");
                            }
                        }
                        else // not owner / not admin
                        {
                            if (MarketSignPerm.SIGN_DESTROY_OTHER.isAuthorized(user))
                            {
                                this.tryBreak(user);
                            }
                            else
                            {
                                user.sendMessage("signmarket","&cYou are not allowed to destroy others market-signs!");
                            }
                        }
                    }
                }
                return true;
            case RIGHT_CLICK_BLOCK:
                if (sneaking)
                {
                    this.showInfo(user);
                }
                else
                {
                    if (this.isOwner(user))
                    {
                        this.takeItems(user);
                        return true;
                    }
                    return this.useSign(user);
                }
        }
        return false;
    }

    private void showInfo(User user)
    {
        user.sendMessage("signmarket", "\n--------- &6Sign Market &f---------");
        if (this.info.isBuySign)
        {
            if (this.isAdminSign())
            {
                user.sendMessage("signmarket", "&3Buy: &6%d &ffor &6%s &ffrom &6%s",this.getAmount(),this.parsePrice(),"Server");
            }
            else
            {
                user.sendMessage("signmarket", "&3Buy: &6%d &ffor &6%s &ffrom &2%s",this.getAmount(),this.parsePrice(),
                        this.module.getUserManager().getUser(this.info.owner).getName());
            }
        }
        else
        {
            if (this.isAdminSign())
            {
                user.sendMessage("signmarket", "&3Sell: &6%d &ffor &6%s &fto &6%s",this.getAmount(),this.parsePrice(),"Server");
            }
            else
            {
                user.sendMessage("signmarket", "&3Sell: &6%d &ffor &6%s &fto &2%s",this.getAmount(),this.parsePrice(),
                        this.module.getUserManager().getUser(this.info.owner).getName());
            }
        }
        if (this.getItem().getItemMeta().hasDisplayName() || this.getItem().getItemMeta().hasLore() || !this.getItem().getEnchantments().isEmpty())
        {
            if (this.getItem().getItemMeta().hasDisplayName())
            {
                user.sendMessage("&e"+Match.material().getNameFor(this.getItem())+ " &f(&6"+this.getItem().getItemMeta().getDisplayName()+"&f)");
                if (this.getItem().getItemMeta().hasLore())
                {
                    for (String loreLine: this.getItem().getItemMeta().getLore())
                    {
                        user.sendMessage(" &e-&f "+ loreLine);
                    }
                }
                if (!this.getItem().getEnchantments().isEmpty())
                {
                    user.sendMessage("signmarket","&6Enchantments:");
                }
                for (Map.Entry<Enchantment,Integer> entry : this.getItem().getEnchantments().entrySet())
                {
                    user.sendMessage(" &e-&6 " + Match.enchant().nameFor(entry.getKey())+ " &e"+ RomanNumbers.intToRoman(entry.getValue()));
                }
            }
            else
            {
                user.sendMessage("&e"+Match.material().getNameFor(this.getItem()));
            }
        }
        else
        {
            user.sendMessage("&6"+Match.material().getNameFor(this.getItem()));
        }
        if (!this.isAdminSign())
        {
            if (this.isBuySign())
            {
                user.sendMessage("signmarket","&3In stock: &6%d&f/&63456",this.info.stock);
            }
            else
            {
                user.sendMessage("signmarket","&3In stock: &6%d&f/&6%d",this.info.stock, this.info.demand);
            }
        }
    }

    private String parsePrice()
    {
        return this.getCurrency().formatShort(this.info.price);
    }

    private int putItems(User user, boolean all)
    {
        int amount;
        if (all)
        {
            amount = getAmountOf(user.getInventory(),user.getItemInHand());
        }
        else
        {
            amount = user.getItemInHand().getAmount();
        }
        this.info.stock = this.info.stock + amount;
        ItemStack item = this.getItem();
        item.setAmount(amount);
        user.getInventory().removeItem(item);
        Map<Integer,ItemStack> additional = this.addToInventory(item);
        int amountGivenBack = 0;
        for (ItemStack itemStack : additional.values())
        {
            amountGivenBack += itemStack.getAmount();
            user.getInventory().addItem(itemStack);
        }
        if (amountGivenBack != 0)
        {
            user.sendMessage("&cThe market-sign inventory is full!");
        }
        user.updateInventory();
        this.saveToDatabase();
        return amount-amountGivenBack;
    }

    private Map<Integer,ItemStack> addToInventory(ItemStack item)
    {
        if (this.module.getConfig().allowOverStackedInSign)
        {
            if (this.getAmount() > 64)
            {
                return this.getInventory().addItem(splitIntoMaxItems(item,64));
            }
            return this.getInventory().addItem(splitIntoMaxItems(item,this.getAmount()));
        }
        else
        {
            return this.getInventory().addItem(splitIntoMaxItems(item, item.getMaxStackSize()));
        }
    }

    private Map<Integer,ItemStack> addToUserInventory(User user, ItemStack item)
    {
        if (this.module.getConfig().allowOverStackedOutOfSign)
        {
            return user.getInventory().addItem(splitIntoMaxItems(item,64));
        }
        else
        {
            return user.getInventory().addItem(splitIntoMaxItems(item,item.getMaxStackSize()));
        }
    }

    private void takeItems(User user)
    {
        ItemStack item = this.getItem();
        if (this.getAmount() < this.getStock())
        {
            item.setAmount(this.getAmount());
            this.setStock(this.getStock() - this.getAmount());
        }
        else
        {
            item.setAmount(this.getStock());
            this.setStock(0);
        }
        if (item.getAmount() == 0)
        {
            user.sendMessage("marketsign","&cThere are no more items stored in the sign!");
            return;
        }
        this.getInventory().removeItem(item);
        Map<Integer,ItemStack> additional = this.addToUserInventory(user,item);
        int amountGivenBack = 0;
        for (ItemStack itemStack : additional.values())
        {
            amountGivenBack += itemStack.getAmount();
            this.addToInventory(itemStack);
        }
        if (amountGivenBack != 0 && (amountGivenBack == this.getAmount() || amountGivenBack == this.getStock()))
        {
            user.sendMessage("&cYour inventory is full!");
        }
        user.updateInventory();
        this.saveToDatabase();
    }

    private void tryBreak(User user) {
        if (this.breakingSign.containsKey(user.key) && System.currentTimeMillis() - this.breakingSign.get(user.key) <= 200)//0.2 sec
        {
            if (this.getStock() == 1337) //pssst i am not here
            {
                this.location.getWorld().strikeLightningEffect(location);
            }

            this.breakSign();
            location.getWorld().getBlockAt(location).breakNaturally();
            this.breakingSign.remove(user.key);
        }
        else
        {
            this.breakingSign.put(user.key,System.currentTimeMillis());
            user.sendMessage("signmarket","&eDoubleclick to break the sign!");
        }
    }

    private boolean useSign(User user)
    {
        if (this.isValidSign(user))
        {
            if (this.isBuySign())
            {
                if (this.isAdminSign() || this.getStock() >= this.getAmount())
                {
                    if (this.canAfford(user))
                    {
                        Account userAccount = this.module.getConomy().getAccountsManager().getAccount(user,this.getCurrency());
                        Account ownerAccount = this.module.getConomy().getAccountsManager().getAccount(this.getOwner(),this.getCurrency());
                        ItemStack item = this.getItem();
                        item.setAmount(this.getAmount());
                        if (checkForPlace(user.getInventory(),item.clone()))
                        {
                            this.module.getConomy().getAccountsManager().transaction(userAccount,ownerAccount,this.getPrice());
                            if (!this.isAdminSign())
                            {
                                this.getInventory().removeItem(item);
                                this.saveToDatabase();
                            } // else admin sign -> no change
                            user.getInventory().addItem(item);
                            user.updateInventory();
                            //TODO buy message
                        }
                        else
                        {
                            user.sendMessage("signmarket","&cYou do not have enough space for these items!");
                        }
                    }
                    else
                    {
                        user.sendMessage("signmarket","&cYou cannot afford the price of these items!");
                    }
                }
                else
                {
                    user.sendMessage("signmarket","&cThis market-sign is &4&lSold Out&c!");
                }
            }
            else
            {
                if (this.isAdminSign() || this.getDemand() - this.getStock() > 0)
                {
                    if (this.isAdminSign() || this.canAfford(this.getOwner()))
                    {
                        if (getAmountOf(user.getInventory(),this.getItem()) >= this.getAmount())
                        {
                            ItemStack item = this.getItem();
                            item.setAmount(this.getAmount());
                            if (this.isAdminSign()
                            || (this.module.getConfig().allowOverStackedInSign
                                    &&  checkForPlace(this.getInventory(),splitIntoMaxItems(item,64)))
                            || (!this.module.getConfig().allowOverStackedInSign
                                    &&  checkForPlace(this.getInventory(),splitIntoMaxItems(item,item.getMaxStackSize()))))
                            {
                                Account userAccount = this.module.getConomy().getAccountsManager().getAccount(user,this.getCurrency());
                                Account ownerAccount = this.module.getConomy().getAccountsManager().getAccount(this.getOwner(),this.getCurrency());
                                this.module.getConomy().getAccountsManager().transaction(ownerAccount,userAccount,this.getPrice());
                                user.getInventory().removeItem(item);
                                if (!this.isAdminSign())
                                {
                                    this.addToInventory(item);
                                    this.saveToDatabase();
                                } // else admin sign -> no change
                                user.updateInventory();
                                //TODO sell message
                            }
                            else
                            {
                                user.sendMessage("signmarket","&cThis market-sign is full and cannot accept more items!");
                            }
                        }
                        else
                        {
                            user.sendMessage("signmarket","&cYou do not have enough items to sell!");
                        }
                    }
                    else
                    {
                        user.sendMessage("signmarket","&cThe owner cannot afford the money to aquire your items!");
                    }
                }
                else
                {
                    user.sendMessage("signmarket","&cThis market-sign is &4&lsatisfied&c! You can no longer sell items to it.");
                }
            }
            return true;
        }
        return false;
    }

    private boolean isValidSign(User user)
    {
        boolean result = true;
        if (this.info.isBuySign == null)
        {
            user.sendMessage("signmarket","&cSign is still in editing mode.");
            result= false;
        }
        if (this.info.amount <= 0)
        {
            user.sendMessage("signmarket","&cInvalid amount!");
            result= false;
        }
        if (this.info.price <= 0)
        {
            user.sendMessage("signmarket","&cInvalid price!");
            result= false;
        }
        if (this.info.item == null)
        {
            user.sendMessage("signmarket","&cNo item given!");
            result= false;
        }
        if (this.info.damageValue == null)
        {
            user.sendMessage("signmarket","&cNo item-data given!");
            result= false;
        }
        if (this.info.currency == null)
        {
            user.sendMessage("signmarket","&cNo currency given!");
            result= false;
        }
        if (this.info.owner == null)
        {
            if (this.info.stock != null)
            {
                user.sendMessage("signmarket","&cAdmin signs have no stock!");
                result= false;
            }
            if (this.info.demand != null)
            {
                user.sendMessage("signmarket","&cAdmin signs have no demand!");
                result= false;
            }
        }
        return result;
    }

    public boolean isOwner(User user)
    {
        return this.info.owner == user.key;
    }

    public void updateSign()
    {
        Block block = this.location.getWorld().getBlockAt(this.location);
        if (block.getState() instanceof Sign)
        {
            Sign blockState = (Sign) block.getState();
            String[] lines = new String[4];
            if (this.getItem().getItemMeta().hasDisplayName() || this.getItem().getItemMeta().hasLore() || !this.getItem().getEnchantments().isEmpty())
            {
                lines[1] = "&e"+Match.material().getNameFor(this.getItem());
            }
            else
            {
                lines[1] = Match.material().getNameFor(this.getItem());
            }
            lines[2] = String.valueOf(this.getAmount());
            if (!this.isAdminSign())
            {
                if (this.isBuySign())
                {
                    if (this.getStock() < this.getAmount() || this.getStock() == 0)
                    {
                        lines[2] += " &4x"+this.getStock();
                    }
                    else
                        lines[2] += " &1x"+this.getStock();
                }
                else
                {
                    User user = this.module.getUserManager().getUser(this.info.owner);
                    if (!this.canAfford(user) || this.getDemand()-this.getStock() <= 0
                    || (this.module.getConfig().allowOverStackedInSign
                            && this.getInventory().firstEmpty() == -1)
                    || (!this.module.getConfig().allowOverStackedInSign
                            && !checkForPlace(this.getInventory(),splitIntoMaxItems(this.getItem(),this.getItem().getMaxStackSize()))))
                    {
                        if (this.getDemand() == null)
                        {
                            lines[2] += " &4x?";
                        }
                        else
                        {
                            lines[2] += " &4x"+(this.getDemand()-this.getStock());
                        }
                    }
                    else
                    {
                        if (this.getDemand() == null)
                        {
                            lines[2] += " &bx?";
                        }
                        else
                        {
                            lines[2] += " &bx"+(this.getDemand()-this.getStock());
                        }
                    }
                }
            }
            lines[3] = this.parsePrice();
            if (this.info.isBuySign == null)
            {
                lines[0] = "&5&lEdit";
            }
            else if (this.info.isBuySign)
            {
                if (!this.isAdminSign() && (this.getStock() < this.getAmount() || this.getStock() == 0))
                {
                    lines[0] = "&4&lSold Out";
                }
                else
                    lines[0] = "&1&lBuy";
            }
            else
            {
                if (this.isAdminSign() || (this.getDemand()-this.getStock() > 0))
                {
                    lines[0] = "&1&lSell";
                }
                else
                {
                    lines[0] = "&4&lsatisfied";
                }
            }
            lines[0] = ChatFormat.parseFormats(lines[0]);
            lines[1] = ChatFormat.parseFormats(lines[1]);
            lines[2] = ChatFormat.parseFormats(lines[2]);
            for (int i=0 ;i<4;++i)
            {
                blockState.setLine(i, lines[i]);
            }
            blockState.update();
        }
        else
        {
            this.module.getLogger().warning("Market-Sign is not a sign-block! "+ location);
        }


        if (this.info.isBuySign)
        {

        }
    }

    private boolean canAfford(User user) {
        return this.module.getConomy().getAccountsManager().getAccount(user, this.getCurrency()).canAfford(this.getPrice());
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
                ItemStack item = this.getItem();
                item.setAmount(this.getAmount());
                inventory.setItem(4, item);
            }
            else
            {
                this.inventory = Bukkit.getServer().createInventory(this, 54, "Market-Sign"); // DOUBLE-CHEST
                if (this.getStock() > 0)
                {
                    ItemStack item = this.getItem();
                    item.setAmount(this.getStock());
                    this.addToInventory(item);
                }
            }
        }
        return this.inventory;
    }

    public int getAmount()
    {
        return this.info.amount;
    }

    public void setInfoModel(SignMarketInfoModel infoModel)
    {
        this.info = infoModel;
    }

    public void setBlockModel(SignMarketBlockModel blockModel)
    {
        this.blockInfo = blockModel;
    }

    public Integer getStock()
    {
        return this.info.stock;
    }

    public void setStock(int stock)
    {
        this.info.stock = stock;
    }

    public boolean isBuySign()
    {
        return this.info.isBuySign;
    }

    public Integer getDemand()
    {
        return this.info.demand;
    }

    public Currency getCurrency()
    {
        if (this.currency == null)
        {
            this.currency =  this.module.getConomy().getCurrencyManager().getCurrencyByName(this.info.currency);
        }
        return this.currency;
    }

    public long getPrice()
    {
        return this.info.price;
    }

    public User getOwner()
    {
        if (this.owner == null && !this.isAdminSign())
        {
            this.owner = this.module.getUserManager().getUser(this.info.owner);
        }
        return this.owner;
    }
}


