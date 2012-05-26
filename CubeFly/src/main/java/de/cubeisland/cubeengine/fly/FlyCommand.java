package de.cubeisland.cubeengine.fly;

import static de.cubeisland.cubeengine.CubeEngine._;
import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
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
            User user = cuManager.getUser(sender);
            //PermissionCheck
            if (Perm.COMMAND_FLY_BYPASS.isAuthorized(sender));
            {
                
                if (!Perm.COMMAND_FLY.isAuthorized(sender))
                {
                    sender.sendMessage(_(user.getLanguage(), "core" , "&cYou dont have permission to use this Command!"));
                    //TODO Translations
                    //&cDu bist nicht berechtigt diesen Befehl zu nutzen!
                    player.setAllowFlight(false); //Disable when player is flying
                    return;
                }
                FlyStartEvent event = new FlyStartEvent(CubeCore.getInstance(), user);
                if (event.isCancelled())
                {
                    sender.sendMessage(_(user.getLanguage(), "fly" , "&cYou are not allowed to fly now!"));
                    //&cDu darfst jetzt nicht fliegen!
                    player.setAllowFlight(false); //Disable when player is flying
                    return;
                }
            }
            //I Believe I Can Fly ...     
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight())
            {
                sender.sendMessage(_(user.getLanguage(), "fly" , "&6You can now fly!"));
                //&6Du kannst jetzt fliegen!
            }
            else
            {//or not
                sender.sendMessage(_(user.getLanguage(), "fly" , "&6You cannot fly anymore!"));
                //&6Du kannst jetzt nicht mehr fliegen!
            }
            return;
        }
        sender.sendMessage(_("fly" , "&6ProTip: &eIf your server fly away it goes offline."));
        sender.sendMessage(_("fly" , "So... Stopping the Server in 3.."));
        //&6ProTipp: &eWenn der Server wegfliegt geht er aus.
        //Also... Server fährt runter in 3..
    }
}
