package de.cubeisland.CubeWar.Commands;

import static de.cubeisland.CubeWar.CubeWar.t;
import de.cubeisland.CubeWar.User.User;
import de.cubeisland.CubeWar.User.Users;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import de.cubeisland.libMinecraft.command.RequiresPermission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class UserCommands 
{

    public UserCommands() 
    {
    
    }
    
    @Command(usage = "<Player>", aliases = {"kd"})
    @RequiresPermission
    public boolean show(CommandSender sender, CommandArgs args)
    {
        if (args.size() > 0)    
        {
            User user = Users.getUser(args.getString(0));
            if (user == null)
            {
                sender.sendMessage(t("e")+t("g_noplayer"));
                return true;
            }
            user.showInfo(sender);
            return true;
        }
        if (args.isEmpty())
        {
            User user = Users.getUser(sender);
            user.showInfo(sender);
            return true;
        }
        return false;
    }
    
    @Command(usage = "")
    @RequiresPermission
    public void fly(CommandSender sender, CommandArgs args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player)sender;
            if (Users.getUser(sender).isFly_disable())
            {
                sender.sendMessage(t("fly_block"));
                return;
            }
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight())
                sender.sendMessage(t("fly_on"));
            else
                sender.sendMessage(t("fly_off"));
        }
    }
    
    @Command(usage = "add <Player> <bounty>" )//aliases = {""}
    @RequiresPermission
    public void bounty(CommandSender sender, CommandArgs args)
    {
        //TODO bounty adding etc
    }
}
