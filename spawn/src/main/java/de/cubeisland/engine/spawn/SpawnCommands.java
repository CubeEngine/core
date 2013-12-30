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

    @Command(desc = "Changes the respawnpoint", usage = "[<role>|global] [<x> <y> <z>] [world]", max = 4)
    public void setSpawn(CommandContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        Double x;
        Double y;
        Double z;
        float yaw = 0;
        float pitch = 0;
        World world;
        if (context.hasArg(4))
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
                context.sendTranslated("&eUse &6\"global\"&e instead of the role-name to set the default spawn.");
                return;
            }
            world = sender.getWorld();
        }
        if (context.hasArg(3))
        {
            x = context.getArg(1, Double.class, null);
            y = context.getArg(2, Double.class, null);
            z = context.getArg(3, Double.class, null);
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
                context.sendTranslated("&eUse &6\"global\"&e instead of the role-name to set the default spawn.");
                return;
            }
            final Location loc = sender.getLocation();
            x = loc.getX();
            y = loc.getY();
            z = loc.getZ();
            yaw = loc.getYaw();
            pitch = loc.getPitch();
        }
        if (context.hasArg(0))
        {
            Role role = manager.getProvider(world).getRole(context.getString(0));
            if (role == null)
            {
                if (!context.getString(0).equalsIgnoreCase("global"))
                {
                    context.sendTranslated("&cCould not find the role &6%s&c in &6%s&c!",context.getString(0),world.getName());
                    return;
                }
            }
            else
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
                return;
            }
        }
        this.module.getCore().getEventManager().fireEvent(
            new WorldSetSpawnEvent(this.module.getCore(), world, new Location(world, x,y,z, yaw, pitch)));
        world.setSpawnLocation(x.intValue(), y.intValue(), z.intValue());
        context.sendTranslated("&aThe spawn in &6%s&a is now set to &eX:&6%d &eY:&6%d &eZ:&6%d", world.getName(), x.intValue(), y.intValue(), z.intValue());
    }

    @Command(desc = "Teleport directly to the worlds spawn.", usage = "[player] [world <world>] [role <role>]", max = 2,
             params = {
                 @Param(names = {"world", "w", "in"}, type = World.class),
                 @Param(names = {"role", "r"}, type = String.class, completer = RoleCompleter.class)
             } ,
             flags = {
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
        World world = module.getConfiguration().mainWorld;
        if (world == null && user != null)
        {
            world = user.getWorld();
        }
        boolean force = false;
        if (context.hasFlag("f") && SpawnPerms.COMMAND_SPAWN_FORCE.isAuthorized(context.getSender()))
        {
            force = true; // if not allowed ignore flag
        }
        if (context.hasParam("world"))
        {
            world = context.getParam("world", null);
            if (world == null)
            {
                context.sendTranslated("&cWorld &6%s&c not found!", context.getString("world"));
                return;
            }
        }
        if (world == null)
        {
            context.sendTranslated("&cYou have to specify a world!");
            return;
        }
        if (context.hasFlag("a"))
        {
            if (!SpawnPerms.COMMAND_SPAWN_ALL.isAuthorized(context.getSender()))
            {
                context.sendTranslated("&cYou are not allowed to spawn everyone!");
                return;
            }
            Location loc = world.getSpawnLocation().add(0.5, 0, 0.5);
            for (User player : context.getCore().getUserManager().getOnlineUsers())
            {
                final Location spawnLocation;
                RolesAttachment rolesAttachment = player.get(RolesAttachment.class);
                if (rolesAttachment == null)
                {
                    this.roles.getLog().warn("Missing RolesAttachment!");
                    return;
                }
                String rolespawn = rolesAttachment.getCurrentMetadataString("rolespawn");
                if (rolespawn == null)
                {
                    spawnLocation = this.getSpawnLocation(rolespawn);
                    if (spawnLocation == null)
                    {
                        context.sendTranslated("&cInvalid spawn-location for the role of &2%s&c! &ePlease check your role-configurations!\n&7%s",
                                               player.getName(),rolespawn);
                        return;
                    }
                    spawnLocation.add(0.5, 0, 0.5);
                }
                else
                {
                    spawnLocation = loc;
                }
                if (!force)
                {
                    if (SpawnPerms.COMMAND_SPAWN_PREVENT.isAuthorized(player))
                    {
                        continue;
                    }
                }
                this.tpToSpawn(user,spawnLocation,force);
                return;
            }
            this.module.getCore().getUserManager().broadcastMessage("&aTeleported everyone to the spawn of %s!", world.getName());
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
            if (!force && SpawnPerms.COMMAND_SPAWN_PREVENT.isAuthorized(user))
            {
                context.sendTranslated("&cYou are not allowed to spawn %s!", user.getName());
                return;
            }
        }
        final Location spawnLocation;
        if (context.hasParam("role"))
        {
            String roleName = context.getString("role");
            Role role = manager.getProvider(world).getRole(roleName);
            if (role == null)
            {
                context.sendTranslated("&cCould not find the role &6%s&c in &6%s&c!",roleName,world.getName());
                return;
            }
            String rolespawn = role.getRawMetadata().get("rolespawn");
            if (rolespawn == null)
            {
                context.sendTranslated("&cThe role &6%s&c in &6%s&c has no spawn-point!",role.getName(),world.getName());
                return;
            }
            spawnLocation = this.getSpawnLocation(rolespawn);
            if (spawnLocation == null)
            {
                context.sendTranslated("&cInvalid spawn-location for the role &6%s&c! &ePlease check your role-configuration!\n&7%s",
                                       role.getName(),rolespawn);
                return;
            }
            context.sendTranslated("&aYou are now standing at the spawn of &6%s&a!",role.getName());
        }
        else
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
            }
            else
            {
                spawnLocation = this.getSpawnLocation(rolespawn);
                if (spawnLocation == null)
                {
                    context.sendTranslated("&cInvalid spawn-location for your role! &ePlease check your role-configurations!\n&7%s",rolespawn);
                    return;
                }
            }
        }
        spawnLocation.add(0.5, 0, 0.5);
        this.tpToSpawn(user,spawnLocation,force);
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
