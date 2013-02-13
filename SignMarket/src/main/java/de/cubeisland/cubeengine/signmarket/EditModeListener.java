package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EditModeListener implements Listener
{
    private final Signmarket module;

    private TLongHashSet editModeUsers = new TLongHashSet();
    private TLongObjectHashMap<Location> currentSignLocation = new TLongObjectHashMap<Location>();
    private TLongObjectHashMap<Location> previousSignLocation = new TLongObjectHashMap<Location>();

    private ConversationFactory conversationFactory;

    public EditModeListener(Signmarket module) {
        this.module = module;
        this.conversationFactory = new ConversationFactory((BukkitCore)this.module.getCore());
    }

    public void enterEditMode(User user)
    {
        this.editModeUsers.add(user.key);
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
                    user.sendMessage("marketsign", "&aEdit mode quit!");
                    event.setCancelled(true);
                    return;
                }
                user.sendMessage("marketsign","&cPlease do select a sign to edit.");
                event.setCancelled(true);
                return;
            }
            String[] splitted = StringUtils.explode(" ",event.getMessage());
            MarketSign marketSign = this.module.getMarketSignFactory().getSignAt(loc);
            if (marketSign == null)
            {
                user.sendMessage("marketsign","&cNo market-sign at position.");
                event.setCancelled(true);
                return;
            }
            marketSign.enterEditMode();
            for (int i = 0; i < splitted.length; ++i)
            {
                //TODO permissions
                if ("buy".equalsIgnoreCase(splitted[i])) {
                    marketSign.setBuy();
                }
                else if ("sell".equalsIgnoreCase(splitted[i])) {
                    marketSign.setSell();
                }
                else if ("demand".equalsIgnoreCase(splitted[i])) {
                    if (++i >= splitted.length)
                    {
                        user.sendMessage("marketsign","&cMissing demand amount!");
                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        try {
                            marketSign.setDemand(Integer.parseInt(splitted[i]));
                        }
                        catch (NumberFormatException e)
                        {
                            user.sendMessage("marketsign","&cInvalid demand amount!");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                else if ("admin".equalsIgnoreCase(splitted[i])) {
                    marketSign.setAdminSign();
                }
                else if ("user".equalsIgnoreCase(splitted[i])) {
                    marketSign.setOwner(user);
                }
                else if ("owner".equalsIgnoreCase(splitted[i])) {
                    if (++i >= splitted.length)
                    {
                        user.sendMessage("marketsign","&cMissing owner-name!");
                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        User owner = this.module.getUserManager().findUser(splitted[i]);
                        if (owner == null)
                        {
                            user.sendMessage("marketsign","&cUser %s not found!",splitted[i]);
                        }
                        else
                        {
                            marketSign.setOwner(owner);
                        }
                    }
                }
                else if ("price".equalsIgnoreCase(splitted[i])) {
                    if (++i >= splitted.length)
                    {
                        user.sendMessage("marketsign","&cMissing price!");
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
                            user.sendMessage("marketsign","&cInvalid price for currency!");
                        }
                        else
                        {
                            marketSign.setPrice(price);
                        }
                    }
                }
                else if ("amount".equalsIgnoreCase(splitted[i])) {
                    if (++i >= splitted.length)
                    {
                        user.sendMessage("marketsign","&cMissing amount!");
                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        try {
                            marketSign.setAmount(Integer.parseInt(splitted[i]));
                        }
                        catch (NumberFormatException e)
                        {
                            user.sendMessage("marketsign","&cInvalid amount!");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                else if ("item".equalsIgnoreCase(splitted[i])) {
                    if (++i >= splitted.length)
                    {
                        user.sendMessage("marketsign","&cMissing item!");
                        event.setCancelled(true);
                        return;
                    }
                    else
                    {
                        ItemStack item = Match.material().itemStack(splitted[i]);
                        if (item == null)
                        {
                            user.sendMessage("marketsign","&cItem not found!");
                        }
                        else
                        {
                            marketSign.setItemStack(item, false);
                        }
                    }
                }
                else if ("copy".equalsIgnoreCase(splitted[i])) {
                    Location prevLoc = this.previousSignLocation.get(user.key);
                    if (prevLoc == null)
                    {
                        user.sendMessage("marketsign","&cNo previous sign found!");
                        continue;
                    }
                    MarketSign prevMarketSign = this.module.getMarketSignFactory().getSignAt(loc);
                    if (prevMarketSign == null)
                    {
                        user.sendMessage("marketsign","&cNo market-sign at previous position.");
                        continue;
                    }
                    marketSign.applyAllValues(prevMarketSign);
                }
                else if ("exit".equalsIgnoreCase(splitted[i]))
                {
                    this.editModeUsers.remove(user.key);
                    this.previousSignLocation.put(user.key,this.currentSignLocation.remove(user.key));
                    marketSign.exitEditMode();
                    user.sendMessage("marketsign", "&aEdit mode quit!");
                    event.setCancelled(true);
                    return;
                }
                else
                {
                    user.sendMessage("marketsign","&cInvalid command!");
                    //TODO print list of possible cmds!
                }
                marketSign.showInfo(user);
            }
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

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            if (event.getClickedBlock().getState() instanceof Sign)
            {
                User user = this.module.getUserManager().getExactUser(event.getPlayer());
                if (!this.editModeUsers.contains(user.key))
                {
                    return;
                }
                event.setCancelled(true);
                Location oldLocation = this.currentSignLocation.get(user.key);
                if (oldLocation != null && event.getClickedBlock().getLocation().equals(oldLocation))
                    return;
                MarketSign oldMarketSign = this.module.getMarketSignFactory().getSignAt(oldLocation);
                if (oldMarketSign != null)
                {
                    oldMarketSign.exitEditMode();
                    oldMarketSign.isValidSign(user);
                    this.previousSignLocation.put(user.key,this.currentSignLocation.remove(user.key));
                }
                MarketSign marketSign = this.module.getMarketSignFactory().getSignAt(event.getClickedBlock().getLocation());
                if (marketSign == null)
                {
                    marketSign = this.module.getMarketSignFactory().createSignAt(event.getClickedBlock().getLocation());
                }
                if (marketSign.isInEditMode())
                {
                    user.sendMessage("marketsign","&cThis sign is beeing edited right now!");
                    return;
                }
                marketSign.enterEditMode();
                this.currentSignLocation.put(user.key,event.getClickedBlock().getLocation());
                user.sendMessage("marketsign","&aChanged active sign:");
                marketSign.showInfo(user);
            }
        }
    }
}
