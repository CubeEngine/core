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

import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.world.WorldSetSpawnEvent;

/**
 * Contains spawn-commands.
 * /setspawn
 * /spawn
 * /tpworld
 */
public class SpawnCommands
{
    private final Basics module;

    public SpawnCommands(Basics basics)
    {
        this.module = basics;
    }

    @Command(desc = "Changes the global respawnpoint", usage = "[world] [<x> <y> <z>]", max = 4)
    public void setSpawn(CommandContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        Integer x;
        Integer y;
        Integer z;
        World world;
        if (context.hasArg(0))
        {
            world = context.getSender().getServer().getWorld(context.getString(0));
            if (world == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "World {input} not found", context.getString(0));
                return;
            }
        }
        else
        {
            if (sender == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "If not used ingame you have to specify a world and coordinates!");
                return;
            }
            world = sender.getWorld();
        }

        if (context.hasArg(3))
        {
            x = context.getArg(1, Integer.class, null);
            y = context.getArg(2, Integer.class, null);
            z = context.getArg(3, Integer.class, null);
            if (x == null || y == null || z == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Coordinates are invalid!");
                return;
            }
            this.module.getCore().getEventManager().fireEvent(new WorldSetSpawnEvent(this.module.getCore(), world, new Location(world, x,y,z)));
        }
        else
        {
            if (sender == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "If not used ingame you have to specify a world and coordinates!");
                return;
            }
            final Location loc = sender.getLocation();
            this.module.getCore().getEventManager().fireEvent(new WorldSetSpawnEvent(this.module.getCore(), world, loc));
            x = loc.getBlockX();
            y = loc.getBlockY();
            z = loc.getBlockZ();
        }
        world.setSpawnLocation(x, y, z);
        context.sendTranslated(MessageType.POSITIVE, "The spawn in {world} is now set to {vector:x\\=:y\\=:z\\=}", world, new BlockVector3(x, y, z));
    }

    @Command(desc = "Teleport directly to the worlds spawn.", usage = "[player] [world <world>]", max = 2,
             params = @Param(names = {"world", "w"}, type = World.class),
             flags = {@Flag(longName = "force", name = "f"),
                      @Flag(longName = "all", name = "a")})
    public void spawn(ParameterizedContext context)
    {
        User user = null;
        if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        World world = module.getConfiguration().mainWorld;
        if (world == null && user != null)
        {
            world = user.getWorld();
        }
        boolean force = false;
        if (context.hasFlag("f") && module.perms().COMMAND_SPAWN_FORCE.isAuthorized(context.getSender()))
        {
            force = true; // if not allowed ignore flag
        }
        if (context.hasParam("world"))
        {
            world = context.getParam("world", null);
            if (world == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "World {input#world} not found!", context.getString("world"));
                return;
            }
        }
        if (world == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "You have to specify a world!");
            return;
        }
        if (context.hasFlag("a"))
        {
            if (!module.perms().COMMAND_SPAWN_ALL.isAuthorized(context.getSender()))
            {
                context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to spawn everyone!");
                return;
            }
            Location loc = world.getSpawnLocation().add(0.5, 0, 0.5);
            for (User player : context.getCore().getUserManager().getOnlineUsers())
            {
                if (!force)
                {
                    if (module.perms().COMMAND_SPAWN_PREVENT.isAuthorized(player))
                    {
                        continue;
                    }
                }
                if (!TeleportCommands.teleport(player, loc, true, force, true))
                {
                    return;
                }
            }
            this.module.getCore().getUserManager().broadcastMessage(MessageType.POSITIVE, "Teleported everyone to the spawn of {world}!", world);
            return;
        }
        if (user == null && !context.hasArg(0))
        {
            context.sendTranslated(MessageType.NEGATIVE, "{text:Pro Tip}: Teleport does not work IRL!");
            return;
        }
        if (context.hasArg(0))
        {
            user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Player {user} not found!", context.getString(0));
                return;
            }
            if (!user.isOnline())
            {
                context.sendTranslated(MessageType.NEGATIVE, "You cannot teleport an offline player to spawn!");
                return;
            }
            if (!force && module.perms().COMMAND_SPAWN_PREVENT.isAuthorized(user))
            {
                context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to spawn {user}!", user);
                return;
            }
        }
        final Location spawnLocation = world.getSpawnLocation().add(0.5, 0, 0.5);
        final Location userLocation = user.getLocation(); // TODO possible NPE
        spawnLocation.setPitch(userLocation.getPitch());
        spawnLocation.setYaw(userLocation.getYaw());
        if (!TeleportCommands.teleport(user, spawnLocation, true, force, true))
        {
            context.sendTranslated(MessageType.NEGATIVE, "Teleport failed!");
        }
    }

    @Command(desc = "Teleports you to the spawn of given world",
             usage = "<world>", min = 1, max = 1)
    public void tpworld(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            World world = context.getArg(0, World.class, null);
            if (world == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "World not found!");
                return;
            }
            final Location spawnLocation = world.getSpawnLocation().add(0.5, 0, 0.5);
            final Location userLocation = sender.getLocation();
            spawnLocation.setPitch(userLocation.getPitch());
            spawnLocation.setYaw(userLocation.getYaw());
            if (!module.perms().tpWorld().getPermission(world.getName()).isAuthorized(sender))
            {
                context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to teleport to this world!");
                return;
            }
            if (TeleportCommands.teleport(sender, spawnLocation, true, false, true))
            {
                context.sendTranslated(MessageType.POSITIVE, "Teleported to the spawn of world {world}!", world);
            }
            return;
        }
        context.sendTranslated(MessageType.NEUTRAL, "Pro Tip: Teleport does not work IRL!");
    }
}
