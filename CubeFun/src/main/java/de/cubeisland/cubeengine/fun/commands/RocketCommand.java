package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import de.cubeisland.cubeengine.fun.listeners.RocketListener;
import org.bukkit.util.Vector;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;

public class RocketCommand 
{
    private final Fun module;
    
    public RocketCommand(Fun module) 
    {
        this.module = module;
    }
    
    @Command(
        desc = "rockets a player",
        max = 1,
        usage = "[height] [player <name>]",
        params = {@Param(names = {"player", "p"}, type = User.class)}
    )
    public void rocket(CommandContext context)
    {
        RocketListener rocketListener = this.module.getRocketListener();

        int height = context.getIndexed(0, Integer.class, 10);
        User user = (context.hasNamed("player"))
            ? context.getNamed("player", User.class, null)
            : context.getSenderAsUser("fun", "&cThis command can only be used by a player!");

        if (user == null)
        {
            illegalParameter(context, "core", "User not found!");
        }

        if (height > 100)
        {
            illegalParameter(context, "fun", "Do you never wanna see %s again?", user.getName());
        }
        else if (height < 0)
        {
            illegalParameter(context, "fun", "The height has to be greater than 0");
        }

        rocketListener.addInstance(user, height);
    }
    
}
