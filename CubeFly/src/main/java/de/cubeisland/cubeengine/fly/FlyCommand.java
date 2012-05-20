package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.user.CubeUser;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
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
    CubeUserManager cuManager = CubeCore.getInstance().getUserManager();

    @Command
    public void fly(CommandSender sender, CommandArgs args)
    {
        //PermissionCheck
        if (Perm.COMMAND_FLY_BYPASS.isAuthorized(sender));
        {
            if (!Perm.COMMAND_FLY.isAuthorized(sender))
            {
                //TODO You dont have permission to use this Command
                return;
            }
            CubeUser user = cuManager.getUser(sender);
            if (user.hasFlag(CubeUser.BLOCK_FLY))
            {
                sender.sendMessage(t("fly_block"));
                return;
            }
        }
        //I Believe I Can Fly ...     
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight())
            {
                sender.sendMessage(t("fly_on"));
            }
            else
            {
                sender.sendMessage(t("fly_off"));
            }
            return;
        }
        sender.sendMessage(t("fly_server"));
        sender.sendMessage(t("fly_joke"));
    }
}
