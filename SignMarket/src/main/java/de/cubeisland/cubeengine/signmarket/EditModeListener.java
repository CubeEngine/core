package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EditModeListener implements Listener
{
    private final Signmarket module;

    private TLongHashSet editModeUsers = new TLongHashSet();
    private TLongObjectHashMap<Location> currentSignLocation = new TLongObjectHashMap<Location>();
    private TLongObjectHashMap<MarketSign> previousMarketSign = new TLongObjectHashMap<MarketSign>();

    public EditModeListener(Signmarket module)
    {
        this.module = module;
    }

    public void enterEditMode(User user)
    {
        this.editModeUsers.add(user.key);
    }

    private void setEditingSign(User user, Location location, MarketSign marketSign)
    {
        Location previous = this.currentSignLocation.put(user.key, location);
        if (!location.equals(previous))
        {
            MarketSign previousSign = this.module.getMarketSignFactory().getSignAt(previous);
            if (previousSign != null)
            {
                this.previousMarketSign.put(user.key, previousSign);
                previousSign.exitEditMode(user);
            }
            user.sendMessage("signmarket", "&aChanged active sign:");
            marketSign.showInfo(user);
        }
        marketSign.enterEditMode();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        User user = this.module.getUserManager().getExactUser(event.getPlayer());
        if (this.editModeUsers.contains(user.key))
        {
            user.sendMessage(String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage()));
            Location loc = this.currentSignLocation.get(user.key);
            if (loc == null)
            {
                if ("exit".equalsIgnoreCase(event.getMessage()))
                {
                    this.editModeUsers.remove(user.key);
                    user.sendMessage("signmarket", "&aEdit mode quit!");
                    event.setCancelled(true);
                    return;
                }
                user.sendMessage("signmarket", "&cPlease do select a sign to edit.");
                event.setCancelled(true);
                return;
            }
            String[] splitted = StringUtils.explode(" ", event.getMessage());
            MarketSign marketSign = this.module.getMarketSignFactory().getSignAt(loc);
            if (marketSign == null)
            {
                user.sendMessage("signmarket", "&cNo market-sign at position.");
                event.setCancelled(true);
                return;
            }
            this.setEditingSign(user, loc, marketSign);

            this.currentSignLocation.put(user.key, loc);
            for (int i = 0; i < splitted.length; ++i)
            {
                //TODO permissions
                if ("buy".equalsIgnoreCase(splitted[i]))
                {
                    marketSign.setBuy();
                }
                else if ("sell".equalsIgnoreCase(splitted[i]))
                {
                    marketSign.setSell();
                }
                else if ("demand".equalsIgnoreCase(splitted[i]))
                {
                    if (marketSign.isBuySign() == null)
                    {
                        marketSign.setSell();
                    }
                    if (marketSign.isBuySign())
                    {
                        user.sendMessage("signmarket", "&cBuy signs cannot have a demand!");
                        continue;
                    }
                    if (marketSign.isAdminSign())
                    {
                        user.sendMessage("signmarket", "&cAdmin signs cannot have a demand!");
                        continue;
                    }
                    if (++i >= splitted.length)
                    {
                        user.sendMessage("signmarket", "&cMissing demand amount!");
                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        try
                        {
                            marketSign.setDemand(Integer.parseInt(splitted[i]));
                        }
                        catch (NumberFormatException e)
                        {
                            user.sendMessage("signmarket", "&cInvalid demand amount!");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                else if ("admin".equalsIgnoreCase(splitted[i]))
                {
                    marketSign.setAdminSign();
                }
                else if ("user".equalsIgnoreCase(splitted[i]))
                {
                    marketSign.setOwner(user);
                }
                else if ("owner".equalsIgnoreCase(splitted[i]))
                {
                    if (++i >= splitted.length)
                    {
                        user.sendMessage("signmarket", "&cMissing owner-name!");
                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        User owner = this.module.getUserManager().findUser(splitted[i]);
                        if (owner == null)
                        {
                            user.sendMessage("signmarket", "&cUser %s not found!", splitted[i]);
                        }
                        else
                        {
                            marketSign.setOwner(owner);
                        }
                    }
                }
                else if ("price".equalsIgnoreCase(splitted[i]))
                {
                    if (++i >= splitted.length)
                    {
                        user.sendMessage("signmarket", "&cMissing price!");
                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        if (marketSign.getCurrency() == null) // no currency set yet -> try to match
                        {
                            marketSign.setCurrency(this.module.getConomy().getCurrencyManager().matchCurrency(splitted[i]));
                        }
                        Long price = marketSign.getCurrency().parse(splitted[i]);
                        if (price == null)
                        {
                            user.sendMessage("signmarket", "&cInvalid price for currency!");
                        }
                        else
                        {
                            marketSign.setPrice(price);
                        }
                    }
                }
                else if ("amount".equalsIgnoreCase(splitted[i]))
                {
                    if (++i >= splitted.length)
                    {
                        user.sendMessage("signmarket", "&cMissing amount!");
                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        try
                        {
                            marketSign.setAmount(Integer.parseInt(splitted[i]));
                        }
                        catch (NumberFormatException e)
                        {
                            user.sendMessage("signmarket", "&cInvalid amount!");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                else if ("item".equalsIgnoreCase(splitted[i]))
                {
                    if (++i >= splitted.length)
                    {
                        user.sendMessage("signmarket", "&cMissing item!");
                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        ItemStack item = Match.material().itemStack(splitted[i]);
                        if (item == null)
                        {
                            user.sendMessage("signmarket", "&cItem not found!");
                        }
                        else
                        {
                            marketSign.setItemStack(item, false);
                        }
                    }
                }
                else if ("copy".equalsIgnoreCase(splitted[i]))
                {
                    MarketSign prevMarketSign = this.previousMarketSign.get(user.key);
                    if (prevMarketSign == null)
                    {
                        user.sendMessage("signmarket", "&cNo market-sign at previous position.");
                        continue;
                    }
                    marketSign.applyAllValues(prevMarketSign);
                }
                else if ("exit".equalsIgnoreCase(splitted[i]))
                {
                    this.editModeUsers.remove(user.key);
                    this.previousMarketSign.put(user.key, marketSign);
                    this.currentSignLocation.remove(user.key);
                    marketSign.exitEditMode(user);
                    user.sendMessage("signmarket", "&aEdit mode quit!");
                    event.setCancelled(true);
                    return;
                }
                else
                {
                    user.sendMessage("signmarket", "&cInvalid command! " + splitted[i]);
                    //TODO print list of possible cmds!
                }
            }
            marketSign.showInfo(user);
            marketSign.updateSign();
            event.setCancelled(true);
        }
        for (long userKey : this.editModeUsers.toArray())
        {
            User ignoreChatUser = this.module.getUserManager().getUser(userKey);
            event.getRecipients().remove(ignoreChatUser.getPlayer());
            //TODO save chat for this player (up to ~50lines)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(PlayerInteractEvent event)
    {
        if (event.useItemInHand().equals(Event.Result.DENY))
            return;
        if (event.getPlayer().isSneaking())
            return;
        User user = this.module.getUserManager().getExactUser(event.getPlayer());
        if (!this.editModeUsers.contains(user.key))
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
                MarketSign marketSign = this.module.getMarketSignFactory().getSignAt(newLoc);
                if (marketSign == null)
                {
                    marketSign = this.module.getMarketSignFactory().createSignAt(newLoc);
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
            MarketSign curSign = this.module.getMarketSignFactory().getSignAt(curLoc);
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
            User user = this.module.getUserManager().getExactUser(event.getPlayer());
            if (this.editModeUsers.contains(user.key))
            {
                Location loc = event.getBlockPlaced().getLocation();
                MarketSign marketSign = this.module.getMarketSignFactory().createSignAt(loc);
                this.setEditingSign(user, loc, marketSign);
                marketSign.updateSign();
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        User user = this.module.getUserManager().getExactUser(event.getPlayer());
        if (this.editModeUsers.contains(user.key))
        {
            Location loc = event.getBlock().getLocation();
            if (loc.equals(this.currentSignLocation.get(user.key)))
                event.setCancelled(true);
        }
    }
}
