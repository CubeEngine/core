package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.auctions.AuctionBox;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.database.BidStorage;
import de.cubeisland.cubeengine.auctions.database.BidderStorage;
import de.cubeisland.cubeengine.auctions.database.SubscriptionStorage;
import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Represents a Bidder / Player using AuctionHouse
 * 
 * @author Faithcaio
 */
public final class Bidder implements Model<User>
{
    public static final byte NOTIFY_STATUS = 8;
    public static final byte NOTIFY_ITEMS = 4;
    public static final byte NOTIFY_CANCEL = 2;
    public static final byte NOTIFY_WIN = 1;
    
    private User user;
    
    private final ArrayList<Auction> activeBids = new ArrayList<Auction>();;
    private final ArrayList<Auction> subscriptions = new ArrayList<Auction>();;
    private final ArrayList<Material> materialSub = new ArrayList<Material>();;
    private final AuctionBox auctionbox;
    private byte notifyState = 0;

    private static final Map<OfflinePlayer, Bidder> bidderInstances = new HashMap<OfflinePlayer, Bidder>();
    private static Bidder serverBidder;
    
    private static UserManager cuManager = CubeCore.getInstance().getUserManager();
    private static BidderStorage bidderDB = new BidderStorage();
    private SubscriptionStorage subDB = new SubscriptionStorage();
    private BidStorage bidDB = new BidStorage();
/**
 * Creates a new Bidder + add him to DataBase
 */
    public Bidder(OfflinePlayer player)
    {
        this.user = cuManager.getUser(player);
        this.auctionbox = new AuctionBox(this);
    }

    public Bidder(int id)
    {
        this.user = cuManager.getUser(id);
        this.auctionbox = new AuctionBox(this);
    }
    
/**
 * Creates a new Bidder from DataBase + load in Subs etc...
 */   
    public Bidder(int id, byte notifyState)
    {
        this.auctionbox = new AuctionBox(this);
        this.user = cuManager.getUser(id);
        this.notifyState = notifyState;
        this.addDataBaseSub();
        if (id == 1)
            serverBidder = this;
        else
            bidderInstances.put(user.getOfflinePlayer(), this);
    }

/**
 * @return New Bidder loaded in from DataBase
 */    
    public static Bidder getInstance(int id)
    {
        Bidder instance;
        if (id == 1)
            instance = serverBidder;
        else
            instance = bidderInstances.get(cuManager.getUser(id).getOfflinePlayer());

        if (instance == null)
        {
            instance = new Bidder(id);
            if (id==1)
                serverBidder = instance;
            else
                bidderInstances.put(cuManager.getUser(id).getOfflinePlayer(),instance);
            bidderDB.store(instance);
        }
        return instance;
    }
    
    
    
