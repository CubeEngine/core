package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.auction.Auction;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

/**
 * Confirms a requested action
 * 
 * @author Faithcaio
 */
public class ConfirmCommand
{
    public ConfirmCommand()
    {

    }
    @Command(usage = "")
    public boolean confirm(CommandSender sender, CommandArgs args)
    {
        Manager manager = Manager.getInstance();
        Bidder bidder = Bidder.getInstance(sender);
        if (manager.getAllConfirm().contains(bidder))
        {
            manager.getAllConfirm().remove(bidder);
            int max = manager.size();
            if (max == 0)
            {
                sender.sendMessage(t("i")+" "+t("no_detect"));
                return true;
            }
            for (int i = max - 1; i >= 0; --i)
            {
                manager.cancelAuction(manager.getIndexAuction(i), false);
            }
            sender.sendMessage(t("i")+" "+t("confirm_del"));
            return true;
        }
        if (manager.getBidderConfirm().containsKey(bidder))
        {
            if (manager.getBidderConfirm().get(bidder).isServerBidder())
            {
                int max = Bidder.getInstance(1).getAuctions().size();
                if (max == 0)
                {
                    sender.sendMessage(t("i")+" "+t("confirm_no_serv"));
                    manager.getBidderConfirm().remove(bidder);
                    return true;
                }
                for (int i = max - 1; i >= 0; --i)
                {
                    manager.cancelAuction(Bidder.getInstance(0).getAuctions().get(i), false);
                }
                sender.sendMessage(t("i")+" "+t("confirm_del_serv"));
                manager.getBidderConfirm().remove(bidder);
                return true;
            }
            else
            {
                Bidder player = manager.getBidderConfirm().get(bidder);
                ArrayList<Auction> auctions = (ArrayList<Auction>)player.getActiveBids().clone();
                int max = auctions.size();
                for (Auction auction : auctions)
                {
                    if (auction.getOwner() == player)
                    {
                        if (CubeAuctions.getConfiguration().auction_removeTime <
                            System.currentTimeMillis() - auction.getBids().firstElement().getTime())
                        {
                            if (!sender.hasPermission("aucionhouse.delete.player.other"))
                                {
                                     sender.sendMessage(t("i")+" "+t("rem_time"));
                                     --max;
                                     continue;
                                }
                           
                        }
                        manager.cancelAuction(auction , false);
                    }
                }
                if (max!=0)
                    sender.sendMessage(t("i")+" "+t("confirm_rem",max,player.getName()));
                manager.getBidderConfirm().remove(bidder);
                return true;
            }
        }
        if (manager.getSingleConfirm().containsKey(bidder))
        {
            ItemStack item = Manager.getInstance().getAuction(manager.getSingleConfirm().get(bidder)).getItemStack();
            manager.cancelAuction(manager.getAuction(manager.getSingleConfirm().get(bidder)), false);
            sender.sendMessage(t("i")+" "+t("rem_id",manager.getSingleConfirm().get(bidder),item.getType().toString()+"x"+item.getAmount()));
            manager.getBidderConfirm().remove(bidder);
            return true;
        }
        sender.sendMessage(t("e")+" "+t("confirm_no_req"));
        return true;
    }

    public String getDescription()
    {
        return t("command_confirm");
    }
}