package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.AbstractCommand;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.AuctionHouseConfiguration;
import de.cubeisland.cubeengine.auctions.BaseCommand;
import de.cubeisland.cubeengine.auctions.CommandArgs;
import de.cubeisland.cubeengine.auctions.Manager;
import de.cubeisland.cubeengine.auctions.Util;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
/**
 * Lists all auctions
 * @author Faithcaio
 */
public class ListCommand extends AbstractCommand
{
    
    private static final CubeAuctions plugin = CubeAuctions.getInstance();
    private static final AuctionHouseConfiguration config = plugin.getConfiguration();
    Economy econ = CubeAuctions.getInstance().getEconomy();
    
    public ListCommand(BaseCommand base)
    {
        super(base, "list");
    }

    public boolean execute(CommandSender sender, CommandArgs args)
    {
        Util.sendInfo(sender, Manager.getInstance().getAuctions());
        return true;
    }

    public String getDescription()
    {
        return t("command_list");
    }
}