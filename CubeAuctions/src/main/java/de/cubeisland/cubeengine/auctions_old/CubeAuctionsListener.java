package de.cubeisland.cubeengine.auctions_old;

import static de.cubeisland.cubeengine.auctions_old.CubeAuctions.t;

import de.cubeisland.cubeengine.auctions_old.database.BidderStorage;
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
public class CubeAuctionsListener implements Listener
{
    private final CubeAuctions plugin;
    private final CubeAuctionsConfiguration config;
    private final Economy econ;
    private BidderStorage bidderDB = new BidderStorage();
    
    public CubeAuctionsListener(CubeAuctions plugin)
    {
        this.plugin = plugin;
        this.config = CubeAuctions.getConfiguration();
        this.econ = plugin.getEconomy();
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
