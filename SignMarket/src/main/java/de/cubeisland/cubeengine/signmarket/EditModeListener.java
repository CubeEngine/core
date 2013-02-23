package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.conomy.currency.Currency;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.chatcommand.ChatCommand;
import de.cubeisland.cubeengine.core.command.chatcommand.ChatCommandContext;
import de.cubeisland.cubeengine.core.command.chatcommand.ChatCommandContextFactory;
import de.cubeisland.cubeengine.core.command.parameterized.CommandFlag;
import de.cubeisland.cubeengine.core.command.parameterized.CommandParameter;
import de.cubeisland.cubeengine.core.user.User;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EditModeListener extends ChatCommand<Signmarket>
{
    public EditModeListener(Signmarket module) {
        super(module, new ChatCommandContextFactory());
        this.getContextFactory()
                .addFlag(new CommandFlag("exit", "exit"))
                .addFlag(new CommandFlag("copy", "copy"))
                .addFlag(new CommandFlag("buy", "buy"))
                .addFlag(new CommandFlag("sell","sell"))
                .addFlag(new CommandFlag("admin","admin"))
                .addFlag(new CommandFlag("user","user"))
                .addFlag(new CommandFlag("stock","stock"))
                .addParameter(new CommandParameter("demand", Integer.class))
                .addParameter(new CommandParameter("owner", User.class))
                .addParameter(new CommandParameter("price", String.class))
                .addParameter(new CommandParameter("amount", Integer.class))
                .addParameter(new CommandParameter("item", ItemStack.class))
                .addParameter(new CommandParameter("setstock",Integer.class))
                .addParameter(new CommandParameter("size",Integer.class))
                .addParameter(new CommandParameter("currency",String.class))
        ;
    }
//TODO itemblacklist?
    private TLongObjectHashMap<Location> currentSignLocation = new TLongObjectHashMap<Location>();
    private TLongObjectHashMap<MarketSign> previousMarketSign = new TLongObjectHashMap<MarketSign>();

    private void setEditingSign(User user, Location location, MarketSign marketSign)
    {
        Location previous = this.currentSignLocation.put(user.key, location);
        if (!location.equals(previous))
        {
            MarketSign previousSign = this.getModule().getMarketSignFactory().getSignAt(previous);
            if (previousSign != null)
            {
                this.previousMarketSign.put(user.key, previousSign);
                previousSign.exitEditMode(user);
            }
            user.sendMessage("signmarket", "&aChanged active sign!");
            marketSign.updateSign();
        }
        marketSign.enterEditMode();
    }

    @Override
    public void removeUser(User user)
    {
        super.removeUser(user);
        MarketSign marketSign = this.getModule().getMarketSignFactory().getSignAt(this.currentSignLocation.remove(user.key));
        if (marketSign != null)
        {
            marketSign.exitEditMode(user);
        }
        user.sendMessage("signmarket", "&aEdit mode quit!");
    }

    public CommandResult run(CommandContext runContext) throws Exception
    {
        User user = (User)runContext.getSender();
        ChatCommandContext context = (ChatCommandContext) runContext;
        Location loc = this.currentSignLocation.get(user.key);
        if (loc == null)
        {
            if (context.hasFlag("exit"))
            {
                this.removeUser(user);
                return null;
            }
            user.sendMessage("signmarket", "&cPlease do select a sign to edit.");
            return null;
        }
        MarketSign marketSign = this.getModule().getMarketSignFactory().getSignAt(loc);
        if (marketSign == null)
        {
            user.sendMessage("signmarket", "&4No market-sign at position! This should not happen!");
            return null;
        }
        this.setEditingSign(user, loc, marketSign);
        if (context.hasFlag("copy"))
        {
            MarketSign prevMarketSign = this.previousMarketSign.get(user.key);
            if (prevMarketSign == null)
            {
                user.sendMessage("signmarket", "&cNo market-sign at previous position.");
            }
            else
            {
                marketSign.applyValues(prevMarketSign);
            }
        }
        if (context.hasFlag("buy"))
        {
            if (marketSign.isAdminSign())
            {
                if (MarketSignPerm.SIGN_CREATE_ADMIN_BUY.isAuthorized(user))
                {
                    marketSign.setBuy();
                }
                else
                {
                    context.sendMessage("signmarket","&cYou are not allowed to create admin-buy signs!");
                }
            }
            else
            {
                if (MarketSignPerm.SIGN_CREATE_USER_BUY.isAuthorized(user))
                {
                    marketSign.setBuy();
                }
                else
                {
                    context.sendMessage("signmarket","&cYou are not allowed to create user-buy signs!");
                }
            }
        }
        if (context.hasFlag("sell"))
        {
            if (marketSign.isAdminSign())
            {
                if (MarketSignPerm.SIGN_CREATE_ADMIN_SELL.isAuthorized(user))
                {
                    marketSign.setSell();
                }
                else
                {
                    context.sendMessage("signmarket","&cYou are not allowed to create admin-sell signs!");
                }
            }
            else
            {
                if (MarketSignPerm.SIGN_CREATE_USER_SELL.isAuthorized(user))
                {
                    marketSign.setSell();
                }
                else
                {
                    context.sendMessage("signmarket","&cYou are not allowed to create user-sell signs!");
                }
            }
        }
        if (context.hasParam("demand"))
        {
            if (marketSign.isBuySign() == null)
            {
                marketSign.setSell();
            }
            if (marketSign.isBuySign())
            {
                user.sendMessage("signmarket", "&cBuy signs cannot have a demand!");
            }
            else if (marketSign.isAdminSign())
            {
                user.sendMessage("signmarket", "&cAdmin signs cannot have a demand!");
            }
            else
            {
                Integer demand = context.getParam("demand",null);
                if (demand == null || demand > 0)
                {
                    marketSign.setDemand(demand);
                }
                else
                {
                    context.sendMessage("signmarket","&cInvalid demand amount!");
                }
            }
        }
        if (context.hasFlag("admin"))
        {
            if (MarketSignPerm.SIGN_CREATE_ADMIN.isAuthorized(user))
            {
                marketSign.setAdminSign();
            }
            else
            {
                context.sendMessage("signmarket","&cYou are not allowed to create admin-signs");
            }
        }
        if (context.hasFlag("user"))
        {
            if (MarketSignPerm.SIGN_CREATE_USER.isAuthorized(user))
            {
                marketSign.setOwner(user);
            }
            else
            {
                context.sendMessage("signmarket","&cYou are not allowed to create user-signs");
            }
        }
        if (context.hasParam("owner"))
        {
            if (MarketSignPerm.SIGN_CREATE_USER_OTHER.isAuthorized(user))
            {
                User owner = context.getParam("owner",null);
                if (owner == null)
                {
                    user.sendMessage("signmarket", "&cUser %s not found!",context.getString("owner"));
                }
                else
                {
                    marketSign.setOwner(owner);
                }
            }
            else
            {
                context.sendMessage("signmarket","&cYou are not allowed to create user-signs for other users");
            }
        }
        if (context.hasFlag("stock"))
        {
            if (marketSign.isAdminSign())
            {
                if (marketSign.hasStock())
                {
                    if (this.getModule().getConfig().allowAdminNoStock)
                    {
                        if (MarketSignPerm.SIGN_CREATE_ADMIN_NOSTOCK.isAuthorized(user))
                        {
                            marketSign.setStock(null);
                        }
                        else
                        {
                            context.sendMessage("signmarket","&cYou are not allowed to create admin-signs with no stock");
                        }
                    }
                    else
                    {
                        context.sendMessage("signmarket","&cAdmin-signs without stock are not allowed!");
                    }
                }
                else
                {
                    if (this.getModule().getConfig().allowAdminStock)
                    {
                        if (MarketSignPerm.SIGN_CREATE_ADMIN_STOCK.isAuthorized(user))
                        {
                            marketSign.setStock(0);
                        }
                        else
                        {
                            context.sendMessage("signmarket","&cYou are not allowed to create admin-signs with stock");
                        }
                    }
                    else
                    {
                        context.sendMessage("signmarket","&cAdmin-signs with stock are not allowed!");
                    }
                }
            }
            else
            {
                context.sendMessage("signmarket","&cUser signs cannot have no stock!");
            }
        }
        if (context.hasParam("setstock"))
        {
            if (MarketSignPerm.SIGN_SETSTOCK.isAuthorized(user))
            {
                if (marketSign.hasStock())
                {
                    marketSign.setStock(context.getParam("setstock",0));
                }
                else
                {
                    context.sendMessage("signmarket","&cThis sign has no stock! Use \"stock\" first to enable it!");
                }
            }
            else
            {
                context.sendMessage("signmarket","&cYou are not allowed to set the stock!");
            }
        }
        if (context.hasParam("currency"))
        {
            Currency currency = this.getModule().getConomy().getCurrencyManager().getCurrencyByName(context.getString("currency"));
            if (currency == null)
            {
                context.sendMessage("signmarket","&cInvalid currency: %s!",context.getString("currency"));
            }
            else
            {
                marketSign.setCurrency(currency);
            }
        }

        if (context.hasParam("price"))
        {
            Currency currency = marketSign.getCurrency();
            if (currency == null)
            {
                currency = this.getModule().getConomy().getCurrencyManager().getMainCurrency();
                marketSign.setCurrency(currency);
                context.sendMessage("signmarket","&aCurrency set to default!");
            }
            Long price = currency.parse(context.getString("price"));
            if (price == null)
            {
                user.sendMessage("signmarket", "&cInvalid price for currency!");
                marketSign.setPrice(0);
            }
            else if (price < 0)
            {
                user.sendMessage("signmarket", "&cA negative price!? Are you serious?");
            }
            else
            {
                marketSign.setPrice(price);
            }
        }
        if (context.hasParam("amount"))
        {
            Integer amount = context.getParam("amount",null);
            if (amount == null)
            {
                user.sendMessage("signmarket", "&cInvalid amount %s!",context.getString("amount"));
            }
            else if (amount < 0)
            {
                user.sendMessage("signmarket", "&cNegative amounts could be unfair!");
            }
            else
            {
                marketSign.setAmount(amount);
            }
        }
        if (context.hasParam("item"))
        {
            ItemStack item = context.getParam("item", null);
            if (item == null)
            {
                user.sendMessage("signmarket", "&cItem not found!");
            }
            else
            {
                marketSign.setItemStack(item, false);
            }
        }
        if (context.hasParam("size"))
        {
            if (MarketSignPerm.SIGN_SIZE_CHANGE.isAuthorized(user))
            {
                Integer size = context.getParam("size",null);
                if (size == null || size == 0 || size > 6 || size < -1)
                {
                    context.sendMessage("signmarket","&cInvalid size! Use -1 for infinite OR 1-6 inventory-lines!");
                }
                else
                {
                    if (size == -1 && !MarketSignPerm.SIGN_SIZE_CHANGE_INFINITE.isAuthorized(user))
                    {
                        context.sendMessage("marketsign","&cYou are not allowed to set infinite inventories!");
                    }
                    else
                    {
                        marketSign.setSize(size);
                    }
                }
            }
            else
            {
                context.sendMessage("signmarket","&cYou are not allowed to change the sign-inventory-size.");
            }
        }
        if (context.hasFlag("exit"))
        {
            this.removeUser(user);
            this.previousMarketSign.put(user.key, marketSign);
            this.currentSignLocation.remove(user.key);
            marketSign.exitEditMode(user);
            return null;
        }
        marketSign.showInfo(user);
        marketSign.updateSign();
        return null;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(PlayerInteractEvent event)
    {
        if (event.useItemInHand().equals(Event.Result.DENY))
            return;
        if (event.getPlayer().isSneaking())
            return;
        User user = this.getModule().getUserManager().getExactUser(event.getPlayer());
        if (!this.hasUser(user))
        {
            return;
        }
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            if (event.getClickedBlock().getState() instanceof Sign)
            {
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                Location newLoc = event.getClickedBlock().getLocation();
                if (!newLoc.equals(this.currentSignLocation.get(user.key)))
                {
                    if (this.currentSignLocation.valueCollection().contains(newLoc))
                    {
                        user.sendMessage("signmarket", "&cSomeone else is editing this sign!");
                        return;
                    }
                }
                MarketSign marketSign = this.getModule().getMarketSignFactory().getSignAt(newLoc);
                if (marketSign == null)
                {
                    if (user.isSneaking())
                    {
                        event.setUseInteractedBlock(Event.Result.DEFAULT);
                        event.setUseItemInHand(Event.Result.DEFAULT);
                        return;
                    }
                    user.sendMessage("signmarket","&cThis is not a market-sign!\n&eUse shift leftclick to destroy the sign.");
                    return;
                }
                if (marketSign.isInEditMode())
                {
                    if (marketSign.tryBreak(user))
                    {
                        this.previousMarketSign.put(user.key, marketSign);
                        this.currentSignLocation.remove(user.key);
                    }
                    return;
                }
                if (!MarketSignPerm.SIGN_EDIT.isAuthorized(user))
                {
                    user.sendMessage("signmarket","&cYou are not allowed to edit market-signs!");
                    return;
                }
                this.setEditingSign(user, newLoc, marketSign);
            }
        }
        else
        {
            BlockState signFound = null;
            if (event.getAction().equals(Action.RIGHT_CLICK_AIR))
            {
                if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getTypeId() != 0)
                {
                    signFound = MarketSignListener.getTargettedSign(event.getPlayer());
                }
            }
            else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getState() instanceof Sign)
            {
                signFound = event.getClickedBlock().getState();
            }
            if (signFound == null)
            {
                return;
            }
            if (user.getItemInHand() == null || user.getItemInHand().getTypeId() == 0)
                return;
            Location curLoc = signFound.getLocation();
            MarketSign curSign = this.getModule().getMarketSignFactory().getSignAt(curLoc);
            if (curSign == null)
            {
                user.sendMessage("signmarket", "&eThis sign is not a market-sign!");
                return; // not a market-sign
            }
            //TODO prevent changing if user-sign and items in stock! OR take out all items

            this.setEditingSign(user, curLoc, curSign);
            curSign.setItemStack(user.getItemInHand(), true);
            curSign.updateSign();
            user.sendMessage("signmarket", "&aItem in sign updated!");
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onSignPlace(BlockPlaceEvent event)
    {
        if (event.getBlockPlaced().getState() instanceof Sign)
        {
            User user = this.getModule().getUserManager().getExactUser(event.getPlayer());
            if (this.hasUser(user))
            {
                if (!MarketSignPerm.SIGN_CREATE.isAuthorized(user))
                {
                    user.sendMessage("signmarket","&cYou are not allowed to create market-signs!");
                    event.setCancelled(true);
                    return;
                }
                Location loc = event.getBlockPlaced().getLocation();
                MarketSign marketSign = this.getModule().getMarketSignFactory().createSignAt(user, loc);
                this.setEditingSign(user, loc, marketSign);
                marketSign.updateSign();
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        User user = this.getModule().getUserManager().getExactUser(event.getPlayer());
        if (this.hasUser(user))
        {
            Location loc = event.getBlock().getLocation();
            if (loc.equals(this.currentSignLocation.get(user.key)))
                event.setCancelled(true);
        }
    }
}
