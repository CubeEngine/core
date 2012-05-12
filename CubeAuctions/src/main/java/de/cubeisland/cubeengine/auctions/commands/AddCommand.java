package de.cubeisland.cubeengine.auctions.commands;


import de.cubeisland.cubeengine.auctions.CommandArgs;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.CubeAuctionsConfiguration;
import de.cubeisland.cubeengine.auctions.Perm;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.libMinecraft.command.Command;
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
        ItemStack newItem = null;
        Material newMaterial;
        Integer amount;
        Double startBid = 0.0;
        long auctionEnd = 1;
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

        if (args.getString("m") != null)
        {
            multiAuction = args.getInt("m");
            if (multiAuction == null)
            {
                sender.sendMessage(t("i")+" "+t("add_multi_number"));
                return true;
            }
            if (!Perm.command_add_multi.check(sender)) return true;
        }
        if (args.getString(0)==null)
        {
            sender.sendMessage(t("e")+" "+t("invalid_com"));
            return true;
        }
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
                if (args.getString(1) != null)
                {
                    Integer length = Util.convertTimeToMillis(args.getString(1));
                    if (length == null)
                    {
                        sender.sendMessage(t("e")+" "+t("add_invalid_length"));
                        return true;
                    }
                    if (length <= config.auction_maxLength)
                    {
                        auctionEnd = (System.currentTimeMillis() + length);
                    }
                    else
                    {
                        sender.sendMessage(t("i")+" "+t("add_max_length",
                                DateFormatUtils.format(config.auction_maxLength, "dd:hh:mm:ss")));
                        return true;
                    }
                }
                else
                {
                    auctionEnd = (System.currentTimeMillis() + config.auction_standardLength);
                }
                if (args.getString(2) != null)
                {
                    startBid = args.getDouble(2);
                    if (startBid == null)
                    {
                        sender.sendMessage(t("i")+" "+t("add_invalid_time"));
                        return true;
                    }
                }
                else
                {
                    startBid = 0.0;
                }
            }
        }
        else
        {
            if (args.getItem(0)==null)
            {
                sender.sendMessage(t("e")+" "+t("add_invalid_item",args.getString(0)));
                return true;
            }
           
            newMaterial = args.getItem(0).getType();
            if (newMaterial == null)
            {
                sender.sendMessage(t("i") + " " +t("add_invalid_item",args.getString(0)));
                return true;
            }
            if (newMaterial.equals(Material.AIR))
            {
                sender.sendMessage(t("i") +t("add_invalid_item","AIR"));
                return true;
            }
            amount = args.getInt(1);
            if (amount == null)
            {
                sender.sendMessage(t("i") + " " + t("add_no_amount"));
                return true;
            }
            
            newItem = new ItemStack(newMaterial, amount);
            newItem.setDurability(args.getItem(0).getDurability());
            if (args.getString(2) != null)
            {
                Integer length = Util.convertTimeToMillis(args.getString(2));
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
            if (args.getString(3) != null)
            {
                startBid = args.getDouble(3);
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
            if (sender instanceof ConsoleCommandSender)
            {
                newAuction = new Auction(newItem, Bidder.getInstance(0), auctionEnd, startBid);
            }
            else
            {
                newAuction = new Auction(newItem, Bidder.getInstance((Player) sender), auctionEnd, startBid);//Created Auction
            }
            if (args.getString("s") != null)
            {
                if (args.getString("s").equalsIgnoreCase("Server"))
                    if (sender.hasPermission("auctionhouse.command.add.server"))
                    {
                        newAuction.giveServer();
                        if (!(Util.registerAuction(newAuction, Bidder.getInstance(0))))
                        {
                            sender.sendMessage(t("i")+" "+t("add_all_stop"));
                            sender.sendMessage(t("i")+" "+t("add_max_auction",config.auction_maxAuctions_overall));
                            return true;
                        }
                    }
            }
            else
            {
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