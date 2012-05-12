package de.cubeisland.cubeengine.auctions.commands;



import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.Perm;
import de.cubeisland.cubeengine.auctions.auction.Bidder;
import de.cubeisland.cubeengine.auctions.database.BidderStorage;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Changes notification
 * 
 * @author Faithcaio
 */
public class NotifyCommand
{
    private BidderStorage bidderDB = new BidderStorage();
    public NotifyCommand()
    {
    }
    
    @Command(usage = "toggle", aliases = {"n"})
    public boolean notify(CommandSender sender, CommandArgs args)
    {
        if (args.isEmpty())
        {
            sender.sendMessage(t("note_title1"));
            sender.sendMessage(t("note_title2"));
            sender.sendMessage(t("note_title3"));
            sender.sendMessage(t("note_title4"));
            sender.sendMessage("");
            return true;
        }
        if (!Perm.command_notify.check(sender)) return true;
        if (args.getString(0) == null)
        {
            return true;
        }
        if (sender instanceof ConsoleCommandSender)
        {
            CubeAuctions.log("Console can not use notification!");
            return true;
        }
        Bidder bidder = Bidder.getInstance((Player) sender);
        if (args.getString(0).equalsIgnoreCase("true") || args.getString(0).equalsIgnoreCase("on"))
        {
            bidder.setNotifyState(Bidder.NOTIFY_STATUS);
        }
        if (args.getString(0).equalsIgnoreCase("false") || args.getString(0).equalsIgnoreCase("off"))
        {
            bidder.unsetNotifyState(Bidder.NOTIFY_STATUS);
        }
        if (args.getString(0).equalsIgnoreCase("toggle") || args.getString(0).equalsIgnoreCase("t"))
        {
            bidder.toggleNotifyState(Bidder.NOTIFY_STATUS);
        }
        if (bidder.hasNotifyState(Bidder.NOTIFY_STATUS))
            sender.sendMessage(t("i")+" "+t("note_on"));
        else
            sender.sendMessage(t("i")+" "+t("note_off"));
        bidderDB.updateNotifyData(bidder);
        return true;
    }


    public String getDescription()
    {
        return t("command_note");
    }
}
