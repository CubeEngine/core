package de.cubeisland.cubeengine.auctions;

import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.auctions.database.AuctionStorage;
import de.cubeisland.cubeengine.auctions.database.BidStorage;
import de.cubeisland.cubeengine.auctions.database.SubscriptionStorage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import org.bukkit.inventory.ItemStack;

/**
 * Manages all Auctions
 *
 * @author Faithcaio
 */
public class Manager
{
    private static Manager instance = null;
    private final List<Auction> auctions;
    private final Stack<Integer> freeIds;
    private static final CubeAuctionsConfiguration config = CubeAuctions.getConfiguration();
    private HashMap<Bidder, Bidder> remBidderConfirm = new HashMap();
    private HashSet<Bidder> remAllConfirm = new HashSet();
    private HashMap<Bidder, Integer> remSingleConfirm = new HashMap();
    private Price price = new Price();
    

/**
 * Init Manager
 */    
    private Manager()
    {
        int maxAuctions = config.auction_maxAuctions_overall;
        if (maxAuctions <= 0)
        {
            maxAuctions = 1;
        }
        this.auctions = new ArrayList<Auction>();
        this.freeIds = new Stack<Integer>();
        for (int i = maxAuctions; i > 0; --i)
        {
            this.freeIds.push(i);
        }
    }
    
/**
 * @return Manager or create new
 */  
    public static Manager getInstance()
    {
        if (instance == null)
        {
            instance = new Manager();
        }
        return instance;
    }
    
/**
 * @return Manager or create new
 */  
    public Auction getAuction(int id) //Get Auction with ID
    {
        Auction auction = null;
        int size = this.auctions.size();
        for (int i = 0; i < size; i++)
        {
            if (this.auctions.get(i).getId() == id)
            {
                auction = this.auctions.get(i);
            }
        }
        return auction;
    }

/**
 * @return All auctions
 */
    public List<Auction> getAuctions()
    {
        return auctions;
    }
    
/**
 * @return auction with id
 */
    public Auction getIndexAuction(int id)
    {
        return auctions.get(id);
    }

/**
 * @return true if no freeId availiable
 */
    public boolean isEmpty()
    {
        return freeIds.isEmpty();
    }
    
/**
 * @return amount of auctions
 */
    public int size()
    {
        return auctions.size();
    }
    
/**
 * @return All auctions with material
 */
    public List<Auction> getAuctionItem(ItemStack material)
    {
        List<Auction> auctionlist = new ArrayList<Auction>();
        int size = this.auctions.size();
        for (int i = 0; i < size; i++)
        {
            Auction auction = this.auctions.get(i);
            if (auction == null)
            {
                return null;
            }
            if (auction.getItemStack().getType() == material.getType())
            //    && (this.auctions.get(i).getItemData() == material.getDurability())
            {
                auctionlist.add(auction);
            }
        }
        return auctionlist;
    }
    
/**
 * @return All auctions with material and without bidder
 */
    public List<Auction> getAuctionItem(ItemStack material, Bidder bidder)
    {
        List<Auction> auctionlist = this.getAuctionItem(material);
        for (Auction auction : bidder.getActiveBids())
        {
            if (auction.getOwner() == bidder)
            {
                auctionlist.remove(auction);
            }
        }
        return auctionlist;
    }
    
/**
 * @return All auctions sorted by EndDate
 */
    public List<Auction> getEndingAuctions()
    {
        List<Auction> endingActions = new ArrayList<Auction>();
        int size = this.auctions.size();
        for (int i = 0; i < size; ++i)
        {
            endingActions.add(this.auctions.get(i));
        }
        Sorter.DATE.sortAuction(endingActions);
        return endingActions;
    }
    
/**
 * removes Auction completly
 */
    public boolean cancelAuction(Auction auction, boolean win)
    {
        this.freeIds.push(auction.getId());
        Collections.sort(this.freeIds);
        Collections.reverse(this.freeIds);

        if (!(auction.getOwner().isServerBidder()))
        {
            auction.getOwner().removeAuction(auction);
            while (!(auction.getBids().isEmpty()))
            {
                Bidder.getInstance(auction.getBids().peek().getBidder().getOffPlayer()).removeAuction(auction);
                auction.getBids().pop();
            }
            if (!win)
                auction.getOwner().getBox().addItem(auction);
        }
        else
        {
            Bidder.getInstance(0).removeAuction(auction);
        }
        AuctionStorage auctionDB = new AuctionStorage();
        auctionDB.delete(auction.getId());
        BidStorage bidDB = new BidStorage();
        bidDB.deleteByAuction(auction.getId());
        SubscriptionStorage subDB = new SubscriptionStorage();
        this.auctions.remove(auction);
        return true;
    }

/**
 * Adds an auction
 */
    public void addAuction(Auction auction)
    {
        this.auctions.add(auction);
    }
    
/**
 * Adds auctions from DB
 */
    public void addAuctions(Collection<Auction> auctions)
    {
        for (Auction auction : auctions)
        {
            this.auctions.add(auction);
        }
    }
    
/**
 * @return Stack of Free AuctionIds
 */
    public Stack<Integer> getFreeIds()
    {
        return this.freeIds;
    }
    
/**
 * @return HashMap for remove Bidder-auction confirmation
 */
    public HashMap<Bidder, Bidder> getBidderConfirm()
    {
        return this.remBidderConfirm;
    }
    
/**
 * @return HashMap for remove all auction confirmation
 */
    public HashSet<Bidder> getAllConfirm()
    {
        return this.remAllConfirm;
    }

/**
 * @return HashMap for remove single auction confirmation
 */
    public HashMap<Bidder, Integer> getSingleConfirm()
    {
        return this.remSingleConfirm;
    }
    
/**
 * removes old auctions after starting Server
 */
    public void removeOldAuctions()
    {
        List<Auction> t_auctions = new ArrayList<Auction>(this.auctions);
        for (Auction auction : t_auctions)
        {
            if (auction.getAuctionEnd() < System.currentTimeMillis())
                this.cancelAuction(auction, false);
        }
    }
    
/**
 * @return average Price of item
 */
    public double getPrice(ItemStack item)
    {
       return this.price.getPrice(item);
    }
    
/**
 * adjust average Price for item
 */
    public double adjustPrice(ItemStack item, double price)
    {
        return this.price.adjustPrice(item, price);
    }
    
/**
 * set average Price for item
 */
    public double setPrice(ItemStack item, double price, int amount)
    {
        return this.price.setPrice(item, price, amount);
    }
}
