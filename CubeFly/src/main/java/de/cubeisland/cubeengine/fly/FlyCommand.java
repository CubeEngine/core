package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.user.CubeUser;
import de.cubeisland.cubeengine.core.user.CubeUserManager;
import static de.cubeisland.cubeengine.fly.CubeFly.t;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import de.cubeisland.libMinecraft.command.RequiresPermission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class FlyCommand {
    
    CubeUserManager cuManager = CubeUserManager.getInstance();
    
    public FlyCommand() {}
    
    @Command
    @RequiresPermission
    public void fly(CommandSender sender, CommandArgs args)
    {
        //TODO Permission abfragen bei CubeFly
        CubeUser user = cuManager.getCubeUser(sender);
        if (user.hasFlag(CubeUser.BLOCK_FLY))
        {
            sender.sendMessage(t("fly_block"));
            return;
        }
        if (sender instanceof Player)
        {
            Player player = (Player)sender;
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight())
                sender.sendMessage(t("fly_on"));
            else
                sender.sendMessage(t("fly_off"));
            return;
        }
        sender.sendMessage(t("fly_server"));
        sender.sendMessage(t("fly_joke"));
    }
}
