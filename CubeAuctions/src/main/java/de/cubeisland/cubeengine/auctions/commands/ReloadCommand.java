package de.cubeisland.cubeengine.auctions.commands;

import de.cubeisland.cubeengine.auctions.AbstractCommand;
import de.cubeisland.cubeengine.auctions.CubeAuctions;
import static de.cubeisland.cubeengine.auctions.CubeAuctions.t;
import de.cubeisland.cubeengine.auctions.BaseCommand;
import de.cubeisland.cubeengine.auctions.CommandArgs;
import org.bukkit.command.CommandSender;

/**
 * reload the plugin
 * 
 * @author Faithcaio
 */
public class ReloadCommand extends AbstractCommand
{
 
    public ReloadCommand(BaseCommand base)
    {
        super(base, "reload");
    }
    
    public boolean execute(CommandSender sender, CommandArgs args)
    {
        CubeAuctions.getInstance().onDisable();
        CubeAuctions.getInstance().onEnable();
        CubeAuctions.log("reload complete");
        return true;
    }
    
        @Override
    public String getUsage()
    {
        return super.getUsage();
    }

    public String getDescription()
    {
        return t("command_reload");
    }
}
