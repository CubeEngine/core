package de.cubeisland.cubeengine.spawn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.ConfigRole;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;

public class SpawnCommands
{

    private final Roles roles;
    private final Spawn module;

    public SpawnCommands(Roles roles, Spawn module)
    {
        this.roles = roles;
        this.module = module;
    }

    @Command(desc = "Changes the global respawnpoint", usage = "[role] [<x> <y> <z>] [world]", max = 4)
    //TODO set global spawn from console
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
            yaw = loc.getYaw();
            pitch = loc.getPitch();
        }

        if (context.hasArg(0))
        {
            ConfigRole role = roles.getApi().getRole(world,context.getString(0));
            if (role == null)
            {
               context.sendTranslated("&cCould not find the role &6%s&c in &6%s&c!",context.getString(0),world.getName());
            }
            else
            {
                String[] locStrings = new String[6];
                locStrings[0] = x.toString();
                locStrings[1] = y.toString();
                locStrings[2] = z.toString();
                locStrings[3] = String.valueOf(yaw);
                locStrings[4] = String.valueOf(pitch);
                locStrings[5] = world.getName();
                role.setMetaData("rolespawn", StringUtils.implode(":",locStrings));
                roles.getApi().recalculateDiryRoles();
            }
        }
        else
        {
            world.setSpawnLocation(x, y, z);
            context.sendTranslated("&aThe spawn in &6%s&a is now set to &eX:&6%d &eY:&6%d &eZ:&6%d", world.getName(), x, y, z);
        }
    }

    @Command(desc = "Teleport directly to the worlds spawn.", usage = "[player] [world <world>] [role <role>]", max = 2,
             params = {@Param(names = {
                 "world", "w", "in"
             }, type = World.class),
                       @Param(names = {
                           "role", "r"
                       }, type = String.class) //TODO tabcompleter
             } , flags = {
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
        if (world == null)
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
                context.sendTranslated("&cWorld not found!");
                return;
            }
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
                String rolespawn = roles.getApi().getMetaData(user, world, "rolespawn");
                if (rolespawn == null)
                {
                    spawnLocation = this.getSpawnLocation(rolespawn);
                    if (spawnLocation == null)
                    {
                        context.sendTranslated("&cInvalid spawn-location for the role of &2%s&c! &ePlease check your role-configurations!\n&7%s",
                                               user.getName(),rolespawn);
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
            ConfigRole role = this.roles.getApi().getRole(world,roleName);
            if (role == null)
            {
                context.sendTranslated("&cCould not find the role &6%s&c in &6%s&c!",roleName,world.getName());
                return;
            }
            RoleMetaData rolespawn = role.getMetaData().get("rolespawn");
            if (rolespawn == null)
            {
                context.sendTranslated("&cThe role &6%s&c in &6%s&c has no spawn-point!",role.getName(),world.getName());
                return;
            }
            spawnLocation = this.getSpawnLocation(rolespawn.getValue());
            if (spawnLocation == null)
            {
                context.sendTranslated("&cInvalid spawn-location for the role &6%s&c! &ePlease check your role-configuration!\n&7%s",
                                       role.getName(),rolespawn.getValue());
                return;
            }
            context.sendTranslated("&aYou are now standing at the spawn of &6%s&a!",role.getName());
        }
        else
        {
            String rolespawn = roles.getApi().getMetaData(user, world, "rolespawn");
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
}
