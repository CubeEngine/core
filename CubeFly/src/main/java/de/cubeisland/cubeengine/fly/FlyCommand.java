package de.cubeisland.cubeengine.fly;

import static de.cubeisland.cubeengine.fly.CubeFly.t;
import de.cubeisland.libMinecraft.command.Command;
import de.cubeisland.libMinecraft.command.CommandArgs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
 */
public class FlyCommand {

    
    public FlyCommand() {}
    
    @Command
    public void fly(CommandSender sender, CommandArgs args)
    {
        //TODO Permission abfragen bei CubeFly
        //if (Perm.command_fly_BP.hasNotPerm(sender))
        //    if (Perm.command_fly.hasNotPerm(sender)) return;
        if (sender instanceof Player)
        {
            Player player = (Player)sender;
            /*
            //TODO Permission abfragen bei CubeWar
            if (Perm.command_fly_BP.hasNotPerm(sender))
                if (Users.getUser(sender).isFly_disable())
                {
                    sender.sendMessage(t("fly_block"));
                    return;
                }
                */
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight())
                sender.sendMessage(t("fly_on"));
            else
                sender.sendMessage(t("fly_off"));
        }
    }
}
