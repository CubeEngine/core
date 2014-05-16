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

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.math.BlockVector3;

import static de.cubeisland.engine.core.util.ChatFormat.DARK_GREEN;
import static de.cubeisland.engine.core.util.ChatFormat.WHITE;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

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
    private final Basics module;

    public TeleportCommands(Basics module)
    {
        this.module = module;
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

    @Command(desc = "Teleport directly to a player.",
             indexed = { @Grouped(@Indexed(label = "player", type = User.class)),
                         @Grouped(req = false, value = @Indexed(label = "player", type = User.class))},
             flags = { @Flag(longName = "force", name = "f"), // is not shown directly in usage
                       @Flag(longName = "unsafe", name = "u")})
    public void tp(ParameterizedContext context)
    {
        User user = null;
        if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        User target = context.getArg(0);
        if (target == null)
        {
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getArg(0));
            return;
        }
        if (!target.isOnline())
        {
            context.sendTranslated(NEGATIVE, "Teleportation only works with online players!");
            return;
        }
        boolean force = context.hasFlag("f") && module.perms().COMMAND_TP_FORCE.isAuthorized(context.getSender());
        if (context.hasArg(1)) //tp player1 player2
        {
            user = target; // The first user is not the target
            target = context.getArg(1);
            if (target == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getArg(1));
                return;
            }
            if (!target.isOnline())
            {
                context.sendTranslated(NEGATIVE, "Teleportation only works with online players!");
                return;
            }
            if (target != context.getSender() && !module.perms().COMMAND_TP_OTHER.isAuthorized(context.getSender())) // teleport other persons
            {
                context.sendTranslated(NEGATIVE, "You are not allowed to teleport other people!");
                return;
            }
            if (!force) // if force no need to check
            {
                if (user != context.getSender())
                {
                    if (module.perms().TELEPORT_PREVENT_TP.isAuthorized(user)) // teleport the user
                    {
                        context.sendTranslated(NEGATIVE, "You are not allowed to teleport {user}!", user);
                        return;
                    }
                } // else equals tp -> no need to check tp perm
                if (target != context.getSender())
                {
                    if (module.perms().TELEPORT_PREVENT_TPTO.isAuthorized(target)) // teleport to the target
                    {
                        if (module.perms().COMMAND_TP_FORCE.isAuthorized(context.getSender()))
                        {
                            context.sendTranslated(POSITIVE, "Use the {text:-force (-f)} flag to teleport to this player."); //Show force flag if has permission
                        }
                        context.sendTranslated(NEGATIVE, "You are not allowed to teleport to {user}!", target);
                        return;
                    }
                } // else equals tphere -> no need to check tpto perm
            }
        }
        else
        {
            if (user == null) // if not tp other persons console cannot use this
            {
                context.sendTranslated(NEGATIVE, "Teleport to {text:hell:color=DARK_RED} initiated...");
                return;
            }
        }
        if (!force && module.perms().TELEPORT_PREVENT_TPTO.isAuthorized(target))// Check if no force & target does not prevent
        {
            if (module.perms().COMMAND_TP_FORCE.isAuthorized(context.getSender()))
            {
                context.sendTranslated(POSITIVE, "Use the {text:-force (-f)} flag to teleport to this player."); //Show force flag if has permission
            }
            context.sendTranslated(NEGATIVE, "You are not allowed to teleport to {user}!", target);
            return;
        }
        boolean safe = !context.hasFlag("u");
        if (user.equals(target))
        {
            if (context.getSender() == user)
            {
                context.sendTranslated(NEUTRAL, "You found yourself!");
                return;
            }
            context.sendTranslated(NEUTRAL, "You just teleported {user} to {user}... Not very useful right?", user, user);
            return;
        }
        if (TeleportCommands.teleport(user, target.getLocation(), safe, force, true))
        {
            context.sendTranslated(POSITIVE, "You teleported to {user}!", target);
        }
    }

    @Command(desc = "Teleports everyone directly to a player.",
             indexed = @Grouped(@Indexed(label = "player", type = User.class)),
             flags = {@Flag(longName = "force", name = "f"),
                      @Flag(longName = "unsafe", name = "u")})
    public void tpall(ParameterizedContext context)
    {
        User user = context.getArg(0);
        if (user == null)
        {
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getArg(0));
            return;
        }
        if (!user.isOnline())
        {
            context.sendTranslated(NEGATIVE, "You cannot teleport to an offline player!");
            return;
        }
        boolean force = context.hasFlag("f") && module.perms().COMMAND_TPALL_FORCE.isAuthorized(context.getSender());
        boolean safe = !context.hasFlag("u");
        if (!force && module.perms().TELEPORT_PREVENT_TPTO.isAuthorized(user))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to teleport to {user}!", user);
            return;
        }
        ArrayList<String> noTp = new ArrayList<>();
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force && module.perms().TELEPORT_PREVENT_TP.isAuthorized(player))
            {
                noTp.add(player.getName());
                continue;
            }
            if (!teleport(user.getCore().getUserManager().getExactUser(player.getUniqueId()), user.getLocation(), safe, force, true))
            {
                noTp.add(player.getName());
            }
        }
        context.getCore().getUserManager().broadcastMessage(POSITIVE, "Teleporting everyone to {user}", user);
        if (!noTp.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "The following players were not teleported: \n{user#list}", StringUtils.implode(WHITE + ","+ DARK_GREEN, noTp));
        }
    }

    @Command(desc = "Teleport a player directly to you.",
             indexed = @Grouped(@Indexed(label = "player", type = User.class)),
             flags = { @Flag(longName = "force", name = "f"),
                       @Flag(longName = "unsafe", name = "u")})
    public void tphere(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendTranslated(NEGATIVE, "{text:Pro Tip}: Teleport does not work IRL!");
            return;
        }
        User target = context.getArg(0);
        if (target == null)
        {
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getArg(0));
            return;
        }
        if (!target.isOnline())
        {
            context.sendTranslated(NEGATIVE, "You cannot teleport an offline player to you!");
            return;
        }
        boolean force = context.hasFlag("f") && module.perms().COMMAND_TPHERE_FORCE.isAuthorized(context.getSender());
        boolean safe = !context.hasFlag("u");
        if (sender.equals(target))
        {
            context.sendTranslated(NEUTRAL, "You found yourself!");
            return;
        }
        if (!force && module.perms().TELEPORT_PREVENT_TP.isAuthorized(target))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to teleport {user}!", target);
            return;
        }
        if (TeleportCommands.teleport(target, sender.getLocation(), safe, force, true))
        {
            context.sendTranslated(POSITIVE, "You teleported {user} to you!", target);
            target.sendTranslated(POSITIVE, "You were teleported to {sender}", sender);
        }
    }

    @Command(desc = "Teleport every player directly to you.",
             flags = {@Flag(longName = "force", name = "f"),
                      @Flag(longName = "unsafe", name = "u")})
    public void tphereall(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendTranslated(NEGATIVE, "{text:Pro Tip}: Teleport does not work IRL!");
            return;
        }
        boolean force = false;
        if (context.hasFlag("f") && module.perms().COMMAND_TPHEREALL_FORCE.isAuthorized(context.getSender()))
        {
            force = true; // if not allowed ignore flag
        }
        ArrayList<String> noTp = new ArrayList<>();
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force && module.perms().TELEPORT_PREVENT_TP.isAuthorized(player))
            {
                noTp.add(player.getName());
                continue;
            }
            boolean safe = !context.hasFlag("u");
            if (!teleport(sender.getCore().getUserManager().getExactUser(player.getUniqueId()), sender.getLocation(), safe, force, true))
            {
                noTp.add(player.getName());
            }
        }
        context.sendTranslated(POSITIVE, "You teleported everyone to you!");
        context.getCore().getUserManager().broadcastMessage(POSITIVE, "Teleporting everyone to {sender}", sender);
        if (!noTp.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "The following players were not teleported: \n{user#list}",
                                   StringUtils.implode(WHITE + ","+ DARK_GREEN,noTp));
        }
    }

    @Command(desc = "Direct teleport to a coordinate.",
             indexed = @Grouped(value = {
                 @Indexed(label = "x", type = Integer.class),
                 @Indexed(label = "y", req = false, type = Integer.class),
                 @Indexed(label = "z", type = Integer.class)}),
             params = @Param(names = {"world", "w"}, type = World.class),
             flags = @Flag(longName = "safe", name = "s"))
    public void tppos(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Integer x = context.getArg(0);
            Integer y;
            Integer z;
            World world = sender.getWorld();
            if (context.hasParam("world"))
            {
                world = context.getParam("world");
                if (world == null)
                {
                    context.sendTranslated(NEGATIVE, "World not found!");
                    return;
                }
            }
            if (context.hasArg(2))
            {
                y = context.getArg(1, null);
                z = context.getArg(2, null);
            }
            else
            {
                z = context.getArg(1, null);
                if (x == null || z == null)
                {
                    context.sendTranslated(NEGATIVE, "Coordinates have to be numbers!");
                    return;
                }
                y = sender.getWorld().getHighestBlockAt(x, z).getY() + 1;
            }
            if (x == null || y == null || z == null)
            {
                context.sendTranslated(NEGATIVE, "Coordinates have to be numbers!");
                return;
            }
            Location loc = new Location(world, x, y, z).add(0.5, 0, 0.5);
            if (TeleportCommands.teleport(sender, loc, context.hasFlag("s") && module.perms().COMMAND_TPPOS_SAFE.isAuthorized(context.getSender()), false, true))
            {
                context.sendTranslated(POSITIVE, "Teleported to {vector:x\\=:y\\=:z\\=} in {world}!", new BlockVector3(x, y, z), world);
            }
            return;
        }
        context.sendTranslated(NEGATIVE, "{text:Pro Tip}: Teleport does not work IRL!");
    }
}
