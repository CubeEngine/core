package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.user.User;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;

public class FlyCommand
{
    @Command(desc = "Lets you fly away", max = 1, params = @Param(names = {
        "player", "p"
    }, type = User.class), usage = "[flyspeed] [player <player>]")
    public void fly(ParameterizedContext context)
    {
        final CommandSender sender = context.getSender();
        User target = null;
        boolean other = false;
        if (context.hasParam("player"))
        {
            target = context.getUser("player");
            if (target == null)
            {
                context.sendMessage("fly", "&cThe given user was not found!");
                return;
            }
        }
        if (target == null)
        {
            if (sender instanceof User)
            {
                target = (User)sender;
            }
            else
            {
                context.sendMessage("fly", "&6ProTip: &eIf your server flies away it will go offline.");
                context.sendMessage("fly", "So... Stopping the Server in &c3..");
                return;
            }
        }
        if (!target.isOnline())
        {
            illegalParameter(context, "core", "User %s is not online!", target.getName());
        }
        // PermissionChecks
        if (sender != target && !FlyPerm.COMMAND_FLY_OTHER.isAuthorized(context.getSender()))
        {
            context.sendMessage("fly", "&cYou are not allowed to change the fly-mode of other user!");
            return;
        }
        FlyStartEvent event = new FlyStartEvent(context.getCore(), target);
        if (event.isCancelled())
        {
            target.sendMessage("fly", "You are not allowed to fly now!");
            target.setAllowFlight(false); //Disable when player is flying
            return;
        }
        //I Believe I Can Fly ...     
        if (context.hasArg(0))
        {
            Float speed = context.getArg(0, Float.class);
            if (speed != null && speed >= 0 && speed <= 10)
            {
                if (speed > 0 && speed <= 10)
                {
                    target.setFlySpeed(speed / 10f);
                    target.sendMessage("fly", "You can now fly at %.2f", speed);
                }
                else
                {
                    target.sendMessage("fly", "FlySpeed has to be a Number between 0 and 10!");
                    if (speed > 9000)
                    {
                        target.sendMessage("fly", "&cIt's over 9000!");
                    }
                }
            }
            else
            {
                sender.sendMessage("fly", "FlySpeed has to be a Number between 0 and 10!");
            }
            target.setAllowFlight(true);
            target.setFlying(true);
        }
        else
        {
            target.setAllowFlight(!target.getAllowFlight());
            if (target.getAllowFlight())
            {
                target.setFlySpeed(0.1f);
                target.sendMessage("fly", "You can now fly!");
            }
            else
            {//or not
                target.sendMessage("fly", "You cannot fly anymore!");
            }
        }
    }
}
