package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.CubeAuctionsConfiguration;
import de.cubeisland.cubeengine.auctions.Perm;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Adds an auction.
 * 
 * @author Faithcaio
 */

public class AddCommand
{
    private static final CubeAuctions plugin = CubeAuctions.getInstance();
    private static final CubeAuctionsConfiguration config = CubeAuctions.getConfiguration();
    Economy econ = CubeAuctions.getInstance().getEconomy();

    public AddCommand(){}
    
    @Command(usage = "<Item> <Amount>")
    public boolean add(CommandSender sender, CommandArgs args)
    {
        ItemStack newItem;
        Integer amount;
        Double startBid;
        long auctionEnd;
        Integer multiAuction = 1;
        if (!Perm.command_add.check(sender)) return true;
        if (args.isEmpty())
        {
            sender.sendMessage(t("add_title1"));
            sender.sendMessage(t("add_title2"));
            sender.sendMessage(t("add_title3"));
            sender.sendMessage(t("add_use"));
            sender.sendMessage("");
            return true;
        }
        int pos;
        if (args.getString(0).equalsIgnoreCase("hand"))
        {
            if (!(sender instanceof ConsoleCommandSender))
            {
                newItem = ((Player) sender).getItemInHand();
                if (newItem.getType() == Material.AIR)
                {
                    sender.sendMessage(t("pro")+" "+t("add_sell_hand"));
                    return true;
                }
                pos = 1;
            }
            else
            {
                sender.sendMessage(t("pro")+t("add_server_nohand"));
                return true;
            }
        }
        else
        {
            newItem = Util.convertItem(args.getString(0));
            if (newItem==null)
            {
                sender.sendMessage(t("e")+" "+t("add_invalid_item",args.getString(0)));
                return true;
            }
            if (newItem.getType().equals(Material.AIR))
            {
                sender.sendMessage(t("i") +t("add_invalid_item","AIR"));//Why dont you try to sell your hands?
                return true;
            }
            if (args.size()<2) 
            {
                sender.sendMessage(t("e")+ t("too_few_args"));
                return true;
            }
            try {amount = args.getInt(1);}
            catch (NumberFormatException ex)
            {
                sender.sendMessage(t("i") + " " + t("add_no_amount"));
                return true;
            }
            newItem.setAmount(amount);
            pos = 2;
        }
        if ((args.size() > pos)
        && (args.getString(pos+0)) != null)
        {
            Integer length = Util.convertTimeToMillis(args.getString(pos+0));
            if (length == -1)
            {
                sender.sendMessage(t("e") + " " + t("add_invalid_length"));
                return true;
            }
            if (length <= config.auction_maxLength)
            {
                auctionEnd = (System.currentTimeMillis() + length);
            }
            else
            {
                sender.sendMessage(t("i")+" "+t("add_max_length",Util.convertTime(config.auction_maxLength)));
                return true;
            }
        }
        else
        {
            auctionEnd = (System.currentTimeMillis() + config.auction_standardLength);
        }
        if ((args.size() > pos+1)
        && (args.getString(pos+1)) != null)
        {
            startBid = args.getDouble(pos+1);
            if (startBid == null)
            {
                sender.sendMessage(t("i") + " "+t("add_invalid_startbid"));
                return true;
            }
        }
        else
        {
            startBid = 0.0;
        }
        pos += 2;
        if (args.size() > pos)
            if (args.getString(pos).contains("m:"))
            {
                try
                {
                    multiAuction = Integer.valueOf(args.getString(pos).substring(2));
                }
                catch (NumberFormatException ex)
                {
                    sender.sendMessage(t("i")+" "+t("add_multi_number"));
                    return true;
                }
                if (!Perm.command_add_multi.check(sender)) return true;
            }

        if (sender instanceof ConsoleCommandSender)
        {
            sender.sendMessage(t("i")+" "+t("add_server_create"));
        }

        if (newItem == null)
        {
            sender.sendMessage(t("pro")+" "+t("add_server_nohand"));
            return true;
        }
        ItemStack removeItem = newItem.clone();
        removeItem.setAmount(removeItem.getAmount() * multiAuction);

        if (!(sender instanceof ConsoleCommandSender))
        {
            if (!((Player) sender).getInventory().contains(removeItem.getType(), removeItem.getAmount()))
            {
                if (Perm.command_add_cheatItems.check(sender))
                {
                    sender.sendMessage(t("i")+" "+t("add_enough_item")+" "+t("add_cheat"));
                }
                else
                {
                    sender.sendMessage(t("i")+" "+t("add_enough_item"));
                    return true;
                }
            }
        }

        for (ItemStack item : config.auction_blacklist)
        {
            if (item.getType().equals(newItem.getType()))
            {
                sender.sendMessage(t("e")+" "+t("add_blacklist"));
                return true;
            }
        }
        Auction newAuction;
        for (int i = 0; i < multiAuction; ++i)
        {
            if (sender instanceof ConsoleCommandSender || 
               (args.hasFlag("s")&&(sender.hasPermission("auctionhouse.command.add.server"))))
            {
                newAuction = new Auction(newItem, Bidder.getInstance(0), auctionEnd, startBid);
            }
            else
            {
                newAuction = new Auction(newItem, Bidder.getInstance((Player) sender), auctionEnd, startBid);//Created Auction
            }
            if (sender.hasPermission("auctionhouse.command.add.nolomit"))
            {
                if (Bidder.getInstance(sender).getOwnAuctions().size()>=config.auction_maxAuctions_player)
                {
                    sender.sendMessage(t("i")+" "+t("add_max_auction",config.auction_maxAuctions_player));
                    return true;
                }    
            }
            if (!(Util.registerAuction(newAuction, sender)))
            {
                sender.sendMessage(t("i")+" "+t("add_all_stop"));
                sender.sendMessage(t("i")+" "+t("add_max_auction",config.auction_maxAuctions_overall));
                return true;
            }
            if (!(sender instanceof ConsoleCommandSender))
            {

                ((Player) sender).getInventory().removeItem(removeItem);
            }
            else
            {
                CubeAuctions.log("ServerAuction(s) added succesfully!");
            }
        }

        sender.sendMessage(t("i")+" "+t("add_start",
                                        multiAuction,
                                        newItem.getType().toString()+"x"+newItem.getAmount(),
                                        econ.format(startBid),
                                        DateFormatUtils.format(auctionEnd, config.auction_timeFormat)));                     
        return true;
    }

    public String getDescription()
    {
        return t("command_add");
    }
}