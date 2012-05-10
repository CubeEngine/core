package de.cubeisland.cubeengine.auctions;

import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.AuctionItem;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import java.util.Collections;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Faithcaio
 */
public class AuctionHouseListener implements Listener
{
    private final CubeAuctions plugin;
    private final AuctionHouseConfiguration config;
    private final Economy econ;
    
    public AuctionHouseListener(CubeAuctions plugin)
    {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.econ = plugin.getEconomy();
    }

/**
 * handles notification when player goes online
 */
    @EventHandler
    public void goesOnline(final PlayerJoinEvent event)
    {
        if (!event.getPlayer().hasPermission("auctionhouse.use")) return;
        
        Bidder bidder = Bidder.getInstance(event.getPlayer());
        Util.updateNotifyData(bidder);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
        {
            public void run()
            {
                Bidder bidder = Bidder.getInstance(event.getPlayer());
                if (bidder.hasNotifyState(Bidder.NOTIFY_WIN))
                {
                    event.getPlayer().sendMessage(t("i")+" "+t("event_new"));
                    bidder.unsetNotifyState(Bidder.NOTIFY_WIN);
                }
                if (bidder.hasNotifyState(Bidder.NOTIFY_CANCEL))
                {
                    event.getPlayer().sendMessage(t("i")+" "+t("event_fail"));
                    bidder.unsetNotifyState(Bidder.NOTIFY_CANCEL);
                }
                if (bidder.hasNotifyState(Bidder.NOTIFY_ITEMS))
                {
                    event.getPlayer().sendMessage(t("i")+" "+t("event_old",config.auction_itemBoxLength));
                    bidder.unsetNotifyState(Bidder.NOTIFY_ITEMS);
                }
            };
        });
    }
    
/**
 * handles notification when player goes offline
 */
    @EventHandler
    public void goesOffline(PlayerQuitEvent event)
    {
        Bidder bidder = Bidder.getInstance(event.getPlayer());
        AuctionBox items = bidder.getBox();
        
        if (!(items.getItemList().isEmpty()))
        {
            for (AuctionItem item : items.getItemList())
            {
                if (System.currentTimeMillis() - item.getDate() > config.auction_itemBoxLength * 24 * 60 * 60 * 1000)
                {
                    items.getItemList().remove(item);
                }
            }
        }
        if (!(items.getItemList().isEmpty()))
        {
            bidder.setNotifyState(Bidder.NOTIFY_ITEMS);
        }
        Util.updateNotifyData(bidder);
    }

