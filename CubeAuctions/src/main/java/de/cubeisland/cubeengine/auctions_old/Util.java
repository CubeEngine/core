package de.cubeisland.cubeengine.auctions_old;


import static de.cubeisland.cubeengine.auctions_old.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions_old.auction.Auction;
import de.cubeisland.cubeengine.auctions_old.auction.Bid;
import de.cubeisland.cubeengine.auctions_old.auction.Bidder;
import de.cubeisland.cubeengine.auctions_old.database.AuctionStorage;
import de.cubeisland.cubeengine.auctions_old.database.BidStorage;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * a lot of methods used everywhere
 * 
 * @author Faithcaio
 */
public class Util
{
    private static final CubeAuctions plugin = CubeAuctions.getInstance();
    //private static final AuctionHouseConfiguration config = plugin.getConfiguration();
    
/**
 * Converts Time in d | h | m | s  to Milliseconds
 */ 
    public static int convertTimeToMillis(String str) //ty quick_wango
    {
        Pattern pattern = Pattern.compile("^(\\d+)([smhd])?$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        matcher.find();

        int time;
        try
        {
            time = Integer.parseInt(String.valueOf(matcher.group(1)));
        }
        catch (IllegalStateException ex) {return -1;}
        catch (Throwable t)
        {
            CubeAuctions.error("Failed to convert to a number", t);
            return -1;
        }
        if (time < 0)
        {
            return -1;
        }
        String unitSuffix = matcher.group(2);
        if (unitSuffix == null)
        {
            unitSuffix = "m";
        }
        switch (unitSuffix.toLowerCase().charAt(0))
        {
            case 'd':
                time *= 24;
            case 'h':
                time *= 60;
            case 'm':
                time *= 60;
            case 's':
                time *= 1000;
        }
        return time;
    }
    
/**
 * register auction for sender
 */ 
    public static boolean registerAuction(Auction auction, CommandSender sender)
    {
        return registerAuction(auction, Bidder.getInstance(sender));
    }
    
/**
 * register auction for bidder
 */ 
    public static boolean registerAuction(Auction auction, Bidder owner)
    {
        if (Manager.getInstance().isEmpty())
        {
            return false;
        }
        Manager.getInstance().addAuction(auction);
        owner.addAuction(auction);

        for (Bidder bidder : Bidder.getInstances().values())
        {
            if (bidder.getMatSub().contains(auction.getItemStack().getType()))
            {
                if (!bidder.equals(auction.getOwner()))
                {
                    bidder.addSubscription(auction);
                    bidder.getPlayer().sendMessage(t("info_new",auction.getKey(),auction.getItemType()));
                }
            }
        }
        BidStorage bidDB = new BidStorage();
        AuctionStorage auctionDB = new AuctionStorage();
        auctionDB.store(auction);
        bidDB.store(auction.getBids().peek());
        return true;
    }

/**
 * send Info about auction to sender
 */ 
    public static void sendInfo(CommandSender sender, Auction auction)
    {
        Economy econ = plugin.getEconomy();
        String output = "";
        if (auction.getItemData()==0)
            output += t("info_out_1",auction.getKey(),auction.getItemType(),auction.getItemAmount());
        else
            output += t("info_out_11",auction.getKey(),auction.getItemType(),auction.getItemData(),auction.getItemAmount());
        if (auction.getItemStack().getEnchantments().size() > 0)
        {
            output += " "+t("info_out_ench");
            for (Enchantment enchantment : auction.getItemStack().getEnchantments().keySet())
            {
                output += " "+enchantment.getName() + ":";
                output += auction.getItemStack().getEnchantmentLevel(enchantment);
            }
        }
        Bid bid = auction.getBids().peek();
        if (bid.getBidder().equals(auction.getOwner()))
        {
            output += " "+t("info_out_bid",econ.format(bid.getAmount()));
        }
        else
        {
            if (bid.getBidder().isServerBidder())
            {
                output += " "+t("info_out_leadserv");
            }
            else
            {
                if (bid.getBidder().getName().equals(sender.getName()))
                    output += " "+t("info_out_lead",bid.getBidder().getName());
                else
                    output += " "+t("info_out_lead2",bid.getBidder().getName());
            }
            output +=" "+t("info_out_with",econ.format(bid.getAmount()));
        }
        CubeAuctionsConfiguration config = CubeAuctions.getConfiguration();
        if (auction.getAuctionEnd()-System.currentTimeMillis()>1000*60*60*24)
            output += " "+t("info_out_end",DateFormatUtils.format(auction.getAuctionEnd(), 
                            config.auction_timeFormat));
        else
            output += " " + t("info_out_end2", convertTime(auction.getAuctionEnd() - System.currentTimeMillis()));
        sender.sendMessage(output);
    }
    
    public static void sendInfo(CommandSender sender, List<Auction> auctionlist)
    {
        int max = auctionlist.size();
        if (max == 0)
        {
            sender.sendMessage(t("i")+" "+t("no_detect"));
        }
        for (int i = 0; i < max; ++i)
        {
            sendInfo(sender, auctionlist.get(i));
        }
    }
    
    public static String convertTime(long time)
    {
        if (TimeUnit.MILLISECONDS.toMinutes(time)==0)
            return t("less_time");
        return String.format("%dh %dm", 
            TimeUnit.MILLISECONDS.toHours(time),
            TimeUnit.MILLISECONDS.toMinutes(time) - 
            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time))
            );
    }
    
    public static String convertItem(ItemStack item)
    {
        String out = item.getTypeId()+":"+item.getDurability();
        if (!item.getEnchantments().isEmpty())
        {
            for (Enchantment ench : item.getEnchantments().keySet())
            {
                out += " "+ench.getId()+":"+item.getEnchantmentLevel(ench);
            }
        }
        return out;
    }
    
/**
 * convert Item as String / Integer to ItemStack
 */ 
    public static ItemStack convertItem (String in, int amount)
    {
        ItemStack out = convertItem(in);
        out.setAmount(amount);
        return out;
    }
    
/**
 * convert ItemStack to String
 */ 
    public static ItemStack convertItem(String in)
    {
        //id:data
        String mat = in;
        if (in.indexOf(":")!= -1)
            mat = in.substring(0,in.indexOf(":"));
        Material material = Material.matchMaterial(mat);
        if (material == null) return null;
        short data;
        if (in.indexOf(" ")==-1)
        {
            data = Short.valueOf(in.substring(in.indexOf(":")+1));
            in = "";
        }
            
        else
        {
            data = Short.valueOf(in.substring(in.indexOf(":")+1,in.indexOf(" ")));
            in.replace(in.substring(0, in.indexOf(" ")+1), "");
        }
        
        
        ItemStack out = new ItemStack(material,1,data);
        //ench1:val1 ench2:val2 ...
        while (in.length()>1)
        {
            int enchid = Integer.valueOf(in.substring(0,in.indexOf(":")));
            int enchval; 
            if (in.indexOf(" ")==-1)
            {
                enchval = Short.valueOf(in.substring(in.indexOf(":")+1));
                in = "";
            }
            else
            {
                enchval = Integer.valueOf(in.substring(in.indexOf(":")+1,in.indexOf(" ")));
                in.replace(in.substring(0, in.indexOf(" ")+1), "");
            }
            if (Enchantment.getById(enchid) != null)
            {
                out.addEnchantment(Enchantment.getById(enchid), enchval);
            }
        }  
        return out;
    }
}
