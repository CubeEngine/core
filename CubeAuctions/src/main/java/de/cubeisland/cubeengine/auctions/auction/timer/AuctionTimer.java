package de.cubeisland.cubeengine.auctions.auction.timer;

import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.AuctionManager;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Anselm
 */
public class AuctionTimer
{
    private static TimerTask timerTask;
    private static AuctionManager manager;
    private static Timer lastTimer;
    private static Auction nextAuction;

    public AuctionTimer(AuctionManager p_manager)
    {
        timerTask = new TimerTask()
        {
            public void run()
            {
                manager.endAuction(nextAuction);
                startTimer();
            }
        };
        manager = p_manager;
    }

    public static void startTimer()
    {
        Auction auction = manager.getNextAuktion();
        if (auction == null)
        {
            return;
        }
        startTimer(auction);
    }

    public static void startTimer(Auction auction)
    {
        long delay = auction.getAuctionEnd() - System.currentTimeMillis();
        if (delay <= 0)
        {
            manager.endAuction(auction);
            startTimer();
            return;
        }
        startTimer(delay, auction);
    }

    private static void startTimer(long delay, Auction auction)
    {
        if (lastTimer != null)
        {
            lastTimer.cancel();
        }
        Timer timer = new Timer();
        timer.schedule(timerTask, delay);
        lastTimer = timer;
        nextAuction = auction;
    }
}
