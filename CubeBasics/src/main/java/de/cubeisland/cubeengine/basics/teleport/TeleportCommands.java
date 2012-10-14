package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import sun.misc.BASE64Decoder;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

public class TeleportCommands
{
    private void teleport(User user, Location loc)
    {
        user.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Command(
    desc = "Teleport directly to a player.",
    usage = "<player> [player]",
    min = 1,
    max = 2,
    flags =
    {
        @Flag(longName = "force", name = "f")
    })
    public void tp(CommandContext context)
    {
        User user1 = context.getSenderAsUser();
        User user2 = context.getUser(0);
        if (user2 == null)
        {
            illegalParameter(context, "basics", "User %s not found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TP_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (!force)
        {
            if (!BasicsPerm.COMMAND_TP_PREVENT_TPTO.isAuthorized(user2))
            {
                denyAccess(context, "basics", "You are not allowed to teleport to %s!", user2.getName());
            }
        }
        if (context.hasIndexed(1))
        {
            user1 = context.getUser(1);
            if (user1 == null)
            {
                illegalParameter(context, "basics", "User %s not found!", context.getString(1));
            }
            if (!force) // if force no need to check
            {
                if (!BasicsPerm.COMMAND_TP_OTHER.isAuthorized(context.getSender()))
                {
                    denyAccess(context, "basics", "You are not allowed to teleport other persons!");
                }
                if (!BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(context.getSender()))
                {
                    denyAccess(context, "basics", "You are not allowed to teleport %s!", user1.getName());
                }
            }
        }
        else
        {
            if (user1 == null)
            {
                invalidUsage(context, "basics", "&cYou are now teleporting yourself into hell!");
            }
        }
        this.teleport(user1, user2.getLocation());
        //TODO msg teleported!
    }

    @Command(
    desc = "Teleport everyone directly to a player.",
    usage = "<player>",
    min = 1,
    max = 1,
    flags =
    {
        @Flag(longName = "force", name = "f")
    })
    public void tpall(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "User %s not found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TPALL_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (!force)
        {
            if (!BasicsPerm.COMMAND_TP_PREVENT_TPTO.isAuthorized(user))
            {
                denyAccess(context, "basics", "You are not allowed to teleport to %s!", user.getName());
            }
        }
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force)
            {
                if (!BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(player))
                {
                    continue;
                }
            }
            this.teleport(CubeEngine.getUserManager().getUser(player), user.getLocation());
        }
        // TODO msg tped everyone!
    }

    public void tphere(CommandContext context)
    {
    }

    public void tphereall(CommandContext context)
    {
    }

    public void tppos(CommandContext context)
    {
    }

    public void spawn(CommandContext context)
    {
    }

    public void tpa(CommandContext context)
    {
    }

    public void tpaccept(CommandContext context)
    {
    }

    public void tpdeny(CommandContext context)
    {
    }

    public void jumpTo(CommandContext context)
    {
    }

    public void back(CommandContext context)
    {
        //TODO register onTeleportEvent for this
    }

    public void tpworld(CommandContext context)
    {
    }
}