/**
 * checks AuctionHouse sign creation
 */
    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        if(event.getLine(0).equalsIgnoreCase("[AuctionHouse]")||event.getLine(0).equalsIgnoreCase("[ah]"))
        {
            Player player = event.getPlayer();
            if (event.getLine(1).equalsIgnoreCase("AuctionBox")||event.getLine(1).equalsIgnoreCase("box"))
            {
                if (!Perm.sign_create_box.check(player))
                {
                    event.setCancelled(true);
                    return;
                }
                event.setLine(1, "AuctionBox");
                event.setLine(2, "");
                event.setLine(3, "");
            }
            else
            {
                if (event.getLine(1).equalsIgnoreCase("Start"))
                {
                    if (!Perm.sign_create_add.check(player))
                    {
                        event.setCancelled(true);
                        return;
                    }
                    if (Util.convertTimeToMillis(event.getLine(2)) < 0)
                    {
                        player.sendMessage(t("event_sign_fail"));
                        event.setCancelled(true);
                        return;
                    }
                    event.setLine(1, "Start");
                }
                else
                {
                    if (event.getLine(1).equalsIgnoreCase("List")||event.getLine(1).equalsIgnoreCase("AuctionSearch"))
                    {
                        if (!Perm.sign_create_list.check(player))
                        {
                            event.setCancelled(true);
                            return;
                        }
                        if (Material.matchMaterial(event.getLine(2))!=null)
                            event.setLine(2, Material.matchMaterial(event.getLine(2)).toString());
                        else
                            event.setLine(2, "# All #");
                        event.setLine(1, "AuctionSearch");
                        event.setLine(3, "");
                    }
                    else
                    {

                        player.sendMessage(t("event_sign_fail"));
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            player.sendMessage(t("event_sign_create"));            
            event.setLine(0, "[AuctionHouse]");
        }
    }
    
/**
 * prevent destroying signs
 */    
    @EventHandler
    public void onBreakBlock(BlockBreakEvent event)
    {
        final Player player = event.getPlayer();
        final Block signblock = event.getBlock();
        if (!player.isSneaking())
        {
            if (signblock.getType().equals(Material.WALL_SIGN))
            {
                Sign sign = (Sign)signblock.getState();
                if (sign.getLine(0).equals("[AuctionHouse]"))
                {
                    event.setCancelled(true);
                    signblock.getState().update();
                    return;
                }
            }
            for(BlockFace face: BlockFace.values())
            {
                if (event.getBlock().getRelative(face).getType().equals(Material.WALL_SIGN))
                {
                    Sign sign = (Sign)event.getBlock().getRelative(face).getState();
                    if (sign.getLine(0).equalsIgnoreCase("[AuctionHouse]"))
                    {    
                        if  (sign.getRawData()==0x2 && face.equals(BlockFace.EAST))  {event.setCancelled(true); return;}
                        if  (sign.getRawData()==0x3 && face.equals(BlockFace.WEST))  {event.setCancelled(true); return;}
                        if  (sign.getRawData()==0x4 && face.equals(BlockFace.NORTH)) {event.setCancelled(true); return;}
                        if  (sign.getRawData()==0x5 && face.equals(BlockFace.SOUTH)) {event.setCancelled(true); return;}
                    }    
                }
            }
        }
    }
    
/**
 * handles RightClick on Auctionsigns
 */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        if (block == null)
        {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if (block.getType().equals(Material.WALL_SIGN))
            {
                Sign sign = (Sign)block.getState();
                if (sign.getLine(0).equals("[AuctionHouse]"))
                {
                    event.setCancelled(true);
                    if ((sign).getLine(1).equals("AuctionBox"))
                    {
                        //AuktionBox GetItems
                        if (!Perm.sign_auctionbox.check(player)) return;
                        if (!(Bidder.getInstance(player).getBox().giveNextItem()))
                        {
                            player.sendMessage(t("i")+" "+t("time_sign_empty"));
                        }
                    }
                    if (sign.getLine(1).equals("Start"))
                    {
                        if (player.getItemInHand().getType().equals(Material.AIR))
                        {
                            player.sendMessage(t("pro")+" "+t("add_sell_hand"));
                            return;
                        }
                        //AuktionBox Start Auktion
                        if (!Perm.sign_start.check(player)) return;
                        Double startbid;
                        Integer length = Util.convertTimeToMillis(sign.getLine(2));
                        if (length == null)
                        return;
                        try
                        {
                            startbid = Double.parseDouble(sign.getLine(3));
                        }
                        catch (NumberFormatException ex)
                        {
                            startbid = 0.0;
                        }
                        if (startbid == null) startbid = 0.0;

                        for (ItemStack item : config.auction_blacklist)
                        {
                            if (item.getType().equals(player.getItemInHand().getType()))
                            {
                                player.sendMessage(t("e")+" "+t("add_blacklist"));
                                return;
                            }
                        }

                        Auction newAuction = new Auction(player.getItemInHand(), 
                                                        Bidder.getInstance(player),
                                                        System.currentTimeMillis()+length,
                                                        startbid);
                        if (!(Util.registerAuction(newAuction, player)))
                        {
                            player.sendMessage(t("i")+" "+t("add_max_auction",config.auction_maxAuctions_overall));
                        }
                        else
                        {
                            player.getInventory().removeItem(player.getItemInHand());
                            player.updateInventory();
                            player.sendMessage(t("i")+" "+t("add_start",1,
                                    newAuction.getItemType()+"x"+newAuction.getItemAmount(),
                                    econ.format(startbid),
                                    DateFormatUtils.format(newAuction.getAuctionEnd(), config.auction_timeFormat))); 
                        }    
                    }
                    if ((sign).getLine(1).equals("AuctionSearch"))
                    {
                        if (!Perm.sign_list.check(player)) return;
                        List<Auction> auctions;
                        if ((sign).getLine(2).equals("# All #"))
                        {
                            auctions = Manager.getInstance().getAuctions();
                            Sorter.DATE.sortAuction(auctions);
                        }   
                        else
                        {
                             auctions= Manager.getInstance().getAuctionItem(new ItemStack(Material.matchMaterial(sign.getLine(2)),1));
                            Sorter.DATE.sortAuction(auctions);
                        }
                        if (auctions.isEmpty())
                        {
                           player.sendMessage(t("no_detect"));
                           return;
                        }
                        Collections.reverse(auctions);
                        for (Auction auction : auctions)
                            Util.sendInfo(player, auction);
                    }
                }
            }
        }
    }
}
