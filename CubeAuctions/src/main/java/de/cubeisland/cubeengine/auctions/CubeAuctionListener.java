package de.cubeisland.cubeengine.auctions;

import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.AuctionManager;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.auctions.auction.BidderManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.user.event.UserCreatedEvent;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Faithcaio
 */
public class CubeAuctionListener implements Listener
{
    CubeAuctions plugin;
    BidderManager bidderManager;//TODO
    AuctionManager auctionManager; //TODO;
    UserManager cuManager;//TODO
    AuctionsConfiguration config;

    public CubeAuctionListener(CubeAuctions plugin)
    {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
    }

    private Bidder getBidderOfPlayer(Player player)
    {
        User user = cuManager.getUser(player);
        return bidderManager.getBidder(user);
    }

    @EventHandler
    public void bidderCreator(final UserCreatedEvent event)
    {
        //TODO
        Bidder bidder = new Bidder(event.getUser());
        bidderManager.addBidder(bidder);
    }

    @EventHandler
    public void goesOnline(final PlayerJoinEvent event)
    {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                User user = cuManager.getUser(event.getPlayer());
                Bidder bidder = bidderManager.getBidder(user);
                if (bidder.getNotifyState().isset(Bidder.NOTIFY_WIN))
                {
                    user.sendMessage("");// t("i")+" "+t("event_new"));
                    bidder.getNotifyState().unset(Bidder.NOTIFY_WIN);
                }
                if (bidder.getNotifyState().isset(Bidder.NOTIFY_CANCEL))
                {
                    user.sendMessage("");//t("i")+" "+t("event_fail"));
                    bidder.getNotifyState().unset(Bidder.NOTIFY_CANCEL);
                }
                if (bidder.getNotifyState().isset(Bidder.NOTIFY_ITEMS))
                {
                    user.sendMessage("");//t("i")+" "+t("event_old",config.auction_itemBoxLength));
                    bidder.getNotifyState().unset(Bidder.NOTIFY_ITEMS);
                }
                //TODO update bidder DB
            }
        ;
    }

    );
    }
    
    @EventHandler
    public void goesOffline(PlayerQuitEvent event)
    {
        Bidder bidder = this.getBidderOfPlayer(event.getPlayer());
        Collection<Auction> auctionBox = bidder.getBox();
        Collection<Auction> oldItems = new ArrayList<Auction>();
        for (Auction auction : auctionBox)
        {
            if (System.currentTimeMillis() - auction.getAuctionEnd() > StringUtils.convertTimeToMillis(config.itemBoxLength))
            {
                oldItems.add(auction);
            }
        }
        for (Auction auction : oldItems)
        {
            auctionManager.removeAuction(auction);
        }
        if (!(auctionBox.isEmpty()))
        {
            bidder.getNotifyState().set(Bidder.NOTIFY_ITEMS);
        }
        //TODO update Databases
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        if (event.getLine(0).equalsIgnoreCase("[CubeAuctions]") || event.getLine(0).equalsIgnoreCase("[ca]"))
        {
            Player player = event.getPlayer();
            if (event.getLine(1).equalsIgnoreCase("AuctionBox") || event.getLine(1).equalsIgnoreCase("box"))
            {
                //TODO perm & cancel
                event.setLine(1, "AuctionBox");
                event.setLine(2, "");
                event.setLine(3, "");
            }
            else
            {
                if (event.getLine(1).equalsIgnoreCase("Start"))
                {
                    //TODO perm & cancel
                    if (StringUtils.convertTimeToMillis(event.getLine(2)) < 0)
                    {
                        //TODO msg player.sendMessage(t("event_sign_fail"));
                        event.setCancelled(true);
                        return;
                    }
                    event.setLine(1, "START");
                }
                else
                {
                    if (event.getLine(1).equalsIgnoreCase("List") || event.getLine(1).equalsIgnoreCase("AuctionSearch"))
                    {
                        //TODO perm & cancel
                        if (Material.matchMaterial(event.getLine(2)) != null)
                        {
                            event.setLine(2, Material.matchMaterial(event.getLine(2)).toString());
                        }
                        else
                        {
                            event.setLine(2, "# All #");
                        }
                        event.setLine(1, "AuctionSearch");
                        event.setLine(3, "");
                    }
                    else
                    {
                        //TODO player.sendMessage(t("event_sign_fail"));
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            //TODO player.sendMessage(t("event_sign_create"));            
            event.setLine(0, "[CubeAuctions]");
        }
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event)
    {
        final Player player = event.getPlayer();
        if (!player.isSneaking())
        {
            final Block signblock = event.getBlock();
            if (signblock.getType().equals(Material.WALL_SIGN))
            {
                Sign sign = (Sign) signblock.getState();
                if (sign.getLine(0).equals("[CubeAuctions]"))
                {
                    event.setCancelled(true);
                    signblock.getState().update();
                    return;
                }
            }
            for (BlockFace face : BlockFace.values())
            {
                if (event.getBlock().getRelative(face).getType().equals(Material.WALL_SIGN))
                {
                    Sign sign = (Sign) event.getBlock().getRelative(face).getState();
                    if (sign.getLine(0).equalsIgnoreCase("[CubeAuctions]"))
                    {
                        if (sign.getRawData() == 0x2 && face.equals(BlockFace.EAST))
                        {
                            event.setCancelled(true);
                            return;
                        }
                        if (sign.getRawData() == 0x3 && face.equals(BlockFace.WEST))
                        {
                            event.setCancelled(true);
                            return;
                        }
                        if (sign.getRawData() == 0x4 && face.equals(BlockFace.NORTH))
                        {
                            event.setCancelled(true);
                            return;
                        }
                        if (sign.getRawData() == 0x5 && face.equals(BlockFace.SOUTH))
                        {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }
}
