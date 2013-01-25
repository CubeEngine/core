package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

public class FlyCommand
{
    @Command(desc = "Lets you fly away", max = 1, params = @Param(names = {
    "player", "p"
    }, type = User.class), usage = "[flyspeed] [player <player>]")
    public void fly(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        User user = sender;
        boolean other = false;
        if (context.hasNamed("player"))
        {
            user = context.getNamed("player", User.class);
            if (user != sender)
            {
                other = true;
            }
        }
        else if (sender == null) // Sender is console and no player given!
        {
            context.sendMessage("fly", "&6ProTip: &eIf your server flies away it will go offline.");
            context.sendMessage("fly", "So... Stopping the Server in &c3..");
            invalidUsage(context);
        }
        if (user == null)
        {
            illegalParameter(context, "core", "User not found!");
        }
        if (!user.isOnline())
        {
            illegalParameter(context, "core", "User %s is not online!", user.getName());
        }
        // PermissionChecks
        if (other)
        {
            if (!FlyPerm.COMMAND_FLY_OTHER.isAuthorized(context.getSender())) // /fly [speed] [player <name>]
            {
                denyAccess(context, "fly", "&cYou are not allowed to change the fly-mode of other user!");
            }
        }
        FlyStartEvent event = new FlyStartEvent(context.getCore(), user);
        if (event.isCancelled())
        {
            user.sendMessage("fly", "You are not allowed to fly now!");
            user.setAllowFlight(false); //Disable when player is flying
            return;
        }
        //I Believe I Can Fly ...     
        if (context.hasIndexed(0))
        {
            Float speed = context.getIndexed(0, Float.class);
            if (speed != null && speed >= 0 && speed <= 10)
            {
                if (speed > 0 && speed <= 10)
                {
                    user.setFlySpeed(speed / 10f);
                    user.sendMessage("fly", "You can now fly at %.2f", speed);
                }
                else
                {
                    user.sendMessage("fly", "FlySpeed has to be a Number between 0 and 10!");
                    if (speed > 9000)
                    {
                        user.sendMessage("fly", "&cIt's over 9000!");
                    }
                }
            }
            else
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
}
