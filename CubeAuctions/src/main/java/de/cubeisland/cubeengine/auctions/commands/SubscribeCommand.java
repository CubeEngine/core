package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.CommandArgs;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.libMinecraft.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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
    
    @Command(usage = "m:<Material>", aliases = {"sub"})
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
        if (args.getString("m") != null)
        {
            if (args.getItem("m") != null)
            {
                if (bidder.addSubscription(args.getItem("m").getType()))
                {
                    sender.sendMessage(t("i")+" "+t("sub_add_mat",args.getItem("m").getType().toString()));
                    if (!bidder.hasNotifyState(Bidder.NOTIFY_STATUS))
                    {
                        sender.sendMessage(t("i")+" "+t("sub_note"));
                    } 
                }
                return true;
            }
            sender.sendMessage(t("i")+" "+args.getString("m") + " "+t("no_valid_item"));
            return true;
        }
        if (args.getString("i") != null)
        {
            if (args.getInt("i") != null)
            {
                Manager manager = Manager.getInstance();
                if (manager.getAuction(args.getInt("i")) != null)
                {
                    if (bidder.addSubscription(manager.getAuction(args.getInt("i"))))
                    {
                        sender.sendMessage(t("i")+" "+t("sub_add",args.getInt("i")));
                        if (!bidder.hasNotifyState(Bidder.NOTIFY_STATUS))
                        {
                            sender.sendMessage(t("i")+" "+t("sub_note"));
                        }
                    }
                    return true;
                }
                sender.sendMessage(t("e")+" "+t("auction_no_exist",args.getInt("i")));
                return true;
            }
            sender.sendMessage(t("e")+" "+t("invalid_id"));
            return true;
        }
        sender.sendMessage(t("e")+" "+t("invalid_com"));
        return true;
    }



    public String getDescription()
    {
        return t("command_sub");
    }
}