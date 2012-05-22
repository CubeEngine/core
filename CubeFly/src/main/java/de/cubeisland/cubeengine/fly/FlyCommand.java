package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import static de.cubeisland.cubeengine.fly.CubeFly.t;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class FlyCommand
{
    UserManager cuManager = CubeCore.getInstance().getUserManager();

    @Command
    public void fly(CommandSender sender, CommandArgs args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            //PermissionCheck
            if (Perm.COMMAND_FLY_BYPASS.isAuthorized(sender));
            {
                if (!Perm.COMMAND_FLY.isAuthorized(sender))
                {
                    sender.sendMessage("Permission fehlt");
                    //TODO Translation: You dont have permission to use this Command!
                    //Du bist nicht berechtigt diesen Befehl zu nutzen!
                    player.setAllowFlight(false); //Disable when player is flying
                    return;
                }
                User user = cuManager.getUser(sender);
                if (user.hasFlag(User.BLOCK_FLY))
                {
                    sender.sendMessage(t("fly_block"));
                    player.setAllowFlight(false); //Disable when player is flying
                    return;
                }
            }
            //I Believe I Can Fly ...     
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight())
            {
                sender.sendMessage(t("fly_on"));
            }
            else
            {//or not
                sender.sendMessage(t("fly_off"));
            }
            return;
        }
        sender.sendMessage(t("fly_server"));
        sender.sendMessage(t("fly_joke"));
    }
}
