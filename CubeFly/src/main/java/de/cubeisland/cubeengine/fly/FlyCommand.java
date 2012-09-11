package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeEngine;
import static de.cubeisland.cubeengine.core.CubeEngine._;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Anselm Brehme
 */
public class FlyCommand
{
    UserManager cuManager = CubeEngine.getCore().getUserManager();

    @Command(desc="Lets you fly away")
    public void fly(CommandSender sender, Object[] args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            User user = cuManager.getUser(sender);
            //PermissionCheck
            if (Perm.COMMAND_FLY_BYPASS.isAuthorized(sender));
            {

                if (!Perm.COMMAND_FLY.isAuthorized(sender))
                {
                    user.sendMessage("core", "You dont have permission to use this Command!");
                    player.setAllowFlight(false); //Disable when player is flying
                    return;
                }
                FlyStartEvent event = new FlyStartEvent(CubeEngine.getCore(), user);
                if (event.isCancelled())
                {
                    user.sendMessage("fly", "You are not allowed to fly now!");
                    player.setAllowFlight(false); //Disable when player is flying
                    return;
                }
            }
            //I Believe I Can Fly ...     
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight())
            {
                user.sendMessage("fly", "You can now fly!");
            }
            else
            {//or not
                user.sendMessage("fly", "You cannot fly anymore!");
            }
            return;
        }
        sender.sendMessage(_("fly", "&6ProTip: &eIf your server fly away it goes offline."));
        sender.sendMessage(_("fly", "So... Stopping the Server in 3.."));
        //&6ProTipp: &eWenn der Server wegfliegt geht er aus.
        //Also... Server fährt runter in 3..
    }
}
