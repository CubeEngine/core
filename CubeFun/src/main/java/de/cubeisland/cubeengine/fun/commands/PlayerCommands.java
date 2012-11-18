package de.cubeisland.cubeengine.fun.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.fun.Fun;
import org.bukkit.Location;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;

public class PlayerCommands
{
    private final Fun module;
    
    public PlayerCommands(Fun module)
    {
        this.module = module;
    }
    
    @Command(
        names = {"lightning", "strike"},
        desc = "strucks a player or the location you are looking at by lightning.",
        max = 0,
        params = {
            @Param(names = {"player", "p"}, types = {User.class}),
            @Param(names = {"damage", "d"}, types = {Integer.class}),
            @Param(names = {"fireticks", "f"}, types = {Integer.class})
        },
        usage = "[player <name>] [damage <value>] [fireticks <seconds>]"
    )
    public void lightning(CommandContext context)
    {
        User user;
        Location location;
        int damage = context.getNamed("damage", Integer.class, Integer.valueOf(-1));

        if(context.hasNamed("player"))
        {
            user = context.getNamed("player", User.class);
            if (user == null)
            {
                illegalParameter(context, "core", "User not found!");
            }
            location = user.getLocation();
            if( (damage != -1 && damage < 0) || damage > 20 )
            {
                illegalParameter(context, "fun", "The damage value has to be a number from 1 to 20");
            }
            user.setFireTicks(20 * context.getNamed("fireticks", Integer.class, Integer.valueOf(0)));
        }
        else
        {
            user = context.getSenderAsUser("fun", "&cThis command can only be used by a player!");
            location = user.getTargetBlock(null, this.module.getConfig().lightningDistance).getLocation();
        }

        user.getWorld().strikeLightningEffect(location);
        if(damage != -1)
        {
            user.damage(damage);
        }
    }
    
    @Command(
        desc = "slaps a player",
        min = 1,
        max = 2,
        usage = "<player> [damage]"
    )
    public void slap(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
              illegalParameter(context, "core", "User not found!");
        }
        
        int damage = context.getIndexed(1, Integer.class, 3);

        if (damage < 1 || damage > 20)
        {
            illegalParameter(context, "fun", "Only damage values from 1 to 20 are allowed!");
            return;
        }
        
        user.damage(damage);
    }
    
    @Command(
        desc = "burns a player",
        min = 1,
        max = 2,
        flags = {@Flag(longName = "unset", name = "u")},
        usage = "<player> [seconds] [-unset]"
    )
    public void burn(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "core", "User not found!");
        }
        
        int seconds = context.getIndexed(1, Integer.class, 5);

        if (context.hasFlag("u"))
        {
            seconds = 0;
        }
        else if (seconds < 1 || seconds > 26)
        {
            illegalParameter(context, "fun", "Only 1 to 26 seconds are permitted!");
        }

        user.setFireTicks(seconds * 20);
    }
}
