package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.CommandArgs;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.CubeAuctionsConfiguration;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.Util;
import de.cubeisland.libMinecraft.command.Command;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
/**
 * Lists all auctions
 * @author Faithcaio
 */
public class ListCommand
{
    
    private static final CubeAuctions plugin = CubeAuctions.getInstance();
    private static final CubeAuctionsConfiguration config = plugin.getConfiguration();
    Economy econ = CubeAuctions.getInstance().getEconomy();
    
    public ListCommand()
    {
    }

    @Command(usage = "")
    public boolean list(CommandSender sender, CommandArgs args)
    {
        Util.sendInfo(sender, Manager.getInstance().getAuctions());
        return true;
    }

    public String getDescription()
    {
        return t("command_list");
    }
}