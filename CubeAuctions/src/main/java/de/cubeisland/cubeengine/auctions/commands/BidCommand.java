package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.AbstractCommand;
import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.auctions.auction.ServerBidder;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.BaseCommand;
import de.cubeisland.cubeengine.auctions.CommandArgs;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.Perm;
import de.cubeisland.cubeengine.auctions.Sorter;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemStack;

/**
 * Bids on an auction
 * 
 * @author Faithcaio
 */
public class BidCommand extends AbstractCommand
{
    Economy econ = CubeAuctions.getInstance().getEconomy();
    
    public BidCommand(BaseCommand base)
    {
        super(base, "bid");
    }

    public boolean execute(CommandSender sender, CommandArgs args)
    {
        Manager manager = Manager.getInstance();
        if (!Perm.command_bid.check(sender)) return true;
        if (sender instanceof ConsoleCommandSender)
        {
            sender.sendMessage(t("bid_console"));
            return true;
        }

        Double bidAmount = null;
        Auction auction;
        Integer quantity;
        if (args.size() < 2)
        {
            sender.sendMessage(t("bid_title1"));
            sender.sendMessage(t("bid_title2"));
            sender.sendMessage(t("bid_title3"));
            sender.sendMessage(t("bid_use"));
            sender.sendMessage("");
            return true;
        }
        
        if (args.getString("i") != null)
        {
            if (args.getItem("i") != null)
            {
                ItemStack item = args.getItem("i");
                if (args.getString("q") == null)
                {
                    quantity = item.getMaxStackSize();
                }
                else
                {
                    if (args.getInt("q") == null)
                    {
                        sender.sendMessage(t("e")+" "+t("bid_quantity_num"));
                        return true;
                    }
                    if (args.getInt("q") < 1)
                    {
                        sender.sendMessage(t("e")+" "+t("bid_quantity"));
                        return true;
                    }
                    else
                    {
                        quantity = args.getInt("q");
                    }
                }
                List<Auction> auctions = manager.getAuctionItem(item,Bidder.getInstance(sender));
                
                if (auctions.isEmpty())
                {
                    sender.sendMessage(t("i")+" "+t("bid_no_auction",item.getType().toString()));
                    return true;
                }
                Sorter.QUANTITY.sortAuction(auctions, quantity);
                Sorter.PRICE.sortAuction(auctions);
                while (!auctions.isEmpty() && auctions.get(0).getOwner()==Bidder.getInstance(sender))
                    auctions.remove(0);
                
                if (auctions.isEmpty())
                {
                    sender.sendMessage(t("i")+" "+t("bid_no_auc_least",quantity,item.getType().toString()));
                    return true;
                }
                auction = auctions.get(0);//First is Cheapest after Sort
                if (args.getString(0)!=null)
                    bidAmount = args.getDouble(0);
                if (bidAmount != null)
                {
                    Bidder bidder = Bidder.getInstance(sender);
                    Bidder oldBidder = auction.getBids().peek().getBidder();
                    if (auction.bid(bidder, bidAmount))
                    {
                        bidder.addAuction(auction);
                        this.SendBidInfo(auction, sender);
                        if (oldBidder == bidder)
                            sender.sendMessage(t("i")+" "+t("bid_again"));
                        else
                            if (oldBidder != auction.getOwner())
                                if (oldBidder.hasNotifyState(Bidder.NOTIFY_STATUS))
                                    if (oldBidder.getPlayer()!=null)
                                        oldBidder.getPlayer().sendMessage(t("i")+" "+t("bid_over",auction.getId()));
                        return true;
                    }
                    return true;
                }
                else
                {
                    sender.sendMessage(t("e")+" "+t("bid_no_price"));
                    return true;
                }
            }
            else
            {
                sender.sendMessage(t("i")+t("no_valid_item",args.getString("i")));
                return true;
            }
        }
        Integer id = args.getInt(0);
        if (id != null)
        {
            if (args.getString(1)!=null)
                    bidAmount = args.getDouble(1);
            if (bidAmount != null)
            {
                if (manager.getAuction(id) == null)
                {
                    sender.sendMessage(t("i")+" "+t("auction_no_exist",id));
                    return true;
                }
                auction = manager.getAuction(id);
                Bidder bidder = Bidder.getInstance(sender);
                if (auction.getOwner() == bidder)
                {
                    sender.sendMessage(t("pro")+" "+t("bid_own"));
                    return true;
                }
                Bidder oldBidder = auction.getBids().peek().getBidder();
                if (auction.bid(bidder, bidAmount))
                {
                    bidder.addAuction(auction);
                    this.SendBidInfo(auction, sender);
                    if (oldBidder == bidder)
                        sender.sendMessage(t("i")+" "+t("bid_again"));
                    else
                        if (oldBidder != auction.getOwner())
                            if (oldBidder.hasNotifyState(Bidder.NOTIFY_STATUS))
                                if (oldBidder.getPlayer()!=null)
                                    oldBidder.getPlayer().sendMessage(t("i")+" "+t("bid_over",auction.getId()));
                    return true;
                }
                return true;
            }
            else
            {
                sender.sendMessage(t("e")+" "+t("bid_no_price"));
                return true;
            }
        }
        sender.sendMessage(t("e")+" " + t("bid_valid_id",args.getString(0)));
        return true;
    }

    public void SendBidInfo(Auction auction, CommandSender sender)
    {
        sender.sendMessage(t("bid_out",econ.format(auction.getBids().peek().getAmount()),
            auction.getItemType()+"x"+auction.getItemAmount(),auction.getId()));
        if (!(auction.getOwner() instanceof ServerBidder) && auction.getOwner().isOnline())
        {
            if (auction.getOwner().hasNotifyState(Bidder.NOTIFY_STATUS))
            {
                auction.getOwner().getPlayer().sendMessage(t("bid_owner",auction.getId()));
            }
        }
    }

    @Override
    public String getUsage()
    {
        return super.getUsage() + " <#ID> <amount>";
    }

    public String getDescription()
    {
        return t("command_bid");
    }
}
