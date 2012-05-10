package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.auctions.AuctionHouseConfiguration;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.Perm;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.database.Database;
import de.cubeisland.cubeengine.auctions.database.DatabaseEntity;
import de.cubeisland.cubeengine.auctions.database.EntityIdentifier;
import de.cubeisland.cubeengine.auctions.database.EntityProperty;
import java.sql.Timestamp;
import java.util.Stack;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an auction
 *
 * @author Faithcaio
 */
public class Auction implements DatabaseEntity
{
    
    @EntityIdentifier
    private int id;
    @EntityProperty
    private final ItemStack item;
    @EntityProperty
    private Bidder owner;
    @EntityProperty
    private final long auctionEnd;
    @EntityProperty
    private final Stack<Bid> bids;
    
    
    private static final CubeAuctions plugin = CubeAuctions.getInstance();
    private static final AuctionHouseConfiguration config = plugin.getConfiguration();
    private final Database db;
    
/**
 * Creates an new auction
 */
    public Auction(ItemStack item, Bidder owner, long auctionEnd, double startBid)
    {
        
        
        
        this.db = CubeAuctions.getInstance().getDB();
        this.id = Manager.getInstance().getFreeIds().pop();
        this.item = item;
        this.owner = owner;
        this.auctionEnd = auctionEnd;
        this.bids = new Stack<Bid>();
        this.bids.push(new Bid(owner, startBid, this));
    }

/**
 * Load in auction from DataBase
 */
    public Auction(int id,ItemStack item, Bidder owner, long auctionEnd)
    {
        Manager.getInstance().getFreeIds().removeElement(id);
        this.db = CubeAuctions.getInstance().getDB();
        this.id = id;
        this.item = item;
        this.owner = owner;
        this.auctionEnd = auctionEnd;
        this.bids = new Stack<Bid>();
    }

/**
 * Adds a bid to auction
 * @return true if bidded succesfully
 */
    public boolean bid(final Bidder bidder, final double amount)//evtl nicht bool / bessere Unterscheidung
    {
        if (amount <= 0)
        {
            bidder.getPlayer().sendMessage(t("e")+" "+t("auc_bid_low1"));
            return false;
        }
        if (amount <= this.bids.peek().getAmount())
        {
            bidder.getPlayer().sendMessage(t("i")+" "+t("auc_bid_low2"));
            return false;
        }
        if ((CubeAuctions.getInstance().getEconomy().getBalance(bidder.getName()) >= amount)
                || Perm.command_bid_infinite.check(bidder.getPlayer()))
        {
            if (CubeAuctions.getInstance().getEconomy().getBalance(bidder.getName()) - bidder.getTotalBidAmount() >= amount
                    || Perm.command_bid_infinite.check(bidder.getPlayer()))
            {
                this.bids.push(new Bid(bidder, amount, this));
                return true;
            }
            bidder.getPlayer().sendMessage(t("e")+" "+t("auc_bid_money1"));
            return false;
        }
        bidder.getPlayer().sendMessage(t("e")+" "+t("auc_bid_money2"));
        return false;
    }
    
/**
 * reverts a bid if allowed
 * @return true if reverted succesfully
 */
    public boolean undobid(final Bidder bidder)
    {
        
        Bid bid = this.bids.peek();
        if (bidder != bid.getBidder())
        {
            bidder.getPlayer().sendMessage(t("e")+" "+t("undo_bidder"));
            return false;
        }
        if (bidder == this.owner)
        {
            bidder.getPlayer().sendMessage(t("pro")+" "+t("undo_pro2"));
            return false;
        }
        long undoTime = config.auction_undoTime;
        if (undoTime < 0) //Infinite UndoTime
        {
            undoTime = this.auctionEnd - bid.getTimestamp();
        }
        if ((System.currentTimeMillis() - bid.getTimestamp()) > undoTime)
        {
            bidder.getPlayer().sendMessage(t("e")+" "+t("undo_time"));
            return false;
        }
        //else: Undo Last Bid
        db.execUpdate("DELETE FROM `bids` WHERE `bidderid`=? && `auctionid`=? && `timestamp`=?"
                      ,bidder.getId(), this.id, bid.getTimestamp());
        this.bids.pop();
        return true;
    }

/**
 * @return id as int
 */   
    public int getId()
    {
       return this.id; 
    }
    
/**
 * sets the AuctionID
 * @param int id
 */   
    public void setId(int id)
    {
        this.id = id;
    }
    
/**
 * @return item as ItemStack
 */       
    public ItemStack getItemStack()
    {
        return this.item;
    }   
    
/**
 * @return owner as Bidder
 */      
    public Bidder getOwner()
    {
        return this.owner;
    }
    
/**
 * @return auctionEnd in Milliseconds
 */     
    public long getAuctionEnd()
    {
        return this.auctionEnd;
    }
    
/**
 * @return all bids
 */       
    public Stack<Bid> getBids()
    {
        return this.bids;
    }
    
/**
 * gives owner (and last/initial bid) to Server
 */    
    public void giveServer()
    {
        this.owner = ServerBidder.getInstance();
        this.bids.peek().giveServer();
    }
    
/**
 * 
 * @return DataBase Id of the owner
 */
    public int getOwnerId()
    {
        return this.owner.getId();
    }
    
/**
 * 
 * @return Amount of item in this auction
 */
    public int getItemAmount()
    {
        return this.item.getAmount();
    }
    
/**
 * 
 * @return item as String for DataBase
 */
    public String getConvertItem()
    {
        return Util.convertItem(this.item);
    }
    
/**
 * 
 * @return DataBase Timestamp of AuctionEnd
 */
    public Timestamp getEndTimestamp()
    {
        return new Timestamp(this.auctionEnd);
    }
    
/**
 * 
 * @return ItemType of item as String
 */
    public String getItemType()
    {
        return this.item.getType().toString();
    }
    
/**
 * 
 * @return DataValue / DamageValue of item
 */
    public short getItemData()
    {
        return this.item.getDurability();
    }

/**
 *  @return TableName in Database
 */ 
    public String getTable()
    {
        return "auction";
    }
    
/**
 *  @return TableName for Database
 */ 
    public String getDBTable()
    {
        return "`"+this.getTable()+"`";
    }
}