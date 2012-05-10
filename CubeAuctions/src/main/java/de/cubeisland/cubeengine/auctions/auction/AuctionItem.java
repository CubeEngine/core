package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.core.persistence.Database;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an Item in the AuctionBox
 * 
 * @author Faithcaio
 */
public class AuctionItem
{
    private int id;

    private Bidder bidder;
    private ItemStack item;
    private long date;
    private Bidder owner;
    private Double price;
    
    private final Database db;
    
/**
 * Creates a new AuctionItem when won auction + Add it to DataBase
 */
    public AuctionItem(Auction auction)
    {
        this.db = CubeAuctions.getInstance().getDB();
        if (auction.getBids().isEmpty())
        {
            this.bidder = auction.getOwner();
            this.price = 0.0;
        }
        else
        {
            this.bidder = auction.getBids().peek().getBidder();
            this.price = auction.getBids().peek().getAmount();
        }
        this.item = auction.getItemStack().clone();// = new ItemStack(auction.item.getType(),auction.item.getAmount());
        this.date = System.currentTimeMillis();
        this.owner = auction.getOwner();
        this.id = -1;
        try
        {
            db.exec(
                    "INSERT INTO `auctionbox` ("+
                    "`bidderid` ,"+
                    "`item` ,"+
                    "`amount` ,"+
                    "`price` ,"+
                    "`timestamp` ,"+
                    "`ownerid`"+
                    ")"+
                    "VALUES ( ?, ?, ?, ?, ?, ? );"
                  ,this.bidder.getId(),Util.convertItem(this.item),
                  this.item.getAmount(),this.price,new Timestamp(this.date),auction.getOwnerId());
            ResultSet set =
                    db.query("SELECT * FROM `auctionbox` ORDER BY `id` DESC LIMIT 1");
             if (set.next())
                this.id = set.getInt("id");
                
        }
        catch (SQLException ex)
        {
            
        }
    }

/**
 * Loads in an AuctionItem from DataBase
 */
    public AuctionItem(int id, Bidder bidder, ItemStack item, Timestamp time,Bidder owner, double price)
    {
        this.db = CubeAuctions.getInstance().getDB();
        this.bidder = bidder;
        this.item = item;
        this.date = time.getTime();
        this.owner = owner;
        this.price = price;
        this.id = id; 
    }
    
/**
 * Creates a new AuctionItem when aborted + Add it to DataBase
 */ 
    public AuctionItem(ItemStack item, Bidder bidder)
    {
        this.db = CubeAuctions.getInstance().getDB();
        this.bidder = bidder;
        this.item = item;
        this.date = System.currentTimeMillis();
        this.owner = bidder;
        this.price = 0.0;
        this.id = -1; 
        
        try
        {
            db.exec(
                    "INSERT INTO `auctionbox` ("+
                    "`playerid` ,"+
                    "`item` ,"+
                    "`amount` ,"+
                    "`price` ,"+
                    "`timestamp` ,"+
                    "`ownerid`"+
                    ")"+
                    "VALUES ( ?, ?, ?, ?, ?, ?);"
                  ,bidder.getId(),Util.convertItem(item),
                  item.getAmount(),this.price,this.date,bidder.getId());
            ResultSet set =
                    db.query("SELECT * FROM `auctionbox` ORDER BY `id` DESC LIMIT 1");
            if (set.next())
                this.id = set.getInt("id");
        }
        catch (SQLException ex)
        {
            
        }
    }
    
/**
 * Creates a Fake auctionItem
 */
    private AuctionItem(Bidder bidder, ItemStack item, long date, Bidder owner, Double price)
    {
        this.db = CubeAuctions.getInstance().getDB();
        this.bidder = bidder;
        this.item = item;
        this.date = date;
        this.owner = owner;
        this.price = price;
    }
    
/**
 *  @return TableName in Database
 */ 
    public String getTable()
    {
        return "auctionbox";
    }

/**
 * @return A clone of this auctionItem
 */
    public AuctionItem cloneItem()
    {
        return new AuctionItem(bidder, item, date, owner, price);
    }

/**
 * @return owner of this auctionItem
 */
    public Bidder getBidder()
    {
        return this.bidder;
    }
    
/**
 * @return item as Itemstack
 */ 
    public ItemStack getItemStack()
    {
        return this.item;
    }
    
/**
 * @return date when added to Box
 */ 
    public long getDate()
    {
        return this.date;
    }
    
/**
 * @return original owner
 */ 
    public String getOwnerName()
    {
        return this.owner.getName();
    }
    
    
        public Bidder getOwner()
    {
        return this.owner;
    }
    
/**
 * @return price item was bought
 */ 
    public Double getPrice()
    {
        return this.price;
    }
    
/**
 * @return Id in DataBase
 */ 
    public int getId()
    {
        return this.id;
    }   
    
/**
 *  @return TableName for Database
 */ 
    public String getDBTable()
    {
        return "`"+this.getTable()+"`";
    }
    
    
    public Timestamp getTimestamp()
    {
        return new Timestamp(this.date);
    }
}