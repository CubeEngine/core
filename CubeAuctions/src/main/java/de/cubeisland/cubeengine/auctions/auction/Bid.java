package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.database.BidStorage;
import de.cubeisland.cubeengine.core.persistence.Database;
import java.sql.Timestamp;

/**
 * Represents a bid by a player
 *
 * @author Faithcaio
 */
public class Bid
{
    private int id;
    private int auctionId;

    private final double amount;
    private Bidder bidder;
    private final long timestamp;
    
    private final Database db = CubeAuctions.getDB();
    private BidStorage bidDB = new BidStorage ();

/**
 * creates a bid and add it to DataBase
 */   
    public Bid(Bidder bidder, double amount, Auction auction)
    {
        this.auctionId = auction.getId();
        this.amount = amount;
        this.bidder = bidder;
        this.timestamp = System.currentTimeMillis();
        this.id = bidDB.getNextBidId();
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
    public Bid(int id,int cubeUserId, int auctionId, double amount, Timestamp timestamp)
    {
        this.bidder = Bidder.getInstance(cubeUserId);
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
    
    public int getId()
    {
        return this.id;
    }
/**
 *  give this bid to the Server + update Database
 */     
    public void giveServer()
    {
        bidDB.updateBidder(this, this.bidder);
        this.bidder = Bidder.getInstance(0);
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
