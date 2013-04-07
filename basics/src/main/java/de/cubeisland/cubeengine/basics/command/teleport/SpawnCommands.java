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
package de.cubeisland.cubeengine.basics.command.teleport;

import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;

/**
 * Contains spawn-commands.
 * /setspawn
 * /spawn
 * /tpworld
 */
public class SpawnCommands
{
    private Basics basics;

    public SpawnCommands(Basics basics)
    {
        this.basics = basics;
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
                context.sendTranslated("&cWorld: %s not found", context.getString(0));
                return;
            }
        }
        else
        {
            if (sender == null)
            {
                context.sendTranslated("&cIf not used ingame you have to specify a world and coordinates!");
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
                context.sendTranslated("&cCoordinates are invalid!");
                return;
            }
        }
        else
        {
            if (sender == null)
            {
                context.sendTranslated("&cIf not used ingame you have to specify a world and coordinates!");
                return;
            }
            final Location loc = sender.getLocation();
            x = loc.getBlockX();
            y = loc.getBlockY();
            z = loc.getBlockZ();
        }
        world.setSpawnLocation(x, y, z);
        context.sendTranslated("&aSpawn was in world %s set to &eX:&6%d &eY:&6%d &eZ:&6%d", world.getName(), x, y, z);
    }

    @Command(desc = "Teleport directly to the worlds spawn.", usage = "[player] [world <world>]", max = 2, params = @Param(names = {
        "world", "w"
    }, type = World.class), flags = {
            @Flag(longName = "force", name = "f"),
            @Flag(longName = "all", name = "a")
    })
    public void spawn(ParameterizedContext context)
    {
        User user = null;
        if (context.getSender() instanceof User)
        {
            user = (User)context.getSender();
        }
        World world = basics.getConfiguration().mainWorld;
        if (world == null)
        {
            world = user.getWorld();
        }
        boolean force = false;
        if (context.hasFlag("f") && BasicsPerm.COMMAND_SPAWN_FORCE.isAuthorized(context.getSender()))
        {
            force = true; // if not allowed ignore flag
        }
        if (context.hasParam("world"))
        {
            world = context.getParam("world", null);
            if (world == null)
            {
                context.sendTranslated("&cWorld not found!");
                return;
            }
        }
        if (context.hasFlag("a"))
        {
            if (!BasicsPerm.COMMAND_SPAWN_ALL.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to spawn everyone!");
                return;
            }
            Location loc = world.getSpawnLocation().add(0.5, 0, 0.5);
            for (User player : context.getCore().getUserManager().getOnlineUsers())
            {
                if (!force)
                {
                    if (BasicsPerm.COMMAND_SPAWN_PREVENT.isAuthorized(player))
                    {
                        continue;
                    }
                }
                if (!TeleportCommands.teleport(player, loc, true, force, true))
                    return;
            }
            this.basics.getCore().getUserManager().broadcastMessage("&aTeleported everyone to the spawn of %s!", world.getName());
            return;
        }
        if (user == null && !context.hasArg(0))
        {
            context.sendTranslated("&6ProTip: &cTeleport does not work IRL!");
            return;
        }
        if (context.hasArg(0))
        {
            user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
                return;
            }
            if (!user.isOnline())
            {
                context.sendTranslated("&cYou cannot teleport an offline player to spawn!");
                return;
            }
            if (!force && BasicsPerm.COMMAND_SPAWN_PREVENT.isAuthorized(user))
            {
                context.sendTranslated("&cYou are not allowed to spawn %s!", user.getName());
                return;
            }
        }
        final Location spawnLocation = world.getSpawnLocation().add(0.5, 0, 0.5);
        final Location userLocation = user.getLocation();
        spawnLocation.setPitch(userLocation.getPitch());
        spawnLocation.setYaw(userLocation.getYaw());
        SpawnCommandEvent event = new SpawnCommandEvent(this.basics, user, spawnLocation);
        this.basics.getCore().getEventManager().fireEvent(event); // catch this event to change spawn location
        TeleportCommands.teleport(user, event.getLoc(), true, force, true);
    }

    @Command(desc = "Teleports you to the spawn of given world", usage = "<world>", min = 1, max = 1)
    public void tpworld(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            World world = context.getArg(0, World.class, null);
            if (world == null)
            {
                context.sendTranslated("&cWorld not found!");
                return;
            }
            final Location spawnLocation = world.getSpawnLocation().add(0.5, 0, 0.5);
            final Location userLocation = sender.getLocation();
            spawnLocation.setPitch(userLocation.getPitch());
            spawnLocation.setYaw(userLocation.getYaw());
            if (TeleportCommands.teleport(sender, spawnLocation, true, false, true))
                context.sendTranslated("&aTeleported to the spawn of world &6%s&a!", world.getName());
            return;
        }
        context.sendTranslated("&eProTip: Teleport does not work IRL!");
    }
}
