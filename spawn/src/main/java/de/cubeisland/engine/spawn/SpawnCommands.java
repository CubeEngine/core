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
package de.cubeisland.engine.spawn;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.world.WorldSetSpawnEvent;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.commands.ManagementCommands;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RolesAttachment;
import de.cubeisland.engine.roles.role.RolesManager;

public class SpawnCommands
{

    private final Roles roles;
    private final Spawn module;

    private static RolesManager manager;

    public SpawnCommands(Roles roles, Spawn module)
    {
        this.roles = roles;
        this.module = module;
        manager = roles.getRolesManager();
    }

    @Command(desc = "Changes the respawnpoint", max = 5,
             usage = "[<role>|global] [<x> <y> <z>] [world]")
    public void setSpawn(CommandContext context)
    {
        if (!(context.getSender() instanceof User) && context.hasArg(4))
        {
            context.sendTranslated(MessageType.NEGATIVE, "If not used ingame you have to specify a world and coordinates!");
            context.sendTranslated(MessageType.NEUTRAL, "Use {text:global} instead of the role-name to set the default spawn.");
            return;
        }
        World world;
        if (context.hasArg(4))
        {
            world = context.getArg(0, World.class, null);
            if (world == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "World {input} not found", context.getString(0));
                return;
            }
        }
        else
        {
            world = ((User)context.getSender()).getWorld();
        }
        Double x;
        Double y;
        Double z;
        float yaw = 0;
        float pitch = 0;
        if (context.hasArg(3))
        {
            x = context.getArg(1, Double.class, null);
            y = context.getArg(2, Double.class, null);
            z = context.getArg(3, Double.class, null);
            if (x == null || y == null || z == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Coordinates are invalid!");
                return;
            }
        }
        else
        {
            final Location loc = ((User)context.getSender()).getLocation();
            x = loc.getX();
            y = loc.getY();
            z = loc.getZ();
            yaw = loc.getYaw();
            pitch = loc.getPitch();
        }
        if (context.hasArg(0) && !"global".equalsIgnoreCase(context.getString(0)))
        {
            Role role = manager.getProvider(world).getRole(context.getString(0));
            if (role == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Could not find the role {input} in {world}!", context.getString(0), world);
                return;
            }
            setRoleSpawn(world, x, y, z, yaw, pitch, role);
            return;
        }
        // else global world spawn
        this.module.getCore().getEventManager().fireEvent(new WorldSetSpawnEvent(this.module.getCore(), world, new Location(world, x,y,z, yaw, pitch)));
        world.setSpawnLocation(x.intValue(), y.intValue(), z.intValue());
        context.sendTranslated(MessageType.POSITIVE, "The spawn in {world} is now set to {vector}", world, new BlockVector3(x.intValue(), y.intValue(), z.intValue()));
    }

    private void setRoleSpawn(World world, Double x, Double y, Double z, float yaw, float pitch, Role role)
    {
        String[] locStrings = new String[6];
        locStrings[0] = String.valueOf(x.intValue());
        locStrings[1] = String.valueOf(y.intValue());
        locStrings[2] = String.valueOf(z.intValue());
        locStrings[3] = String.valueOf(yaw);
        locStrings[4] = String.valueOf(pitch);
        locStrings[5] = world.getName();
        role.setMetadata("rolespawn", StringUtils.implode(":", locStrings));
        role.save();
        manager.getProvider(world).recalculateRoles();
    }

    @Command(desc = "Teleport directly to the worlds spawn.",
             usage = "[player]|* [world <world>] [role <role>]", max = 1,
             params = {@Param(names = {"world", "w", "in"}, type = World.class),
                       @Param(names = {"role", "r"}, type = String.class, completer = RoleCompleter.class)} ,
             flags = @Flag(longName = "force", name = "f"))
    public void spawn(ParameterizedContext context)
    {
        if (!(context.getSender() instanceof User || context.hasArg(0)))
        {
            context.sendTranslated(MessageType.NEGATIVE, "{text:ProTip}: Teleport does not work IRL!");
            return;
        }
        World world;
        if (context.hasParam("world"))
        {
            world = context.getParam("world", null);
            if (world == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "World {input} not found!", context.getString("world"));
                return;
            }
        }
        else if (module.getConfiguration().mainWorld == null || module.getConfiguration().mainWorld.getWorld() == null)
        {
            context.sendTranslated(MessageType.CRITICAL, "Unknown configured main world!");
            context.sendTranslated(MessageType.CRITICAL, "Show this error to an administrator!");
            return;
        }
        else
        {
            world = module.getConfiguration().mainWorld.getWorld();
        }
        Role role = null;
        Location spawnLocation;
        if (context.hasParam("role"))
        {
            String roleName = context.getString("role");
            role = manager.getProvider(world).getRole(roleName);
            if (role == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Could not find the role {input} in {world}!", roleName, world);
                return;
            }
            String rolespawn = role.getRawMetadata().get("rolespawn");
            if (rolespawn == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "The role {name} in {world} has no spawn-point!", role.getName(), world);
                return;
            }
            spawnLocation = this.getSpawnLocation(rolespawn);
            if (spawnLocation == null)
            {
                context.sendTranslated(MessageType.NEGATIVE, "Invalid spawn-location for the role {name}! Please check your role-configuration!\n{}", role.getName(), rolespawn);
                return;
            }
        }
        else
        {
            spawnLocation = world.getSpawnLocation().add(0.5, 0, 0.5);
        }

        boolean force = false;
        if (context.hasFlag("f") && module.perms().COMMAND_SPAWN_FORCE.isAuthorized(context.getSender()))
        {
            force = true; // if not allowed ignore flag
        }

        List<User> tpList = new ArrayList<>();
        if (context.hasArg(0))
        {
            boolean all = false;
            if ("*".equals(context.getString(0)))
            {
                all = true;
                if (!module.perms().COMMAND_SPAWN_ALL.isAuthorized(context.getSender()))
                {
                    context.sendTranslated(MessageType.NEGATIVE, "You are not allowed to spawn everyone!");
                    return;
                }

                for (User user : context.getCore().getUserManager().getOnlineUsers())
                {
                    if (!force && module.perms().COMMAND_SPAWN_PREVENT.isAuthorized(user))
                    {
                        continue;
                    }
                    tpList.add(user);
                }
            }
            else
            {
                String[] names = StringUtils.explode(",", context.getString(0));
                for (String name : names)
                {
                    User user = context.getCore().getUserManager().findUser(name);
                    if (user == null)
                    {
                        context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", context.getString(0));
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
                    tpList.add(user);
                }
            }
            for (User user : tpList)
            {
                this.tpToSpawn(user, spawnLocation, force);
            }
            if (all)
            {
                // BroadCast
                if (role == null)
                {
                    this.module.getCore().getUserManager().broadcastMessage(MessageType.POSITIVE, "Teleported everyone to the spawn of {world}!", world);
                }
                else
                {
                    this.module.getCore().getUserManager().broadcastMessage(MessageType.POSITIVE, "Teleported everyone to the spawn of the role {name#role} in {world}!", role.getName(), world);
                }
            }
            else
            {
                if (role == null)
                {
                    context.getSender().sendTranslatedN(MessageType.POSITIVE, tpList.size(),
                                                        "Teleported {1:user} to the spawn of {world}",
                                                        "Teleported {2:integer#amount} users to the spawn of {world}",
                                                        world, tpList.get(0), tpList.size());
                }
                else
                {
                    context.getSender().sendTranslatedN(MessageType.POSITIVE, tpList.size(),
                                                        "Teleported {2:user} to the spawn of the role {name#role} in {world}",
                                                        "Teleported {3:integer#amount} users to the spawn of the role {name#role} in {world}",
                                                        role.getName(), world, tpList.get(0), tpList.size());
                }
            }
            return;
        }
        // else no user specified
        User user = (User)context.getSender();
        if (role == null)
        {
            RolesAttachment rolesAttachment = user.get(RolesAttachment.class);
            if (rolesAttachment == null)
            {
                this.roles.getLog().warn("Missing RolesAttachment!");
                return;
            }
            String rolespawn = rolesAttachment.getCurrentMetadataString("rolespawn");
            if (rolespawn == null)
            {
                spawnLocation = world.getSpawnLocation();
                Location userLocation = user.getLocation();
                spawnLocation.setPitch(userLocation.getPitch());
                spawnLocation.setYaw(userLocation.getYaw());
                context.sendTranslated(MessageType.POSITIVE, "You are now standing at the spawn of {world}!", world);
            }
            else
            {
                spawnLocation = this.getSpawnLocation(rolespawn);
                if (spawnLocation == null)
                {
                    context.sendTranslated(MessageType.NEGATIVE, "Invalid spawn-location for your role! Please check your role-configurations!\n{}", rolespawn);
                    return;
                }
                context.sendTranslated(MessageType.POSITIVE, "You are now standing at your role-spawn!");
            }
        }
        else
        {
            context.sendTranslated(MessageType.POSITIVE, "You are now standing at the spawn of {name#role} in {world}!", role.getName(), world);
        }
        this.tpToSpawn(user, spawnLocation, force);
    }

    private Location getSpawnLocation(String value)
    {
        try
        {
            String[] spawnStrings = StringUtils.explode(":",value);
            int x = Integer.valueOf(spawnStrings[0]);
            int y = Integer.valueOf(spawnStrings[1]);
            int z = Integer.valueOf(spawnStrings[2]);
            float yaw = Float.valueOf(spawnStrings[3]);
            float pitch = Float.valueOf(spawnStrings[4]);
            World world = this.module.getCore().getWorldManager().getWorld(spawnStrings[5]);
            return new Location(world,x,y,z,yaw, pitch);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private void tpToSpawn(User user, Location spawnLocation, boolean force)
    {
        if (force)
        {
            user.teleport(spawnLocation, TeleportCause.COMMAND);
        }
        else
        {
            user.safeTeleport(spawnLocation,TeleportCause.COMMAND,false);
        }
    }

    public static class RoleCompleter implements Completer
    {
        @Override
        public List<String> complete(CommandSender sender, String token)
        {
            List<String> roles = new ArrayList<>();
            if (sender instanceof User)
            {
                if (((User)sender).get(RolesAttachment.class).getWorkingWorld() != null)
                {
                    for (Role role : manager.getProvider(((User)sender).get(RolesAttachment.class).getWorkingWorld()).getRoles())
                    {
                        roles.add(role.getName());
                    }
                }
                else
                {
                    for (Role role : manager.getProvider(((User)sender).getWorld()).getRoles())
                    {
                        roles.add(role.getName());
                    }
                }
            }
            else
            {
                if (ManagementCommands.curWorldOfConsole != null)
                {
                    for (Role role : manager.getProvider(ManagementCommands.curWorldOfConsole).getRoles())
                    {
                        roles.add(role.getName());
                    }
                }
            }
            return roles;
        }
    }
}
