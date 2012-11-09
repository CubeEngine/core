package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.core.persistence.Model;
import java.util.Stack;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Anselm Brehme
 */
public class Auction implements Model<Integer>
{
    private Integer id;
    private Bidder owner;
    private ItemStack item;
    private long auctionEnd;
    private Stack<Bid> bids;

    public Auction(Bidder owner, ItemStack item, long auctionEnd)
    {
        this.owner = owner;
        this.auctionEnd = auctionEnd;
        this.bids = new Stack<Bid>();
    }

    public Auction(Integer key, Bidder owner, ItemStack item, long auctionEnd)
    {
        this.id = key;
        this.owner = owner;
        this.auctionEnd = auctionEnd;
    }

    public void setBids(Stack<Bid> bids)
    {
        this.bids = bids;
    }

    public Integer getKey()
    {
        return this.id;
    }

    public void setKey(Integer key)
    {
        this.id = key;
    }

    /**
     * @return the owner
     */
    public Bidder getOwner()
    {
        return owner;
    }

    /**
     * @return the auctionEnd
     */
    public long getAuctionEnd()
    {
        return auctionEnd;
    }

    /**
     * @return the bids
     */
    public Stack<Bid> getBids()
    {
        return bids;
    }

    public Bid popBid()
    {
        return this.bids.pop();
    }

    public void pushBid(Bid bid)
    {
        this.bids.push(bid);
    }

    public void removeBid(Bid bid)
    {
        this.bids.remove(bid);
    }

    public Bid getTopBid()
    {
        return this.bids.peek();
    }

    public boolean isTopBidder(Bidder bidder)
    {
        return (this.bids.peek().getBidder().equals(bidder));
    }

    public boolean isOwner(Bidder owner)
    {
        return (this.owner.equals(owner));
    }

    /**
     * @return the item
     */
    public ItemStack getItem()
    {
        return item;
    }

    void setEndedTime()
    {
        this.auctionEnd = System.currentTimeMillis();
    }
}
