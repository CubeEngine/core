package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.CubeEngine;
import static de.cubeisland.cubeengine.CubeEngine._;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Faithcaio
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
                    user.sendTMessage("&cYou dont have permission to use this Command!");
                    //TODO Translations
                    //&cDu bist nicht berechtigt diesen Befehl zu nutzen!
                    player.setAllowFlight(false); //Disable when player is flying
                    return;
                }
                FlyStartEvent event = new FlyStartEvent(CubeEngine.getCore(), user);
                if (event.isCancelled())
                {
                    user.sendTMessage("&cYou are not allowed to fly now!");
                    //&cDu darfst jetzt nicht fliegen!
                    player.setAllowFlight(false); //Disable when player is flying
                    return;
                }
            }
            //I Believe I Can Fly ...     
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight())
            {
                user.sendTMessage("&6You can now fly!");
                //&6Du kannst jetzt fliegen!
            }
            else
            {//or not
                user.sendTMessage("&6You cannot fly anymore!");
                //&6Du kannst jetzt nicht mehr fliegen!
            }
            return;
        }
        sender.sendMessage(_("fly", "&6ProTip: &eIf your server fly away it goes offline."));
        sender.sendMessage(_("fly", "So... Stopping the Server in 3.."));
        //&6ProTipp: &eWenn der Server wegfliegt geht er aus.
        //Also... Server fährt runter in 3..
    }
}
