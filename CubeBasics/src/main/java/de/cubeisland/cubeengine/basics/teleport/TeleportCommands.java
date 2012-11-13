package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.command.args.IntArg;
import de.cubeisland.cubeengine.core.command.args.WorldArg;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

/**
 * Contains commands to teleport to players/worlds/position.
 * /tp
 * /tpall
 * /tphere
 * /tphereall
 * /tppos
 */
public class TeleportCommands
{
    private Basics basics;

    public TeleportCommands(Basics basics)
    {
        this.basics = basics;
    }

    public static void teleport(User user, Location loc, boolean safe, boolean force)
    {
        if (!force && !user.getWorld().equals(loc.getWorld()))
        {
            if (!TpWorldPermissions.getPermission(loc.getWorld().getName()).isAuthorized(user))
            {
                denyAccess(user, "basics", "You are not allowed to teleport to this world!");
            }
        }
        if (safe)
        {
            user.safeTeleport(loc);
        }
        else
        {
            user.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
        }
    }

    @Command(
        desc = "Teleport directly to a player.",
        usage = "<player> [player] [-unsafe]",
        min = 1,
        max = 2,
        flags =
        {
            @Flag(longName = "force", name = "f"), // is not shown directly in usage
            @Flag(longName = "unsafe", name = "u")
        })
    public void tp(CommandContext context)
    {
        User user = context.getSenderAsUser();
        User target = context.getUser(0);
        if (target == null)
        {
            paramNotFound(context, "basics", "&cUser &2%s &cnot found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TP_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (context.hasIndexed(1)) //tp player1 player2
        {
            user = target; // The first user is not the target
            target = context.getUser(1);
            if (target == null)
            {
                paramNotFound(context, "basics", "&cUser &2%s &cnot found!", context.getString(1));
            }
            if (!force) // if force no need to check
            {
                if (!BasicsPerm.COMMAND_TP_OTHER.isAuthorized(context.getSender())) // teleport other persons
                {
                    denyAccess(context, "basics", "&cYou are not allowed to teleport other persons!");
                }
                if (BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(user)) // teleport the user
                {
                    denyAccess(context, "basics", "&cYou are not allowed to teleport %s!", user.getName());
                }
                if (BasicsPerm.COMMAND_TP_PREVENT_TPTO.isAuthorized(target)) // teleport to the target
                {
                    denyAccess(context, "basics", "&cYou are not allowed to teleport to %s!", target.getName());
                }
            }
        }
        else
        {
            if (user == null) // if not tp other persons console cannot use this
            {
                invalidUsage(context, "basics", "&cYou are now teleporting yourself into hell!");
            }
        }
        if (!force)
        {
            if (BasicsPerm.COMMAND_TP_PREVENT_TPTO.isAuthorized(target)) // Check if no force & target does not prevent
            {
                if (BasicsPerm.COMMAND_TP_FORCE.isAuthorized(context.getSender()))
                {
                    context.sendMessage("basics", "&aUse the &e-force (-f) &aflag to teleport to this player."); //Show force flag if has permission
                }
                denyAccess(context, "basics", "&cYou are not allowed to teleport to %s!", target.getName());
            }
        }
        boolean safe = !context.hasFlag("u");
        if (user.equals(target))
        {
            if (context.getSender().getName().equals(user.getName()))
            {
                context.sendMessage("basics", "&6You found yourself!");
            }
            else
            {
                context.sendMessage("basics", "&6%s is now with himself!", user.getName());
            }
            return;
        }
        TeleportCommands.teleport(user, target.getLocation(), safe, force);
        context.sendMessage("basics", "&aYou teleported to &2%s&a!", target.getName());
    }

    @Command(
        desc = "Teleport everyone directly to a player.",
        usage = "<player> [-unsafe]",
        min = 1,
        max = 1,
        flags =
        {
            @Flag(longName = "force", name = "f"),
            @Flag(longName = "unsafe", name = "u")
        })
    public void tpall(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            paramNotFound(context, "basics", "&cUser &2%s &cnot found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TP_FORCE_TPALL.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (!force)
        {
            if (BasicsPerm.COMMAND_TP_PREVENT_TPTO.isAuthorized(user))
            {
                denyAccess(context, "basics", "&cYou are not allowed to teleport to %s!", user.getName());
            }
        }
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force)
            {
                if (BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(player))
                {
                    continue;
                }
            }
            boolean safe = !context.hasFlag("u");
            TeleportCommands.teleport(CubeEngine.getUserManager().getExactUser(player), user.getLocation(), safe, force);
        }
        context.getCore().getUserManager().broadcastMessage("basucs", "&aTeleporting everyone to %s", user.getName());
    }

    @Command(
        desc = "Teleport a player directly to you.",
        usage = "<player>",
        min = 1,
        max = 1,
        flags =
        {
            @Flag(longName = "force", name = "f"),
            @Flag(longName = "unsafe", name = "u")
        })
    public void tphere(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&6ProTip: &cTeleport does not work IRL!");
        User target = context.getUser(0);
        if (target == null)
        {
            paramNotFound(context, "basics", "&cUser &2%s &cnot found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TPHERE_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (!force)
        {
            if (BasicsPerm.COMMAND_TPHERE_PREVENT.isAuthorized(target))
            {
                denyAccess(context, "bascics", "&cYou are not allowed to teleport %s!", target.getName());
                return;
            }
        }
        boolean safe = !context.hasFlag("u");
        if (sender.equals(target))
        {
            context.sendMessage("basics", "&6You found yourself!");
            return;
        }
        TeleportCommands.teleport(target, sender.getLocation(), safe, force);
        context.sendMessage("basics", "&aYou teleported %s to you!", target.getName());
        target.sendMessage("basics", "&aYou were teleported to %s", sender.getName());
    }

    @Command(
        desc = "Teleport every player directly to you.",
        max = 0,
        flags =
        {
            @Flag(longName = "force", name = "f"),
            @Flag(longName = "unsafe", name = "u")
        })
    public void tphereall(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&6ProTip: &cTeleport does not work IRL!");
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TPHEREALL_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force)
            {
                if (BasicsPerm.COMMAND_TPHEREALL_PREVENT.isAuthorized(player))
                {
                    continue;
                }
            }
            boolean safe = !context.hasFlag("u");
            TeleportCommands.teleport(CubeEngine.getUserManager().getExactUser(player), sender.getLocation(), safe, force);
        }
        context.sendMessage("basics", "&aYou teleported everyone to you!");
        context.getCore().getUserManager().broadcastMessage("basics", "&aTeleporting everyone to %s", sender.getName());
    }

    @Command(
        desc = "Teleport a directly to you.",
        usage = "<x> [y] <z> [world <world>]",
        min = 2,
        max = 4,
        params = @Param(names = {"world", "w"}, type = WorldArg.class),
        flags = @Flag(longName = "unsafe", name = "u")
    )
    public void tppos(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&6ProTip: &cTeleport does not work IRL!");
        Integer x = context.getIndexed(0, Integer.class);
        Integer y;
        Integer z;
        World world = sender.getWorld();
        if (context.hasIndexed(2))
        {
            y = context.getIndexed(1, Integer.class);
            z = context.getIndexed(2, Integer.class);
        }
        else
        {
            z = context.getIndexed(1, Integer.class);
            if (x == null || z == null)
            {
                illegalParameter(context, "basics", "&cCoordinates have to be numbers!");
            }
            y = sender.getWorld().getHighestBlockAt(x, z).getY() + 1;
        }
        if (context.hasNamed("world"))
        {
            world = context.getNamed("world", World.class);
            if (world == null)
            {
                paramNotFound(context, "basics", "&cWorld not found!");
            }
        }
        boolean safe = !context.hasFlag("u");
        Location loc = new Location(world, x, y, z).add(0.5, 0, 0.5);
        loc.setYaw(sender.getLocation().getYaw());
        loc.setPitch(sender.getLocation().getPitch());
        TeleportCommands.teleport(sender, loc, safe, false);
        context.sendMessage("basics", "&aTeleported to &eX:&6%d &eY:&6%d &eZ:&6%d &ain %s!", x, y, z, world.getName());
    }
}