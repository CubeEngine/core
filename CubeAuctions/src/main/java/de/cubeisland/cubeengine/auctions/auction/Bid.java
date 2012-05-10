package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.core.persistence.Database;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Represents a bid by a player
 *
 * @author Faithcaio
 */
public class Bid
{
    private int id;

    private final double amount;
    private Bidder bidder;
    private final long timestamp;

    
    private final Database db;

/**
 * creates a bid and add it to DataBase
 */   
    public Bid(Bidder bidder, double amount, Auction auction)
    {
        this.db = CubeAuctions.getInstance().getDB();
        this.amount = amount;
        this.bidder = bidder;
        this.timestamp = System.currentTimeMillis();
        this.id = -1;
        try
        {
            db.exec(
                "INSERT INTO `bids` (`auctionid` ,`bidderid` ,`amount` ,`timestamp`) VALUES ( ?, ?, ?, ?);",
                auction.getId(),
                bidder.getId(),
                amount,
                new Timestamp(System.currentTimeMillis())
            );
            ResultSet set = db.query("SELECT * FROM `bids` WHERE `timestamp`=? && `bidderid`=? LIMIT 1",timestamp,bidder.getId());
            if (set.next())
                this.id = set.getInt("id");
                
        }
        catch (SQLException ex)
        {}
        
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
    public Bid(int id,Bidder bidder, double amount, Timestamp timestamp)
    {
        this.db = CubeAuctions.getInstance().getDB();
        this.amount = amount;
        this.bidder = bidder;
        this.timestamp = timestamp.getTime();
        this.id = bidder.getId();
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
        this.bidder = ServerBidder.getInstance();
        db.execUpdate("UPDATE `bids` SET `bidderid`=? WHERE `id`=?", 
                ServerBidder.getInstance().getId(), this.id);
    }
    
/**
 *  @return TableName for Database
 */ 
    public String getDBTable()
    {
        return "`"+this.getTable()+"`";
    }
}
