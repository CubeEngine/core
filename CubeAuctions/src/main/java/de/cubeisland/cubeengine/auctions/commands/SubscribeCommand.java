package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Manages your Subscriptions
 * 
 * @author Faithcaio
 */
public class SubscribeCommand
{
    public SubscribeCommand()
    {
    }
    
    @Command(usage = "-m <Material>", aliases = {"sub"})
    public boolean subscribe(CommandSender sender, CommandArgs args)
    {
        if (args.isEmpty())
        {
            sender.sendMessage(t("sub_title1"));
            sender.sendMessage(t("sub_title2"));
            sender.sendMessage(t("sub_title3"));
            sender.sendMessage(t("unsub_title2"));
            sender.sendMessage(t("unsub_title3"));
            sender.sendMessage("");
            return true;
        }
        if (sender instanceof ConsoleCommandSender)
        {
            CubeAuctions.log("Console can not subcribe");
            return true;
        }
        Bidder bidder = Bidder.getInstance((Player) sender);
        if (args.hasFlag("m"))
        {
            ItemStack item =  Util.convertItem(args.getString(0));
            if (item != null)
            {
                if (bidder.addSubscription(item.getType()))
                {
                    sender.sendMessage(t("i")+t("sub_add_mat",item.getType().toString()));
                    if (!bidder.hasNotifyState(Bidder.NOTIFY_STATUS))
                    {
                        sender.sendMessage(t("i")+t("sub_note"));
                    } 
                }
                sender.sendMessage(t("i")+t("sub_has_mat"));
                return true;
            }
            sender.sendMessage(t("i")+t("no_valid_item",args.getString(0)));
            return true;
        }
        else if (args.hasFlag("i"))
        {
            try
            {
                int id = args.getInt(0);
                Manager manager = Manager.getInstance();
                if (manager.getAuction(id) != null)
                {
                    if (bidder.addSubscription(manager.getAuction(id)))
                    {
                        sender.sendMessage(t("i")+t("sub_add",id));
                        if (!bidder.hasNotifyState(Bidder.NOTIFY_STATUS))
                        {
                            sender.sendMessage(t("i")+t("sub_note"));
                        }
                    }
                    sender.sendMessage(t("i")+t("sub_has"));
                    return true;
                }
                sender.sendMessage(t("e")+t("auction_no_exist",id));
                return true;
            }
            catch (NumberFormatException ex)
            {
                sender.sendMessage(t("e")+t("invalid_id"));
                return true;
            }
        }
        sender.sendMessage(t("e")+t("invalid_com"));
        return true;
    }



    public String getDescription()
    {
        return t("command_sub");
    }
}