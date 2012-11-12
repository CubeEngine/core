package de.cubeisland.cubeengine.auctions_old.commands;

import de.cubeisland.cubeengine.auctions_old.CubeAuctions;
import static de.cubeisland.cubeengine.auctions_old.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions_old.Perm;
import de.cubeisland.cubeengine.auctions_old.auction.Bidder;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Gives you the next Item from your auctionbox
 */
public class GetItemsCommand
{
    public GetItemsCommand()
    {
    }

    @Command(usage = "", aliases =
    {
        "get"
    })
    public boolean getItems(CommandSender sender, CommandArgs args)
    {
        if (!Perm.command_getItems.check(sender))
        {
            return true;
        }
        if (sender instanceof ConsoleCommandSender)
        {
            CubeAuctions.log("Console can not receive Items");
            return true;
        }

        if (!(Bidder.getInstance((Player) sender).getBox().giveNextItem()))
        {
            sender.sendMessage(t("get_empty"));
        }
        return true;
    }

    public String getDescription()
    {
        return t("command_get");
    }
}
