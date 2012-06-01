package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.core.user.User;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class BidderManager
{
    Map<User, Bidder> bidders;

    public BidderManager()
    {
        this.bidders = new HashMap<User, Bidder>();
    }

    public void addBidder(Bidder bidder)
    {
        this.bidders.put(bidder.getUser(), bidder);
    }

    /**
     * try to give Next AuctionItem to the player
     */
    public boolean giveNextItem(User user)
    {
        Bidder bidder = this.bidders.get(user);

        Auction auction = bidder.getFromBox();
        if (auction == null)
        {
            return false;
        }
        ItemStack item = auction.getItem();
        ItemStack remain = user.getPlayer().getInventory().addItem(item).get(0);
        user.updateInventory();
        if ((remain != null) && (remain.getAmount() != 0))
        {
            item.setAmount(remain.getAmount());
            //TODO update in database
        }
        else
        {
            bidder.removeFromBox();
            //TODO delete from Database
        }

        if (auction.isTopBidder(bidder))
        {
            //TODO msg auction was not succesful
        }
        else
        {
            //TODO msg receiving item
        }
        return true;
    }
    
    public void giveAllItems(User user)
    {
        Material fake = null;
        while (user.getInventory().contains(fake))
        {
            this.giveNextItem(user);
        }
    }
}
