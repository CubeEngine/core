package de.cubeisland.cubeengine.auctions.auction;

import de.cubeisland.cubeengine.auctions.AuctionsConfiguration;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import de.cubeisland.cubeengine.auctions.Sorter;
import de.cubeisland.cubeengine.auctions.auction.timer.AuctionTimer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class AuctionManager
{
    //TODO 2 storages for auctions
    private Map<Integer, Auction> activeAuctions;
    private Map<Integer, Auction> endedAuctions;
    private AuctionsConfiguration config;

    public AuctionManager()
    {
        this.activeAuctions = new HashMap<Integer, Auction>();
        this.endedAuctions = new HashMap<Integer, Auction>();
        this.config = CubeAuctions.getInstance().getConfiguration();
    }

    public void registerAuction(Auction auction)
    {
        this.activeAuctions.put(auction.getKey(), auction);
    }

    public void registerEndingAuction(Auction auction)
    {
        this.endedAuctions.put(auction.getKey(), auction);
    }

    public void startAuction(Bidder owner, ItemStack item, long duration)
    {
        this.startAuction(owner, item, duration, config.default_length);
    }

    public void startAuction(Bidder bidder, ItemStack item, long duration, double startbid)
    {
        Auction auction = new Auction(bidder, item, duration);
        //TODO store auction in db
        this.pushbid(auction, bidder, startbid);
        this.registerAuction(auction);
        AuctionTimer.startTimer();
        CubeAuctions.debug("START auction");
    }

    public void pushbid(Auction auction, Bidder bidder, double amount)
    {
        Bid bid = new Bid(bidder, auction, amount);
        auction.pushBid(bid);
        //TODO store bid in db
    }

    public boolean bid(Auction auction, Bidder bidder, double amount)
    {
        //TODO check money
        //TODO check auctions
        if (1 == 0)
        {
            return false;
        }
        this.pushbid(auction, bidder, amount);
        return true;
    }

    public void popBid(Auction auction)
    {
        auction.popBid();
    }

    public boolean undobid(Auction auction, Bidder bidder)
    {
        if (auction.isTopBidder(bidder))
        {
            auction.popBid();
            return true;
        }
        return false;
    }

    public void endAuction(Auction auction)
    {
        if (auction.isTopBidder(auction.getOwner()))
        {
            this.abortAuction(auction);
            double comission = auction.getTopBid().getAmount() * config.comission / 100;
            //TODO charge comission to User
            this.abortAuction(auction);
            return;
        }
        this.finishAuction(auction, null);
    }

    private void finishAuction(Auction auction, HashSet<Bidder> punished)
    {
        Bid bid = auction.getTopBid();

        if (auction.isTopBidder(auction.getOwner()))
        {
            //all Bidder had not enough money
            this.abortAuction(auction);
        }
        if (punished.contains(bid.getBidder()))
        {
            //User was already punished
            auction.popBid();
            this.finishAuction(auction, punished);
            return;
        }
        //TODO check money
        if (1 == 0)
        {
            if (punished == null)
            {
                punished = new HashSet<Bidder>();
            }
            double punish = bid.getAmount() * config.punish;
            //TODO punish player
            auction.popBid();
            punished.add(bid.getBidder());
            //get next Winner
            this.finishAuction(auction, punished);
            return;
        }
        //TODO take money
        Bidder bidder = bid.getBidder();
        this.notify(bidder, Bidder.NOTIFY_WIN);
        this.notify(bidder, Bidder.NOTIFY_ITEMS);
        //TODO notify or set notifyState

        this.activeAuctions.remove(auction.getKey());
        this.registerEndingAuction(auction);
        //TODO move auction to other storage in db
        CubeAuctions.debug("END auction");
    }

    public void notify(Bidder bidder, byte notifyState)
    {
        if (bidder.getUser().isOnline())
        {
            switch (notifyState)
            {
                case Bidder.NOTIFY_CANCEL://TODO msg
                    return;
                case Bidder.NOTIFY_WIN://TODO msg
                    return;
            }
            return;
        }
        bidder.getNotifyState().set(notifyState);
    }

    public void abortAuction(Auction auction)
    {
        //remove all bids but startbid
        Bid bid = auction.getBids().firstElement();
        auction.getBids().clear();
        auction.pushBid(bid);
        this.activeAuctions.remove(auction.getKey());
        this.registerEndingAuction(auction);
        //TODO move auction to other storage in db
        this.notify(bid.getBidder(), Bidder.NOTIFY_CANCEL);
        CubeAuctions.debug("ABORT auction");
    }

    public void removeAuction(Auction auction)
    {
        this.activeAuctions.remove(auction.getKey());
        this.endedAuctions.remove(auction.getKey());
        for (Bid bid : auction.getBids())
        {
            //TODO delete bid in db
        }
        auction.setBids(null);
        //TODO delete auction in db

        CubeAuctions.debug("REMOVE auction");
    }

    public List<Auction> getSoonEndingAuctions()
    {
        List<Auction> sortList = new ArrayList(this.activeAuctions.values());
        Sorter.DATE.sortAuction(sortList);
        return sortList;
    }

    public Auction getNextAuktion()
    {
        List<Auction> sortList = this.getSoonEndingAuctions();
        if (sortList.isEmpty())
        {
            return null;
        }
        return sortList.get(0);
    }
}
