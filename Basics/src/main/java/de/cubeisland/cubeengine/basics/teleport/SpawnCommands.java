package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.bukkit.World;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

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
                paramNotFound(context, "basics", "&cWorld: %s not found", context.getString(0));
            }
        }
        else
        {
            if (sender == null)
            {
                invalidUsage(context, "basics", "&cIf not used ingame you have to specify a world and coordinates!");
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
                illegalParameter(context, "basics", "&cCoordinates are invalid!");
            }
        }
        else
        {
            if (sender == null)
            {
                invalidUsage(context, "basics", "&cIf not used ingame you have to specify a world and coordinates!");
            }
            final Location loc = sender.getLocation();
            x = loc.getBlockX();
            y = loc.getBlockY();
            z = loc.getBlockZ();
        }
        world.setSpawnLocation(x, y, z);
        context.sendMessage("bascics", "&aSpawn was in world %s set to &eX:&6%d &eY:&6%d &eZ:&6%d", world.getName(), x, y, z);
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
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_SPAWN_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (context.hasParam("world"))
        {
            world = context.getParam("world", null);
            if (world == null)
            {
                paramNotFound(context, "basics", "&cWorld not found!");
            }
        }
        if (context.hasFlag("a"))
        {
            if (!BasicsPerm.COMMAND_SPAWN_ALL.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "&cYou are not allowed to spawn everyone!");
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
                TeleportCommands.teleport(player, loc, true, force, true);
            }
            this.basics.getUserManager().broadcastMessage("basics", "&aTeleported everyone to the spawn of %s!", world.getName());
            return;
        }
        if (user == null && !context.hasArg(0))
        {
            invalidUsage(context, "basics", "&6ProTip: &cTeleport does not work IRL!");
        }

        if (context.hasArg(0))
        {
            user = context.getUser(0);
            if (user == null)
            {
                paramNotFound(context, "basics", "&cUser &2%s &cnot found!", context.getString(0));
            }
            if (!force)
            {
                if (BasicsPerm.COMMAND_SPAWN_PREVENT.isAuthorized(user))
                {
                    denyAccess(context, "basics", "&cYou are not allowed to spawn %s!", user.getName());
                }
            }
        }
        final Location spawnLocation = world.getSpawnLocation().add(0.5, 0, 0.5);
        final Location userLocation = user.getLocation();
        spawnLocation.setPitch(userLocation.getPitch());
        spawnLocation.setYaw(userLocation.getYaw());
        SpawnCommandEvent event = new SpawnCommandEvent(this.basics, user, spawnLocation);
        this.basics.getEventManager().fireEvent(event); // catch this event to change spawn location
        TeleportCommands.teleport(user, event.getLoc(), true, force, true);
    }

    @Command(desc = "Teleports you to the spawn of given world", usage = "<world>", min = 1, max = 1)
    public void tpworld(CommandContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendMessage("basics", "&eProTip: Teleport does not work IRL!");
            return;
        }
        World world = context.getArg(0, World.class, null);
        if (world == null)
        {
            illegalParameter(context, "basics", "&cWorld not found!");
        }
        final Location spawnLocation = world.getSpawnLocation().add(0.5, 0, 0.5);
        final Location userLocation = sender.getLocation();
        spawnLocation.setPitch(userLocation.getPitch());
        spawnLocation.setYaw(userLocation.getYaw());
        TeleportCommands.teleport(sender, spawnLocation, true, false, true);
        context.sendMessage("basics", "&aTeleported to the spawn of world &6%s&a!", world.getName());
    }
}
