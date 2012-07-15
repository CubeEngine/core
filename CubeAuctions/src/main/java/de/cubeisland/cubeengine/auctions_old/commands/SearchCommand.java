package de.cubeisland.cubeengine.auctions_old.commands;

import de.cubeisland.cubeengine.auctions_old.CubeAuctions;
import static de.cubeisland.cubeengine.auctions_old.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions_old.CubeAuctionsConfiguration;
import de.cubeisland.cubeengine.auctions_old.Manager;
import de.cubeisland.cubeengine.auctions_old.Perm;
import de.cubeisland.cubeengine.auctions_old.Sorter;
import de.cubeisland.cubeengine.auctions_old.Util;
import de.cubeisland.cubeengine.auctions_old.auction.Auction;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import java.util.Collections;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

/**
 * Searchs for auctions
 * 
 * @author Anselm Brehme
 */
public class SearchCommand
{
    
    private static final CubeAuctions plugin = CubeAuctions.getInstance();
    private static final CubeAuctionsConfiguration config = CubeAuctions.getConfiguration();
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
/*
        if (args.getString(0) == null)
        {
            if (args.getString("s")!=null)
               sender.sendMessage(t("pro")+" "+t("search_pro")); 
            sender.sendMessage(t("e")+" "+t("invalid_com"));
            return true;
        }
        * 
        */
        ItemStack item = Util.convertItem(args.getString(0));
        if (item == null)
        {
            sender.sendMessage(t("e")+" "+t("item_no_exist",args.getString(0)));
            return true;
        }
        auctionlist = Manager.getInstance().getAuctionItem(item);
        if (args.hasFlag("s"))
        {
            if (args.getString(1).equalsIgnoreCase("date"))
            {
                Sorter.DATE.sortAuction(auctionlist);
                Collections.reverse(auctionlist);
            }
            if (args.getString(1).equalsIgnoreCase("id"))
            {
                Sorter.ID.sortAuction(auctionlist);
                Collections.reverse(auctionlist);
            }
            if (args.getString(1).equalsIgnoreCase("price"))
            {
                Sorter.DATE.sortAuction(auctionlist);
                Sorter.PRICE.sortAuction(auctionlist);
                Collections.reverse(auctionlist);
            }
        }
        if (Manager.getInstance().getPrice(item)==0)
            sender.sendMessage(t("search_item1",item.getType().toString())+" "+t("search_item2"));
        else
            sender.sendMessage(t("search_item1",item.getType().toString())+" "+
                               t("search_item3",String.valueOf(Manager.getInstance().getPrice(item))));
        Util.sendInfo(sender, auctionlist);
        return true;
    }
}
