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
        //TODO change currency
        //TODO setStock
        ;
    }

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
                marketSign.applyAllValues(prevMarketSign);
            }
        }
        if (context.hasFlag("buy"))
        {
            marketSign.setBuy();
        }
        if (context.hasFlag("sell"))
        {
            marketSign.setSell();
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
                    context.sendMessage("signmarket","&cNo demand? So why that sign then?");
                }
            }
        }
        if (context.hasFlag("admin"))
        {
            marketSign.setAdminSign();
        }
        if (context.hasFlag("user"))
        {
            marketSign.setOwner(user);
        }
        if (context.hasParam("owner"))
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
        if (context.hasFlag("stock"))
        {
            if (marketSign.hasStock())
            {
                marketSign.setStock(null);
            }
            else
            {
                marketSign.setStock(0);
            }
        }
        if (context.hasParam("price"))
        {
            Currency currency = marketSign.getCurrency();
            if (currency == null)
            {
                currency = this.getModule().getConomy().getCurrencyManager().getMainCurrency();
                marketSign.setCurrency(currency);
                context.sendMessage("signmarkte","&aCurrency set to default!");
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
                    marketSign = this.getModule().getMarketSignFactory().createSignAt(newLoc);
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
                Location loc = event.getBlockPlaced().getLocation();
                MarketSign marketSign = this.getModule().getMarketSignFactory().createSignAt(loc);
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
