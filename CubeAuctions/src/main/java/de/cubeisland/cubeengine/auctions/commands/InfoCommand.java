package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.CommandArgs;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.CubeAuctionsConfiguration;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.Perm;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.libMinecraft.command.Command;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Provides Info for Auctions
 * 
 * @author Faithcaio
 */
public class InfoCommand
{
    
    private static final CubeAuctions plugin = CubeAuctions.getInstance();
    private static final CubeAuctionsConfiguration config = CubeAuctions.getConfiguration();
    
    
    public InfoCommand()
    {
    }

    @Command(usage = "<AuctionId>")
    public boolean info(CommandSender sender, CommandArgs args)
    {
        if (args.isEmpty())
        {
            sender.sendMessage(t("info_title1"));
            sender.sendMessage(t("info_title2"));
            sender.sendMessage(t("info_title3"));
            sender.sendMessage(t("info_title4"));
            sender.sendMessage(t("info_title5"));
            sender.sendMessage(t("info_title6"));
            sender.sendMessage(t("info_title7"));
            sender.sendMessage(t("info_title8"));
            sender.sendMessage("");
            return true;
        }
        if (!Perm.command_info.check(sender)) return true;
        if (args.getString(0).equalsIgnoreCase("Bids"))//bidding
        {
            List<Auction> auctions = Bidder.getInstance((Player) sender).getAuctions();
            int max = auctions.size();
            if (max == 0)
            {
                sender.sendMessage(t("i")+" "+t("info_no_bid"));
            }
            for (int i = 0; i < max; ++i)
            {
                Auction auction = auctions.get(i);
                if (!auction.getOwner().getPlayer().equals((Player)sender))
                {
                    Util.sendInfo(sender, auction);
                }
            }
        }
        else
        {
            if (args.getString(0).equalsIgnoreCase("own"))
            {
                List<Auction> auctions = Bidder.getInstance((Player) sender).getOwnAuctions();
                int max = auctions.size();
                if (max == 0)
                {
                    sender.sendMessage(t("i")+" "+t("info_no_start"));
                }
                for (int i = 0; i < max; ++i)
                {
                    Auction auction = auctions.get(i);
                    Util.sendInfo(sender, auction);
                }
            }
            else
            {
                if (args.getString(0).equalsIgnoreCase("sub"))
                {
                    List<Auction> auctions = Bidder.getInstance((Player) sender).getSubs();
                    auctions.removeAll(Bidder.getInstance(sender).getOwnAuctions());
                    
                    int max = auctions.size();
                    if (max == 0)
                    {
                        sender.sendMessage(t("i")+" "+t("info_no_sub"));
                    }
                    for (int i = 0; i < max; ++i)
                    {
                        Auction auction = auctions.get(i);
                        Util.sendInfo(sender, auction);
                    }
                }
                else    
                {

                    if (args.getString(0).equalsIgnoreCase("lead"))
                    {
                        List<Auction> auctions = Bidder.getInstance((Player) sender).getLeadingAuctions();
                        auctions.removeAll(Bidder.getInstance(sender).getOwnAuctions());
                        int max = auctions.size();
                        if (max == 0)
                        {
                            sender.sendMessage(t("i")+" "+t("info_no_lead"));
                        }
                        for (int i = 0; i < max; ++i)
                        {
                            Auction auction = auctions.get(i);
                            Util.sendInfo(sender, auction);
                        }
                    }
                    else
                    {
                        if (args.getString(0).equalsIgnoreCase("*Server"))
                        {
                            List<Auction> auctions = Bidder.getInstance(0).getAuctions();
                            int max = auctions.size();
                            if (max == 0)
                            {
                                sender.sendMessage(t("i")+" "+t("info_no_serv"));
                            }
                            for (int i = 0; i < max; ++i)
                            {
                                Auction auction = auctions.get(i);
                                Util.sendInfo(sender, auction);
                            }
                        }
                        else
                        {
                            Integer id = args.getInt(0);
                            if (id != null)
                            {
                                if (Manager.getInstance().getAuction(id) != null)
                                {
                                    Util.sendInfo(sender, Manager.getInstance().getAuction(id));
                                }
                                else
                                {
                                    sender.sendMessage(t("i")+" "+t("auction_no_exist",id));
                                }
                            }
                            else
                            {
                                if (!Perm.command_info_others.check(sender)) return true;
                                Bidder player = args.getBidder(0);
                                if (player != null)
                                {
                                    List<Auction> auctions = player.getAuctions(player);
                                    int max = auctions.size();
                                    if (max == 0)
                                    {
                                        sender.sendMessage(t("e")+t("info_no_auction",player.getName()));
                                    }
                                    for (int i = 0; i < max; ++i)
                                    {
                                        Auction auction = auctions.get(i);
                                        Util.sendInfo(sender, auction);
                                    }
                                }
                                else
                                {
                                    sender.sendMessage(t("e")+" "+t("info_p_no_auction",args.getString(0)));
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }


    public String getDescription()
    {
        return t("command_info");
    }
}
