package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.bitmask.ByteBitMask;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public class Bidder implements Model<Integer>
{
    public static final byte NOTIFY_ITEMS = 4;
    public static final byte NOTIFY_CANCEL = 2;
    public static final byte NOTIFY_WIN = 1;
    private Integer userId;
    private User user;
    private ByteBitMask notifyState;
    private Queue<Auction> auctionBox;
    private AuctionManager auctionManager;

    public Bidder(User user)
    {
        this.userId = user.getKey();
        this.user = user;
        this.notifyState = new ByteBitMask();
        this.auctionBox = new LinkedTransferQueue<Auction>();
        //TODO store bidder in db
    }

    public Bidder(Integer userId, byte notifyState)
    {
        this.userId = userId;
        this.notifyState = new ByteBitMask(notifyState);
        this.auctionBox = new LinkedTransferQueue<Auction>();
    }

    public Integer getKey()
    {
        return this.userId;
    }

    public void setKey(Integer key)
    {
        throw new UnsupportedOperationException("Not needed PrimaryKey is a foreign key.");
    }

    /**
     * @return the notifyState
     */
    public ByteBitMask getNotifyState()
    {
        return notifyState;
    }

    /**
     * @return the user
     */
    public User getUser()
    {
        return user;
    }

    public void addToBox(Auction auction)
    {
        this.auctionBox.offer(auction);
    }

    public Auction getFromBox()
    {
        return this.auctionBox.peek();
    }

    public void removeFromBox()
    {
        this.auctionBox.poll();
    }

    /**
     * @return Total amount of money spend in leading bids
     */
    public double getTotalBidAmount()
    {
        double total = 0;
        List<Auction> auctionlist = this.auctionManager.getAuctionsOf(this);
        if (!auctionlist.isEmpty())
        {
            for (Auction auction : auctionlist)
            {
                total += auction.getTopBid().getAmount();
            }
        }
        return total;
    }

    public Queue<Auction> getBox()
    {
        return this.auctionBox;
    }
}
