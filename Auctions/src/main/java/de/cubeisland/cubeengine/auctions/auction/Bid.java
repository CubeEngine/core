package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.core.persistence.Model;

public class Bid implements Model<Integer>
{
    private Integer key;
    private final Auction auction;
    private final double amount;
    private final Bidder bidder;
    private final long timestamp;

    public Bid(Bidder bidder, Auction auction, double amount)
    {
        this.bidder = bidder;
        this.auction = auction;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    public Bid(Integer key, Bidder bidder, Auction auction, double amount, long timestamp)
    {
        this.bidder = bidder;
        this.auction = auction;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public Integer getKey()
    {
        return this.key;
    }

    public void setKey(Integer key)
    {
        this.key = key;
    }

    /**
     * @return the auction
     */
    public Auction getAuction()
    {
        return auction;
    }

    /**
     * @return the amount
     */
    public double getAmount()
    {
        return amount;
    }

    /**
     * @return the bidder
     */
    public Bidder getBidder()
    {
        return bidder;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp()
    {
        return timestamp;
    }
}
