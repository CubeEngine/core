package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
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
    UserManager um;
    
    public FlyCommand(Fly module)
    {
        this.um = module.getUserManager();
    }

    @Command(
    desc = "Lets you fly away",
    max = 1,
    usage = "[flyspeed]")
    public void fly(CommandContext context)
    {
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User) context.getSender();
            //PermissionCheck
            if (FlyPerm.COMMAND_FLY_BYPASS.isAuthorized(user));
            {

                if (!FlyPerm.COMMAND_FLY.isAuthorized(user))
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
            if (context.getIndexed().size() > 0)
            {
                try
                {
                    float speed = context.getIndexed(0, Float.class);
                    if (speed > 0 && speed <= 10)
                    {
                        user.setFlySpeed(speed / 10f);
                        user.sendMessage("fly", "You can now fly at %.2f", speed);
                    }
                    else if (speed > 9000)
                    {
                        user.sendMessage("fly", "&cIt's over 9000!");
                    }
                    else
                    {
                        user.sendMessage("fly", "FlySpeed has to be a Number between 0 and 10!");
                    }
                }
                catch (ConversionException ex)
                {
                    user.sendMessage("fly", "FlySpeed has to be a Number between 0 and 10!");
                }
                user.setAllowFlight(true);
                user.setFlying(true);
            }
            else
            {
                user.setAllowFlight(!user.getAllowFlight());
                if (user.getAllowFlight())
                {
                    user.setFlySpeed(0.1f);
                    user.sendMessage("fly", "You can now fly!");
                }
                else
                {//or not
                    user.sendMessage("fly", "You cannot fly anymore!");
                }

            }
        }
        else
        {
            sender.sendMessage(_("fly", "&6ProTip: &eIf your server flies away it will go offline."));
            sender.sendMessage(_("fly", "So... Stopping the Server in &c3.."));
        }
    }
}
