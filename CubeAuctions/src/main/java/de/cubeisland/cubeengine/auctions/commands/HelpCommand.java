package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.AbstractCommand;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.BaseCommand;
import de.cubeisland.cubeengine.auctions.CommandArgs;
import de.cubeisland.cubeengine.auctions.Perm;
import org.bukkit.command.CommandSender;

/**
 * This command prints a help message
 *
 * @author Phillip Schichtel
 * @author Faithcaio
 */
public class HelpCommand extends AbstractCommand
{
    public HelpCommand(BaseCommand base)
    {
        super(base, "help");
    }

    public boolean execute(CommandSender sender, CommandArgs args)
    {
        if (!Perm.use.check(sender)) return true;
        sender.sendMessage(t("help_list"));
        for (AbstractCommand command : getBase().getRegisteredCommands())
        {
            if (this.check(command,"add"))         if (!sender.hasPermission("auctionhouse.command.add")) continue;
            if (this.check(command,"bid"))         if (!sender.hasPermission("auctionhouse.command.bid")) continue;
            if (this.check(command,"confirm"))     continue;//if (!sender.hasPermission("auctionhouse.command.delete.id")) continue;
            if (this.check(command,"getItems","get"))    if (!sender.hasPermission("auctionhouse.command.getItems")) continue;
            if (this.check(command,"Info"))        if (!sender.hasPermission("auctionhouse.command.info")) continue;
            if (this.check(command,"list"))        if (!sender.hasPermission("auctionhouse.command.info")) continue;
            if (this.check(command,"notify","n"))      if (!sender.hasPermission("auctionhouse.command.notify")) continue;
            if (this.check(command,"remove","rem","delete","cancel"))      if (!sender.hasPermission("auctionhouse.command.delete.id")) continue;
            if (this.check(command,"search"))      if (!sender.hasPermission("auctionhouse.command.search")) continue;
            if (this.check(command,"subscribe","sub"))   if (!sender.hasPermission("auctionhouse.command.sub")) continue;
            if (this.check(command,"unsubscribe","unsub")) if (!sender.hasPermission("auctionhouse.command.sub")) continue;
            if (this.check(command,"undobid"))     if (!sender.hasPermission("auctionhouse.command.undobid")) continue;
            if (this.check(command,"reload"))  if (!sender.hasPermission("auctionhouse.admin.reload")) continue;
            sender.sendMessage(command.getUsage());
            sender.sendMessage("    " + command.getDescription());
        }
        sender.sendMessage("");
        return true;
    }
    
    private boolean check(AbstractCommand command, String... label)
    {
        int max = label.length;
        for (int i=0;i<max;i++)
            if (command.getLabel().equalsIgnoreCase(label[i])) return true;
        return false;
    }

    @Override
    public String getDescription()
    {
        return t("command_help");
    }
}
