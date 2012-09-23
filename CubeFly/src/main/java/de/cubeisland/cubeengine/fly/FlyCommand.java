package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeEngine;
import static de.cubeisland.cubeengine.core.CubeEngine._;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Anselm Brehme
 */
public class FlyCommand
{
    UserManager cuManager = CubeEngine.getCore().getUserManager();

    @Command(
            desc="Lets you fly away",
            max=1)
    public void fly(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)context.getSender();
            //PermissionCheck
            if (Perm.COMMAND_FLY_BYPASS.isAuthorized(user));
            {

                if (!Perm.COMMAND_FLY.isAuthorized(user))
                {
                    user.sendMessage("core", "You dont have permission to use this Command!");
                    user.setAllowFlight(false); //Disable when player is flying
                    return;
                }
                FlyStartEvent event = new FlyStartEvent(CubeEngine.getCore(), user);
                if (event.isCancelled())
                {
                    user.sendMessage("fly", "You are not allowed to fly now!");
                    user.setAllowFlight(false); //Disable when player is flying
                    return;
                }
            }
            //I Believe I Can Fly ...     
            user.setAllowFlight(!user.getAllowFlight());
            if (user.getAllowFlight())
            {
                try
                {
                    user.setFlySpeed(context.getIndexed(0, float.class));
                }
                catch (ConversionException ex)
                {
                    user.setFlySpeed(1);
                }
                catch (IndexOutOfBoundsException ex)
                {
                    user.setFlySpeed(1);
                }
                user.sendMessage("fly", "You can now fly!");
            }
            else
            {//or not
                user.sendMessage("fly", "You cannot fly anymore!");
            }
        }
        else
        {
            sender.sendMessage(_("fly", "&6ProTip: &eIf your server flies away it will go offline."));
            sender.sendMessage(_("fly", "So... Stopping the Server in &c3.."));
        }
    }
}
