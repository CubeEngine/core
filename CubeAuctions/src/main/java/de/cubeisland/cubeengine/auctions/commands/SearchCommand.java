package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.CommandArgs;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.CubeAuctionsConfiguration;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.Perm;
import de.cubeisland.cubeengine.auctions.Sorter;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.libMinecraft.command.Command;
import java.util.Collections;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;

/**
 * Searchs for auctions
 * 
 * @author Faithcaio
 */
public class SearchCommand
{
    
    private static final CubeAuctions plugin = CubeAuctions.getInstance();
    private static final CubeAuctionsConfiguration config = plugin.getConfiguration();
    Economy econ = CubeAuctions.getInstance().getEconomy();
    
    public SearchCommand()
    {
    }

    @Command(usage = "<Item>")
    public boolean search(CommandSender sender, CommandArgs args)
    {
        if (!Perm.command_search.check(sender)) return true;
        if (args.isEmpty())
        {
            sender.sendMessage(t("search_title1"));
            sender.sendMessage(t("search_title2"));
            sender.sendMessage("");
            return true;
        }
        List<Auction> auctionlist;

        if (args.getString(0) == null)
        {
            if (args.getString("s")!=null)
               sender.sendMessage(t("pro")+" "+t("search_pro")); 
            sender.sendMessage(t("e")+" "+t("invalid_com"));
            return true;
        }
        if (args.getItem(0) == null)
        {
            sender.sendMessage(t("e")+" "+t("item_no_exist",args.getString(0)));
            return true;
        }
        auctionlist = Manager.getInstance().getAuctionItem(args.getItem(0));
        if (args.getString("s") != null)
        {
            if (args.getString("s").equalsIgnoreCase("date"))
            {
                Sorter.DATE.sortAuction(auctionlist);
                Collections.reverse(auctionlist);
            }
            if (args.getString("s").equalsIgnoreCase("id"))
            {
                Sorter.ID.sortAuction(auctionlist);
                Collections.reverse(auctionlist);
            }
            if (args.getString("s").equalsIgnoreCase("price"))
            {
                Sorter.DATE.sortAuction(auctionlist);
                Sorter.PRICE.sortAuction(auctionlist);
                Collections.reverse(auctionlist);
            }
        }
        if (Manager.getInstance().getPrice(args.getItem(0))==0)
            sender.sendMessage(t("search_item1",args.getItem(0).getType().toString())+" "+t("search_item2"));
        else
            sender.sendMessage(t("search_item1",args.getItem(0).getType().toString())+" "+
                               t("search_item3",String.valueOf(Manager.getInstance().getPrice(args.getItem(0)))));
        Util.sendInfo(sender, auctionlist);
        return true;
    }
}
