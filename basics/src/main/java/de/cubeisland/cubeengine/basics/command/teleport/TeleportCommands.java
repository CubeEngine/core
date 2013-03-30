package de.cubeisland.cubeengine.basics.command.teleport;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;

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

    public static boolean teleport(User user, Location loc, boolean safe, boolean force, boolean keepDirection)
    {
        if (!force && !user.getWorld().equals(loc.getWorld()) // TODO NPE!
                && !TpWorldPermissions.getPermission(loc.getWorld().getName()).isAuthorized(user))
        {
            //TODO this feels not correct
            user.sendTranslated("You are not allowed to teleport to this world!");
            return false;
        }
        if (safe)
        {
            user.safeTeleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND, keepDirection);
            return true;
        }
        if (keepDirection)
        {
            final Location userLocation = user.getLocation();
            loc.setYaw(userLocation.getYaw());
            loc.setPitch(userLocation.getPitch());
        }
        user.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
        return true;
    }

    @Command(desc = "Teleport directly to a player.", usage = "<player> [player] [-unsafe]", min = 1, max = 2, flags = {
            @Flag(longName = "force", name = "f"), // is not shown directly in usage
            @Flag(longName = "unsafe", name = "u")
    })
    public void tp(ParameterizedContext context)
    {
        User user = null;
        if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        User target = context.getUser(0);
        if (target == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        boolean force = context.hasFlag("f") && BasicsPerm.COMMAND_TP_FORCE.isAuthorized(context.getSender());
        if (context.hasArg(1)) //tp player1 player2
        {
            user = target; // The first user is not the target
            target = context.getUser(1);
            if (target == null)
            {
                context.sendTranslated("&cUser &2%s &cnot found!", context.getString(1));
                return;
            }
            if (target != context.getSender() && !BasicsPerm.COMMAND_TP_OTHER.isAuthorized(context.getSender())) // teleport other persons
            {
                context.sendTranslated("&cYou are not allowed to teleport other persons!");
                return;
            }
            if (!force) // if force no need to check
            {
                if (user != context.getSender())
                {
                    if (BasicsPerm.TELEPORT_PREVENT_TP.isAuthorized(user)) // teleport the user
                    {
                        context.sendTranslated("&cYou are not allowed to teleport &2%s&c!", user.getName());
                        return;
                    }
                } // else equals tp -> no need to check tp perm
                if (target != context.getSender())
                {
                    if (BasicsPerm.TELEPORT_PREVENT_TPTO.isAuthorized(target)) // teleport to the target
                    {
                        if (BasicsPerm.COMMAND_TP_FORCE.isAuthorized(context.getSender()))
                        {
                            context.sendTranslated("&aUse the &e-force (-f) &aflag to teleport to this player."); //Show force flag if has permission
                        }
                        context.sendTranslated("&cYou are not allowed to teleport to &2%s&c!", target.getName());
                        return;
                    }
                } // else equals tphere -> no need to check tpto perm
            }
        }
        else
        {
            if (user == null) // if not tp other persons console cannot use this
            {
                context.sendTranslated("&cTeleport to &4hell &cinitiated...");
                return;
            }
        }
        if (!force && BasicsPerm.TELEPORT_PREVENT_TPTO.isAuthorized(target))// Check if no force & target does not prevent
        {
            if (BasicsPerm.COMMAND_TP_FORCE.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&aUse the &e-force (-f) &aflag to teleport to this player."); //Show force flag if has permission
            }
            context.sendTranslated("&cYou are not allowed to teleport to &2%s&c!", target.getName());
            return;
        }
        boolean safe = !context.hasFlag("u");
        if (user.equals(target))
        {
            if (context.getSender() == user)
            {
                context.sendTranslated("&6You found yourself!");
                return;
            }
            context.sendTranslated("&aYou just teleported &2%s &ato &2%s... &eNot very useful right?", user.getName(), user.getName());
            return;
        }
        if (TeleportCommands.teleport(user, target.getLocation(), safe, force, true))
        {
            context.sendTranslated("&aYou teleported to &2%s&a!", target.getName());
        }
    }

    @Command(desc = "Teleports everyone directly to a player.",
             usage = "<player> [-unsafe]", min = 1, max = 1, flags = {
            @Flag(longName = "force", name = "f"),
            @Flag(longName = "unsafe", name = "u")
    })
    public void tpall(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        boolean force = context.hasFlag("f") && BasicsPerm.COMMAND_TPALL_FORCE.isAuthorized(context.getSender());
        boolean safe = !context.hasFlag("u");
        if (!force && BasicsPerm.TELEPORT_PREVENT_TPTO.isAuthorized(user))
        {
            context.sendTranslated("&cYou are not allowed to teleport to %s!", user.getName());
            return;
        }
        ArrayList<String> noTp = new ArrayList<String>();
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force && BasicsPerm.TELEPORT_PREVENT_TP.isAuthorized(player))
            {
                noTp.add(player.getName());
                continue;
            }
            teleport(user.getCore().getUserManager().getExactUser(player), user.getLocation(), safe, force, true);
        }
        context.getCore().getUserManager().broadcastMessage("basics", "&aTeleporting everyone to %s", user.getName());
        if (!noTp.isEmpty())
        {
            context.sendTranslated("&eThe following players were not teleported: \n&2%s", StringUtils.implode("&f,&2",noTp));
        }
    }

    @Command(desc = "Teleport a player directly to you.", usage = "<player>", min = 1, max = 1, flags = {
            @Flag(longName = "force", name = "f"),
            @Flag(longName = "unsafe", name = "u")
    })
    public void tphere(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendTranslated("&6ProTip: &cTeleport does not work IRL!");
            return;
        }
        User target = context.getUser(0);
        if (target == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        boolean force = context.hasFlag("f") && BasicsPerm.COMMAND_TPHERE_FORCE.isAuthorized(context.getSender());
        boolean safe = !context.hasFlag("u");
        if (sender.equals(target))
        {
            context.sendTranslated("&6You found yourself!");
            return;
        }
        if (!force && BasicsPerm.TELEPORT_PREVENT_TP.isAuthorized(target))
        {
            context.sendTranslated("&cYou are not allowed to teleport %s!", target.getName());
            return;
        }
        if (TeleportCommands.teleport(target, sender.getLocation(), safe, force, true))
        {
            context.sendTranslated("&aYou teleported %s to you!", target.getName());
            target.sendTranslated("&aYou were teleported to %s", sender.getName());
        }
    }

    @Command(desc = "Teleport every player directly to you.", max = 0, flags = {
            @Flag(longName = "force", name = "f"),
            @Flag(longName = "unsafe", name = "u")
    })
    public void tphereall(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendTranslated("&6ProTip: &cTeleport does not work IRL!");
            return;
        }
        boolean force = false;
        if (context.hasFlag("f") && BasicsPerm.COMMAND_TPHEREALL_FORCE.isAuthorized(context.getSender()))
        {
            force = true; // if not allowed ignore flag
        }
        ArrayList<String> noTp = new ArrayList<String>();
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force && BasicsPerm.TELEPORT_PREVENT_TP.isAuthorized(player))
            {
                noTp.add(player.getName());
                continue;
            }
            boolean safe = !context.hasFlag("u");
            teleport(sender.getCore().getUserManager().getExactUser(player), sender.getLocation(), safe, force, true);
        }
        context.sendTranslated("&aYou teleported everyone to you!");
        context.getCore().getUserManager().broadcastMessage("basics", "&aTeleporting everyone to %s", sender.getName());
        if (!noTp.isEmpty())
        {
            context.sendTranslated("&eThe following players were not teleported: \n&2%s", StringUtils.implode("&f,&2",noTp));
        }
    }

    @Command(desc = "Teleport a directly to you.", usage = "<x> [y] <z> [world <world>]", min = 2, max = 4, params = @Param(names = {
        "world", "w"
    }, type = World.class), flags = @Flag(longName = "unsafe", name = "u"))
    public void tppos(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Integer x = context.getArg(0, Integer.class);
            Integer y;
            Integer z;
            World world = sender.getWorld();
            if (context.hasArg(2))
            {
                y = context.getArg(1, Integer.class);
                z = context.getArg(2, Integer.class);
            }
            else
            {
                z = context.getArg(1, Integer.class);
                if (x == null || z == null)
                {
                    context.sendTranslated("&cCoordinates have to be numbers!");
                    return;
                }
                y = sender.getWorld().getHighestBlockAt(x, z).getY() + 1;
            }
            if (context.hasParam("world"))
            {
                world = context.getParam("world");
                if (world == null)
                {
                    context.sendTranslated("&cWorld not found!");
                    return;
                }
            }
            boolean safe = !context.hasFlag("u");
            Location loc = new Location(world, x, y, z).add(0.5, 0, 0.5);
            if (TeleportCommands.teleport(sender, loc, safe, false, true))
                context.sendTranslated("&aTeleported to &eX:&6%d &eY:&6%d &eZ:&6%d &ain %s!", x, y, z, world.getName());
            return;
        }
        context.sendTranslated("&6ProTip: &cTeleport does not work IRL!");
    }
}