    public static Bidder getInstance(String name)
    {
        return getInstance(cuManager.getUser(name).getPlayer());
    }
    
/**
 * @return HashMap of all current Bidder
 */   
    public static Map<OfflinePlayer, Bidder> getInstances()
    {
        return bidderInstances;
    }
    
/**
 * if not given create a new Bidder
 * @return Bidder by Player
 */   
    public static Bidder getInstance(Player player)
    {
        return getInstance((OfflinePlayer)player);
    }
    
/**
 * if not given create a new Bidder
 * @return Bidder by OfflinePlayer
 */   
    public static Bidder getInstance(OfflinePlayer player)
    {
        Bidder instance = bidderInstances.get(player);
        if (instance == null)
        {
            instance = new Bidder(player);
            bidderDB.store(instance);
            bidderInstances.put(player, instance);
        }
        return instance;
    }
    
/**
 * creates NEVER a new Bidder
 * @return Bidder by Player
 */   
    public static Bidder getInstanceNoCreate(OfflinePlayer player)
    {
        return bidderInstances.get(player);
    }
    
/**
 * if not given create a new Bidder
 * @return Bidder by CommandSender
 */   
    public static Bidder getInstance(CommandSender player)
    {
        if (player instanceof Player)
        {
            return getInstance((Player)player);
        }
        return Bidder.getInstance(1);
    }
    
/**
 * resets the notify bitmask
 */   
    public final void resetNotifyState()
    {
        this.resetNotifyState((byte)0);
    }

/**
 * sets the notify bitmask
 */   
    public void resetNotifyState(byte state)
    {
        this.notifyState = state;
    }

/**
 * sets the notify state
 * @param Bidder.NOTIFY_STATUS | NOTIFY_ITEMS | NOTIFY_CANCEL | NOTIFY_WIN
 */
    public void setNotifyState(byte state)
    {
        this.notifyState |= state;
    }

/**
 * @param Bidder.NOTIFY_STATUS | NOTIFY_ITEMS | NOTIFY_CANCEL | NOTIFY_WIN
 * @return notify State of this Bidder
 */ 
    public boolean hasNotifyState(byte state)
    {
        return ((this.notifyState & state) == state);
    }

/**
 * gets the notify bitmask
 */     
    public byte getNotifyState()
    {
        return this.notifyState;
    }

/**
 * toggles the notify state
 * @param Bidder.NOTIFY_STATUS | NOTIFY_ITEMS | NOTIFY_CANCEL | NOTIFY_WIN
 */ 
    public void toggleNotifyState(byte state)
    {
        this.notifyState ^= state;
    }
    
/**
 * unsets the notify state
 * @param Bidder.NOTIFY_STATUS | NOTIFY_ITEMS | NOTIFY_CANCEL | NOTIFY_WIN
 */ 
    public void unsetNotifyState(byte state)
    {
        this.notifyState &= ~state;
    }

/**
 * @return AuctionBox of this Bidder
 */ 
    public AuctionBox getBox()
    {
        return auctionbox;
    }

/**
 * @return All auctions started or bid on by this Bidder
 */ 
    public ArrayList<Auction> getActiveBids()
    {
        return activeBids;
    }

/**
 * @return All subscribed auctions
 */ 
    public ArrayList<Auction> getSubs()
    {
        return subscriptions;
    }
    
/**
 * @return All material subscriptions
 */ 
    public ArrayList<Material> getMatSub()
    {
        return materialSub;
    }

/**
 * @return Player represented by this Bidder if online
 */ 
    public Player getPlayer()
    {
        return this.user.getPlayer();
    }
    
/**
 * @return BidderID in DataBase
 */ 
    public User getKey()
    {
        return this.user;
    }
    
/**
 * @return BidderID in DataBase
 */ 
    public OfflinePlayer getOffPlayer()
    {
        return user.getOfflinePlayer();
    }

/**
 * @return Exact name of this Bidder
 */  
    public String getName()
    {
        return user.getName();
    }
    
/**
 * @return true if Bidder is online
 */  
    public boolean isOnline()
    {
        if (user == null)
        {
            return false;
        }
        return user.isOnline();
    }
    
/**
 * @return Total amount of money spend in leading bids
 */  
    public double getTotalBidAmount()
    {
        double total = 0;
        List<Auction> auctionlist;
        if (!(this.getLeadingAuctions().isEmpty()))
        {
            auctionlist = this.getLeadingAuctions();
            for (int i = 0; i < auctionlist.size(); ++i)
            {
                total += auctionlist.get(i).getBids().peek().getAmount();
            }
        }
        return total;
    }

/**
 * removes auction + bids + subscription from this Bidder and out of DataBase
 * @return could remove?
 */  
    public boolean removeAuction(Auction auction)
    {
        
        bidDB.deleteByAuctionByUser(auction.getKey() , this.getKey());
        this.removeSubscription(auction);
        return activeBids.remove(auction);
    }
    
/**
 * removes Id Subscription from this Bidder and out of DataBase
 * @return could remove?
 */  
    public boolean removeSubscription(Auction auction)
    {
        subDB.deleteSubByUser(this.getKey() ,String.valueOf(auction.getKey()));
        return subscriptions.remove(auction);
    }

/**
 * removes Material Subscription from this Bidder and out of DataBase
 * @return could remove?
 */  
    public boolean removeSubscription(Material item)
    {
        //MAtSub delete
        subDB.deleteSubByUser(this.getKey(), item.toString());
        return materialSub.remove(item);
    }

/**
 * @param Bidder to get auctions of
 * @return all auctions of player with leading Bid (excluding own auctions)
 */  
    public List<Auction> getLeadingAuctions(Bidder player)
    {
        List<Auction> auctionlist = new ArrayList<Auction>();
        for (Auction auction : this.activeBids)
        {
            if (auction.getBids().peek() == null)
            {
                return null;
            }
            if (auction.getBids().peek().getBidder() == player)
            {
                auctionlist.add(auction);
            }
        }
        return auctionlist;
    }

/**
 * @return all auctions of this Bidder with leading Bid (excluding own auctions)
 */  
    public List<Auction> getLeadingAuctions()
    {
        return this.getLeadingAuctions(this);
    }

/**
 * @return all auctions of this Bidder
 */  
    public List<Auction> getAuctions()
    {
        return activeBids;
    }
    
/**
 * @return all auctions started by player
 */  
    public List<Auction> getAuctions(Bidder player)
    {
        ArrayList<Auction> auctionlist = new ArrayList<Auction>()
        {
        };
        final int length = this.activeBids.size();
        for (int i = 0; i < length; i++)
        {
            if (this.activeBids.get(i).getOwner() == player)
            {
                auctionlist.add(this.activeBids.get(i));
            }
        }
        return auctionlist;
    }
/**
 * @return all auctions started by this Bidder
 */  
    public List<Auction> getOwnAuctions()
    {
        return this.getAuctions(this);
    }

/**
 * @return last Auction player bid on
 */  
    public Auction getlastAuction(Bidder player) //Get last Auction Bid on
    {

        final int length = this.activeBids.size();
        int auctionIndex = -1;
        for (int i = 0; i < length; i++)
        {

            if (this.activeBids.get(i).getBids().peek().getBidder() == player)
            {
                if (auctionIndex == -1)
                {
                    auctionIndex = i;
                }
                if (this.activeBids.get(i).getBids().peek().getTime()
                    > this.activeBids.get(auctionIndex).getBids().peek().getTime())
                {
                    if (this.activeBids.get(i).getOwner() != player)
                    {
                        auctionIndex = i;
                    }
                }
            }
        }
        if (auctionIndex == -1)
        {
            return null;
        }
        return this.activeBids.get(auctionIndex);
    }
    
/**
 * Adds auction to this Bidder
 * @param auction to add to this Bidder
 * @return Bidder who added auction
 */  
    public Bidder addAuction(Auction auction)
    {
        this.activeBids.add(auction);
        this.addSubscription(auction);
        return this;
    }

/**
 * Adds auction to Subsriptionlist
 * @param auction to add to Subscriptions
 * @return could add subscription
 */  
    public boolean addSubscription(Auction auction)
    {
        if (this.subscriptions.contains(auction)) return false;
        subDB.store(this.user, String.valueOf(auction.getKey()));
        this.subscriptions.add(auction);
        return true;
    }

/**
 * Loads all items/auctions to Subsriptionlists from DataBase
 * 
 */  
    public void addDataBaseSub()
    {
        for (String s : subDB.getListByUser(this.getKey()))
        {
            if (Material.matchMaterial(s) != null)
                this.materialSub.add(Material.matchMaterial(s));
            else
                this.subscriptions.add(Manager.getInstance().getAuction(Integer.valueOf(s)));
        }
    }
/**
 * Adds item to Subsriptionlist
 * @param ItemStack to add to Subscriptions
 * @return could add subscription
 */  
    public boolean addSubscription(Material item)
    {
        if (this.materialSub.contains(item)) return false;
        subDB.store(this.user, item.toString());
        this.materialSub.add(item);
        return true;
    }

    public boolean isServerBidder()
    {
        throw new UnsupportedOperationException("No longer implemented");
    }

    public void setKey(User key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}