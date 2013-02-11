package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.InventoryUtil;
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

public class MarketSign implements InventoryHolder
{
    private final Signmarket module;
    private final Location location;
    private SignMarketInfoModel info;
    private SignMarketBlockModel blockInfo;
    
    private TLongLongHashMap breakingSign = new TLongLongHashMap();

    private Inventory inventory = null;

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
        if (this.inventory != null)
        {
            this.info.stock = InventoryUtil.getAmountOf(this.inventory, this.getItem());

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
        if (!this.isAdminSign() && true)//TODO config if do drop
        {
            if (this.getStock() > 0)
            {
                ItemStack item = this.getItem();
                item.setAmount(this.getStock());
                this.location.getWorld().dropItemNaturally(location,item);
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
                    if (user.getGameMode().equals(GameMode.CREATIVE)) // instabreak items
                    {
                        if (this.isOwner(user))
                        {
                            if (MarketSignPerm.SIGN_DESTROY_OWN.isAuthorized(user))
                            {
                                if (this.getStock() == 1337) //pssst i am not here
                                {
                                    this.location.getWorld().strikeLightningEffect(location);
                                }
                                this.breakSign();
                                return false;
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
                                this.breakSign();
                                return false;
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
                                this.breakSign();
                                return false;
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
                                tryBreak(user);
                            }
                            else
                            {
                                user.sendMessage("signmarket", "&cYou are not allowed to break admin-signs!");
                            }
                        }
                        else if (this.isOwner(user))
                        {
                            if (this.info.stock > 0)
                            {
                                this.takeItems(user);
                            }
                            else if (this.info.stock == 0)
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
                        }
                        else // not owner / not admin
                        {
                            if (this.info.stock > 0)
                            {
                                if (MarketSignPerm.SIGN_INVENTORY_TAKE_OTHER.isAuthorized(user))
                                {
                                    this.takeItems(user);
                                }
                                else
                                {
                                    user.sendMessage("signmarket","&cYou are not allowed to destroy others market-signs!");
                                }
                            }
                            else if (this.info.stock == 0)
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
                }
                return true;
            case RIGHT_CLICK_BLOCK:
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
                    user.sendMessage("signmarket", "--------- &6Sign Market &f---------");
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
                            user.sendMessage("signmarket", "&&3Sell: &6%d &ffor &6%s &fto &6%s",this.getAmount(),this.parsePrice(),"Server");
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
                else
                {
                    if (this.isOwner(user))
                    {
                        if (this.info.isBuySign)
                        {
                            if (this.info.matchesItem(itemInHand))
                            {
                                int amount = this.putItems(user, false);
                                if (amount != 0)
                                    user.sendMessage("signmarket","&aAdded &6%d&ax &6%s &ato the stock!",amount, Match.material().getNameFor(this.info.getItem()));
                                return true;
                            }
                            else if (itemInHand != null && itemInHand.getTypeId() != 0)
                            {
                                user.sendMessage("signmarket","&cWrong item to put into the market-sign!");
                                return true;
                            }
                        }
                        user.sendMessage("signmarket","&eThis is your own sign!");
                        return true;
                    }
                    return this.useSign(user);
                }
        }
        return false;
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
            amount = InventoryUtil.getAmountOf(user.getInventory(),user.getItemInHand());
        }
        else
        {
            amount = user.getItemInHand().getAmount();
        }
        this.info.stock = this.info.stock + amount;
        ItemStack item = this.getItem();
        item.setAmount(amount);
        user.getInventory().removeItem(item);
        Map<Integer,ItemStack> additional = this.getInventory().addItem(item);
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

    private void takeItems(User user) {
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
        this.getInventory().removeItem(item);
        Map<Integer,ItemStack> additional = user.getInventory().addItem(item);
        int amountGivenBack = 0;
        for (ItemStack itemStack : additional.values())
        {
            amountGivenBack += itemStack.getAmount();
            this.getInventory().addItem(itemStack);
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
        user.sendMessage("Not implemented yet!");
        return false; //TODO sell / buy
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
                    if (!this.canAfford(user) || this.getDemand()-this.getStock() <= 0)
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
                if (this.getDemand()-this.getStock() <= 0)
                {
                    lines[0] = "&4&lsatisfied";
                }
                else
                    lines[0] = "&1&lSell";
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
                    inventory.addItem(item);
                }
            }
        }
        return this.inventory;
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

    public Integer getStock() {
        return this.info.stock;
    }

    public void setStock(int stock) {
        this.info.stock = stock;
    }

    public boolean isBuySign() {
        return this.info.isBuySign;
    }

    public Integer getDemand() {
        return this.info.demand;
    }

    public Currency getCurrency() {
        return this.module.getConomy().getCurrencyManager().getCurrencyByName(this.info.currency);
    }

    public long getPrice() {
        return this.info.price;
    }
}


