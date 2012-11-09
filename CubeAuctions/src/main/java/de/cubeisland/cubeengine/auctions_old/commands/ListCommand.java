package de.cubeisland.cubeengine.auctions_old.commands;

import de.cubeisland.cubeengine.auctions_old.CubeAuctions;
import static de.cubeisland.cubeengine.auctions_old.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions_old.CubeAuctionsConfiguration;
import de.cubeisland.cubeengine.auctions_old.Manager;
import de.cubeisland.cubeengine.auctions_old.Util;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;

/**
 * Lists all auctions
 *
 * @author Anselm Brehme
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