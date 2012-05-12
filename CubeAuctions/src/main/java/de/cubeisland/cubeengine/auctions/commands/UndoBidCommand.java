package de.cubeisland.cubeengine.auctions.commands;

import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.Perm;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Undoing Bids
 * 
 * @author Faithcaio
 */
public class UndoBidCommand
{
    public UndoBidCommand()
    {
    }

    @Command(usage = "last")
    public boolean undobid(CommandSender sender, CommandArgs args)
    {
        if (!Perm.command_undobid.check(sender)) return true;
        if (args.isEmpty())
        {
            sender.sendMessage(t("undo_title1"));
            sender.sendMessage(t("undo_title2"));
            sender.sendMessage(t("undo_title3"));
            sender.sendMessage("");
            return true;
        }
        Player psender = (Player) sender;
        if (args.getString(0).equals("last"))
        {
            Bidder bidder = Bidder.getInstance(psender);
            if (bidder.getlastAuction(bidder)==null)
            {
                sender.sendMessage(t("pro")+" "+t("undo_pro"));
                return true;
            }
            if (bidder.getlastAuction(bidder).undobid(bidder))
            {
                sender.sendMessage(t("i")+" "+t("undo_redeem"));
                return true;
            }
            else
            {
                sender.sendMessage(t("pro")+" "+t("undo_pro"));
                return true;
            }
        }
        try
        {
            int id = args.getInt(0);
            Manager manager = Manager.getInstance();
            if (manager.getAuction(id) == null)
            {
                sender.sendMessage(t("e")+" "+t("auction_no_exist",id));
                return true;
            }
            if (manager.getAuction(id).undobid(Bidder.getInstance(psender)))
            {
                sender.sendMessage(t("i")+" "+t("undo_bid_n",id));
                return true;
            }
            else return true;
        }
        catch (NumberFormatException ex)
        {
            sender.sendMessage(t("e")+" "+t("undo_fail"));
            return true;
        }
    }

    public String getDescription()
    {
        return t("command_undo");
    }
}
