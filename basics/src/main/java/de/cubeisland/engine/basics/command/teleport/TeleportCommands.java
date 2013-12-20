/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.basics.command.teleport;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsPerm;

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
        if (safe)
        {
            return user.safeTeleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND, keepDirection);
        }
        if (keepDirection)
        {
            final Location userLocation = user.getLocation();
            loc.setYaw(userLocation.getYaw());
            loc.setPitch(userLocation.getPitch());
        }
        return user.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
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
        if (!target.isOnline())
        {
            context.sendTranslated("&cTeleportation only works with online players!");
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
            if (!target.isOnline())
            {
                context.sendTranslated("&cTeleportation only works with online players!");
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
        if (!user.isOnline())
        {
            context.sendTranslated("&cYou cannot teleport to an offline player!");
            return;
        }
        boolean force = context.hasFlag("f") && BasicsPerm.COMMAND_TPALL_FORCE.isAuthorized(context.getSender());
        boolean safe = !context.hasFlag("u");
        if (!force && BasicsPerm.TELEPORT_PREVENT_TPTO.isAuthorized(user))
        {
            context.sendTranslated("&cYou are not allowed to teleport to %s!", user.getName());
            return;
        }
        ArrayList<String> noTp = new ArrayList<>();
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force && BasicsPerm.TELEPORT_PREVENT_TP.isAuthorized(player))
            {
                noTp.add(player.getName());
                continue;
            }
            if (!teleport(user.getCore().getUserManager().getExactUser(player.getName()), user.getLocation(), safe, force, true))
            {
                noTp.add(player.getName());
            }
        }
        context.getCore().getUserManager().broadcastMessage("&aTeleporting everyone to %s", user.getName());
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
        if (!target.isOnline())
        {
            context.sendTranslated("&cYou cannot teleport an offline player to you!");
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
        ArrayList<String> noTp = new ArrayList<>();
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force && BasicsPerm.TELEPORT_PREVENT_TP.isAuthorized(player))
            {
                noTp.add(player.getName());
                continue;
            }
            boolean safe = !context.hasFlag("u");
            if (!teleport(sender.getCore().getUserManager().getExactUser(player.getName()), sender.getLocation(), safe, force, true))
            {
                noTp.add(player.getName());
            }
        }
        context.sendTranslated("&aYou teleported everyone to you!");
        context.getCore().getUserManager().broadcastMessage("&aTeleporting everyone to %s", sender.getName());
        if (!noTp.isEmpty())
        {
            context.sendTranslated("&eThe following players were not teleported: \n&2%s", StringUtils.implode("&f,&2",noTp));
        }
    }

    @Command(desc = "Direct teleport to a coordinate.", usage = "<x> [y] <z> [w <world>]", min = 2, max = 4, params = @Param(names = {
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
            if (context.hasParam("world"))
            {
                world = context.getParam("world");
                if (world == null)
                {
                    context.sendTranslated("&cWorld not found!");
                    return;
                }
            }
            if (context.hasArg(2))
            {
                y = context.getArg(1, Integer.class, null);
                z = context.getArg(2, Integer.class, null);
            }
            else
            {
                z = context.getArg(1, Integer.class, null);
                if (x == null || z == null)
                {
                    context.sendTranslated("&cCoordinates have to be numbers!");
                    return;
                }
                y = sender.getWorld().getHighestBlockAt(x, z).getY() + 1;
            }
            if (x == null || y == null || z == null)
            {
                context.sendTranslated("&cCoordinates have to be numbers!");
                return;
            }
            boolean safe = !context.hasFlag("u");
            Location loc = new Location(world, x, y, z).add(0.5, 0, 0.5);
            if (TeleportCommands.teleport(sender, loc, safe, false, true))
            {
                context.sendTranslated("&aTeleported to &eX:&6%d &eY:&6%d &eZ:&6%d &ain %s!", x, y, z, world.getName());
            }
            return;
        }
        context.sendTranslated("&6ProTip: &cTeleport does not work IRL!");
    }
}
