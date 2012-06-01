package de.cubeisland.cubeengine.auctions_old.auction;

import de.cubeisland.cubeengine.auctions_old.database.BidStorage;
import de.cubeisland.cubeengine.core.persistence.Model;
import java.sql.Timestamp;

/**
 * Represents a bid by a player
 *
 * @author Faithcaio
 */
public class Bid implements Model<Integer>
{
    private int id;
    private int auctionId;

    private final double amount;
    private Bidder bidder;
    private final long timestamp;
    
    private BidStorage bidDB = new BidStorage ();

/**
 * creates a bid and add it to DataBase
 */   
    public Bid(Bidder bidder, double amount, Auction auction)
    {
        this.auctionId = auction.getKey();
        this.amount = amount;
        this.bidder = bidder;
        this.timestamp = System.currentTimeMillis();
        this.id = -1;
    }
/**
 *  @return TableName in Database
 */ 
    public String getTable()
    {
        return "bids";
    }
    
/**
 *  load in Bid from Database
 */ 
    public Bid(int id,int UserId, int auctionId, double amount, Timestamp timestamp)
    {
        this.bidder = Bidder.getInstance(UserId);
        this.auctionId = auctionId;
        this.amount = amount;
        this.timestamp = timestamp.getTime();
        this.id = id;
    }
    
/**
 *  @return Bid amount
 */ 
    public double getAmount()
    {
        return this.amount;
    }

/**
 *  @return Bidder belonging to this bid
 */ 
    public Bidder getBidder()
    {
        return this.bidder;
    }
/**
 *  @return timestamp as long
 */ 
    public long getTime()
    {
        return this.timestamp;
    }
 
    public Timestamp getTimestamp()
    {
        return new Timestamp(this.timestamp);
    }
    
    public Integer getKey()
    {
        return this.id;
    }
    
    public void setKey(Integer id)
    {
        this.id = id;
    }
/**
 *  give this bid to the Server + update Database
 */     
    public void giveServer()
    {
        this.bidder = Bidder.getInstance(1);
        bidDB.update(this);
    }
    
/**
 *  @return TableName for Database
 */ 
    public String getDBTable()
    {
        return "`"+this.getTable()+"`";
    }

    /**
     * @return the auctionId
     */
    public int getAuctionId()
    {
        return auctionId;
    }
}
