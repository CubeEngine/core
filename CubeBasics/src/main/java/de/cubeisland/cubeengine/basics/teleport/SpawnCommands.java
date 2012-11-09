package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
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
    
    @Command(
        desc = "Changes the global respawnpoint",
        usage = "[world] [<x> <y> <z>]",
        max = 4)
    public void setSpawn(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        Integer x;
        Integer y;
        Integer z;
        World world;
        if (context.hasIndexed(0))
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

        if (context.hasIndexed(3))
        {
            x = context.getIndexed(1, Integer.class, null);
            y = context.getIndexed(2, Integer.class, null);
            z = context.getIndexed(3, Integer.class, null);
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
            x = sender.getLocation().getBlockX();
            y = sender.getLocation().getBlockY();
            z = sender.getLocation().getBlockZ();
        }
        world.setSpawnLocation(x, y, z);
        context.sendMessage("bascics", "&aSpawn was in world %s set to &eX:&6%d &eY:&6%d &eZ:&6%d", world.getName(), x, y, z);
    }

    @Command(
        desc = "Teleport directly to the worlds spawn.",
        usage = "[player] [world <world>]",
        max = 2,
        params = { @Param(names = { "world", "w" }, types = { World.class }) },
        flags =
        {
            @Flag(longName = "force", name = "f"),
            @Flag(longName = "all", name = "a")
        })
    public void spawn(CommandContext context)
    {
        // TODO later make diff. spawns for playergroups/roles possible
        User user = context.getSenderAsUser();
        World world;
        String s_world = basics.getConfiguration().spawnMainWorld;
        if (s_world == null)
        {
            world = user.getWorld();
        }
        else
        {
            world = context.getSender().getServer().getWorld(s_world);
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_SPAWN_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (context.hasNamed("world"))
        {
            world = context.getNamed("world", World.class, null);
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
                loc.setPitch(player.getLocation().getPitch());
                loc.setYaw(player.getLocation().getYaw());
                TeleportCommands.teleport(player, loc, true, force);
            }
            this.basics.getUserManager().broadcastMessage("basics", "&aTeleported everyone to the spawn of %s!", world.getName());
            return;
        }
        if (user == null && !context.hasIndexed(0))
        {
            invalidUsage(context, "basics", "&6ProTip: &cTeleport does not work IRL!");
        }

        if (context.hasIndexed(0))
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
        Location loc = world.getSpawnLocation().add(0.5, 0, 0.5);
        loc.setPitch(user.getLocation().getPitch());
        loc.setYaw(user.getLocation().getYaw());
        TeleportCommands.teleport(user, world.getSpawnLocation().add(0.5, 0, 0.5), true, force);
    }

    @Command(
        desc = "Teleports you to the spawn of given world",
        usage = "<world>",
        min = 1,
        max = 1)
    public void tpworld(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
        World world = context.getIndexed(0, World.class, null);
        if (world == null)
        {
            illegalParameter(context, "basics", "&cWorld not found!");
        }
        Location loc = world.getSpawnLocation().add(0.5, 0, 0.5);
        loc.setPitch(sender.getLocation().getPitch());
        loc.setYaw(sender.getLocation().getYaw());
        TeleportCommands.teleport(sender, loc, true, false);
        context.sendMessage("basics", "&aTeleported to the spawn of world &6%s&a!", world.getName());
    }
}
