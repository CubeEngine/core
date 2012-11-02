package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

public class TeleportCommands
{
    private Basics basics;

    public TeleportCommands(Basics basics)
    {
        this.basics = basics;
    }

    private void teleport(User user, Location loc, boolean safe, boolean force)
    {
        if (!force && !user.getWorld().equals(loc.getWorld()))
        {
            if (!TpWorldPermissions.getPermission(loc.getWorld().getName()).isAuthorized(user))
            {
                denyAccess(user, "basics", "You are not allowed to teleport to this world!");
            }
        }
        if (safe)
        {
            user.safeTeleport(loc);
        }
        else
        {
            user.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
        }
    }

    @Command(
    desc = "Teleport directly to a player.",
    usage = "<player> [player] [-unsafe]",
    min = 1,
    max = 2,
    flags =
    {
        @Flag(longName = "force", name = "f"), // is not shown directly in usage
        @Flag(longName = "unsafe", name = "u")
    })
    public void tp(CommandContext context)
    {
        User user = context.getSenderAsUser();
        User target = context.getUser(0);
        if (target == null)
        {
            illegalParameter(context, "basics", "&cUser %s not found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TP_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (context.hasIndexed(1)) //tp player1 player2
        {
            user = target; // The first user is not the target
            target = context.getUser(1);
            if (target == null)
            {
                illegalParameter(context, "basics", "&cUser %s not found!", context.getString(1));
            }
            if (!force) // if force no need to check
            {
                if (!BasicsPerm.COMMAND_TP_OTHER.isAuthorized(context.getSender())) // teleport other persons
                {
                    denyAccess(context, "basics", "&cYou are not allowed to teleport other persons!");
                }
                if (BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(user)) // teleport the user
                {
                    denyAccess(context, "basics", "&cYou are not allowed to teleport %s!", user.getName());
                }
                if (BasicsPerm.COMMAND_TP_PREVENT_TPTO.isAuthorized(target)) // teleport to the target
                {
                    denyAccess(context, "basics", "&cYou are not allowed to teleport to %s!", target.getName());
                }
            }
        }
        else
        {
            if (user == null) // if not tp other persons console cannot use this
            {
                invalidUsage(context, "basics", "&cYou are now teleporting yourself into hell!");
            }
        }
        if (!force)
        {
            if (BasicsPerm.COMMAND_TP_PREVENT_TPTO.isAuthorized(target)) // Check if no force & target does not prevent
            {
                if (BasicsPerm.COMMAND_TP_FORCE.isAuthorized(context.getSender()))
                {
                    context.sendMessage("basics", "&aUse the &e-force (-f) &aflag to teleport to this player."); //Show force flag if has permission
                }
                denyAccess(context, "basics", "&cYou are not allowed to teleport to %s!", target.getName());
            }
        }
        boolean safe = !context.hasFlag("u");
        this.teleport(user, target.getLocation(), safe, force);
        context.sendMessage("basics", "&aYou teleported to %s", target.getName());
    }

    @Command(
    desc = "Teleport everyone directly to a player.",
    usage = "<player> [-unsafe]",
    min = 1,
    max = 1,
    flags =
    {
        @Flag(longName = "force", name = "f"),
        @Flag(longName = "unsafe", name = "u")
    })
    public void tpall(CommandContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "&cUser %s not found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TP_FORCE_TPALL.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (!force)
        {
            if (BasicsPerm.COMMAND_TP_PREVENT_TPTO.isAuthorized(user))
            {
                denyAccess(context, "basics", "&cYou are not allowed to teleport to %s!", user.getName());
            }
        }
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force)
            {
                if (BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(player))
                {
                    continue;
                }
            }
            boolean safe = !context.hasFlag("u");
            this.teleport(CubeEngine.getUserManager().getExactUser(player), user.getLocation(), safe, force);
        }
        context.getCore().getUserManager().broadcastMessage("basucs", "&aTeleporting everyone to %s", user.getName());
    }

    @Command(
    desc = "Teleport a player directly to you.",
    usage = "<player>",
    min = 1,
    max = 1,
    flags =
    {
        @Flag(longName = "force", name = "f"),
        @Flag(longName = "unsafe", name = "u")
    })
    public void tphere(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&6ProTip: &cTeleport does not work IRL!");
        User target = context.getUser(0);
        if (target == null)
        {
            illegalParameter(context, "basics", "&cUser %s not found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TPHERE_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (!force)
        {
            if (BasicsPerm.COMMAND_TPHERE_PREVENT.isAuthorized(target))
            {
                denyAccess(context, "bascics", "&cYou are not allowed to teleport %s!", target.getName());
                return;
            }
        }
        boolean safe = !context.hasFlag("u");
        this.teleport(target, sender.getLocation(), safe, force);
        context.sendMessage("basics", "&aYou teleported %s to you!", target.getName());
        target.sendMessage("basics", "&aYou were teleported to %s", sender.getName());
    }

    @Command(
    desc = "Teleport every player directly to you.",
    max = 0,
    flags =
    {
        @Flag(longName = "force", name = "f"),
        @Flag(longName = "unsafe", name = "u")
    })
    public void tphereall(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&6ProTip: &cTeleport does not work IRL!");
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TPHEREALL_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force)
            {
                if (BasicsPerm.COMMAND_TPHEREALL_PREVENT.isAuthorized(player))
                {
                    continue;
                }
            }
            boolean safe = !context.hasFlag("u");
            this.teleport(CubeEngine.getUserManager().getExactUser(player), sender.getLocation(), safe, force);
        }
        context.sendMessage("basics", "&aYou teleported everyone to you!");
        context.getCore().getUserManager().broadcastMessage("basics", "&aTeleporting everyone to %s", sender.getName());
    }

    @Command(
    desc = "Teleport a directly to you.",
    usage = "<x> [y] <z> [world <world>]",
    min = 2,
    max = 4,
    params =
    {
        @Param(names =
        {
            "world", "w"
        }, types =
        {
            World.class
        })
    },
    flags =
    {
        @Flag(longName = "unsafe", name = "u")
    })
    public void tppos(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&6ProTip: &cTeleport does not work IRL!");
        Integer x = context.getIndexed(0, Integer.class, null);
        Integer y;
        Integer z;
        World world = sender.getWorld();
        if (context.hasIndexed(2))
        {
            y = context.getIndexed(1, Integer.class, null);
            z = context.getIndexed(2, Integer.class, null);
        }
        else
        {
            z = context.getIndexed(1, Integer.class, null);
            if (x == null || z == null)
            {
                illegalParameter(context, "basics", "&cCoordinates have to be numbers!");
            }
            y = sender.getWorld().getHighestBlockAt(x, z).getY() + 1;
        }
        if (context.hasNamed("world"))
        {
            world = context.getNamed("world", World.class);
            if (world == null)
            {
                illegalParameter(context, "basics", "&cWorld not found!");
            }
        }
        boolean safe = !context.hasFlag("u");
        Location loc = new Location(world, x, y, z).add(0.5, 0, 0.5);
        loc.setYaw(sender.getLocation().getYaw());
        loc.setPitch(sender.getLocation().getPitch());
        this.teleport(sender, loc, safe, false);
        context.sendMessage("basics", "&aTeleported to &eX:&6%d &eY:&6%d &eZ:&6%d &ain %s!", x, y, z, world.getName());
    }

    @Command(
    desc = "Teleport directly to the worlds spawn.",
    usage = "[player] [world <world>]",
    max = 2,
    params =
    {
        @Param(names =
        {
            "world", "w"
        }, types =
        {
            World.class
        })
    },
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
        if (context.hasFlag("a"))
        {
            if (!BasicsPerm.COMMAND_SPAWN_ALL.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "&cYou are not allowed to spawn everyone!");
            }
            for (User player : context.getCore().getUserManager().getOnlineUsers())
            {
                if (!force)
                {
                    if (BasicsPerm.COMMAND_SPAWN_PREVENT.isAuthorized(player))
                    {
                        continue;
                    }
                }
                this.teleport(player, world.getSpawnLocation().add(0.5, 0, 0.5), true, force);
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
                illegalParameter(context, "basics", "&cUser %s not found!", context.getString(0));
            }
            if (!force)
            {
                if (BasicsPerm.COMMAND_SPAWN_PREVENT.isAuthorized(user))
                {
                    denyAccess(context, "basics", "&cYou are not allowed to spawn %s!", user.getName());
                }
            }
        }
        this.teleport(user, world.getSpawnLocation().add(0.5, 0, 0.5), true, force);
    }

    @Command(
    desc = "Requests to teleport to a player.",
    usage = "<player>",
    min = 1,
    max = 1)
    public void tpa(CommandContext context)
    {
        final User sender = context.getSenderAsUser("basics", "&6ProTip: &cTeleport does not work IRL!");
        sender.removeAttribute(basics, "tpRequestCancelTask");
        final User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        user.sendMessage("basics", "&2%s &awants to teleport to you!\nUse &e/tpaccept &ato accept or &c/tpdeny &ato deny the request!", sender.getName());
        user.setAttribute(basics, "pendingTpToRequest", sender.getName());
        user.removeAttribute(basics, "pendingTpFromRequest");
        context.sendMessage("basics", "&aTeleport request send to &2%s!", user.getName());
        int waitTime = this.basics.getConfiguration().tpRequestWait * 20;
        if (waitTime > 0)
        {
            final int taskID = context.getCore().getTaskManager().scheduleSyncDelayedTask(basics, new Runnable()
            {
                public void run()
                {
                    user.removeAttribute(basics, "tpRequestCancelTask");
                    user.removeAttribute(basics, "pendingTpToRequest");
                    sender.sendMessage("basics", "&2%s &cdid not accept your teleport-request.", user.getName());
                    user.sendMessage("basics", "&cTeleport-request of &2%s &ctimed out.", sender.getName());
                }
            }, waitTime); // wait x - seconds
            Integer oldtaskID = (Integer)user.getAttribute(basics, "tpRequestCancelTask");
            if (oldtaskID != null)
            {
                context.getCore().getTaskManager().cancelTask(basics, oldtaskID);
            }
            user.setAttribute(basics, "tpRequestCancelTask", taskID);
        }
    }

    @Command(
    desc = "Requests to teleport a player to you.",
    usage = "<player>",
    min = 1,
    max = 1)
    public void tpahere(CommandContext context)
    {
        final User sender = context.getSenderAsUser("basics", "&6ProTip: &cTeleport does not work IRL!");
        sender.removeAttribute(basics, "tpRequestCancelTask");
        final User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        user.sendMessage("basics", "&2%s &awants to teleport you to him!\nUse &e/tpaccept &ato accept or &c/tpdeny &ato deny the request!", sender.getName());
        user.setAttribute(basics, "pendingTpFromRequest", sender.getName());
        user.removeAttribute(basics, "pendingTpToRequest");
        context.sendMessage("basics", "&aTeleport request send to &2%s!", user.getName());
        int waitTime = this.basics.getConfiguration().tpRequestWait * 20;
        if (waitTime > 0)
        {
            final int taskID = context.getCore().getTaskManager().scheduleSyncDelayedTask(basics, new Runnable()
            {
                public void run()
                {
                    user.removeAttribute(basics, "tpRequestCancelTask");
                    user.removeAttribute(basics, "pendingTpFromRequest");
                    sender.sendMessage("basics", "&2%s &cdid not accept your teleport-request.", user.getName());
                    user.sendMessage("basics", "&cTeleport-request of &2%s &ctimed out.", sender.getName());
                }
            }, waitTime); // wait x - seconds
            Integer oldtaskID = (Integer)user.getAttribute(basics, "tpRequestCancelTask");
            if (oldtaskID != null)
            {
                context.getCore().getTaskManager().cancelTask(basics, oldtaskID);
            }
            user.setAttribute(basics, "tpRequestCancelTask", taskID);
        }
    }

    @Command(
    names =
    {
        "tpac", "tpaccept"
    },
    desc = "Accepts any pending teleport-request.",
    max = 0)
    public void tpaccept(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&cNo one wants to teleport to you!");
        String name = sender.getAttribute(basics, "pendingTpToRequest");
        if (name == null)
        {
            name = sender.getAttribute(basics, "pendingTpFromRequest");
            if (name == null)
            {
                invalidUsage(context, "basics", "&cYou don't have any pending requests!");
            }
            sender.removeAttribute(basics, "pendingTpFromRequest");
            User user = basics.getUserManager().getUser(name, false);
            if (user == null || !user.isOnline())
            {
                invalidUsage(context, "basics", "&2%s &cseems to have disappeared.", user.getName());
            }
            this.teleport(sender, user.getLocation(), true, false);
            user.sendMessage("bascis", "&2%s &aaccepted your teleport-request!", sender.getName());
            context.sendMessage("basics", "&aYou accepted to get teleported to &2%s&a!", user.getName());
        }
        else
        {
            sender.removeAttribute(basics, "pendingTpToRequest");
            User user = basics.getUserManager().getUser(name, false);
            if (user == null || !user.isOnline())
            {
                invalidUsage(context, "basics", "&2%s &cseems to have disappeared.", user.getName());
            }
            this.teleport(user, sender.getLocation(), true, false);

            user.sendMessage("bascis", "&2%s &aaccepted your teleport-request!", sender.getName());
            context.sendMessage("basics", "&aYou accepted to teleport to &2%s&a!", user.getName());
        }
        Integer taskID = (Integer)sender.getAttribute(basics, "tpRequestCancelTask");
        sender.removeAttribute(basics, "tpRequestCancelTask");
        if (taskID != null)
        {
            context.getCore().getTaskManager().cancelTask(basics, taskID);
        }
    }

    @Command(
    desc = "Denies any pending teleport-request.",
    max = 0)
    public void tpdeny(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&cNo one wants to teleport to you!");
        String tpa = sender.getAttribute(basics, "pendingTpToRequest");
        String tpahere = sender.getAttribute(basics, "pendingTpFromRequest");
        if (tpa != null)
        {
            sender.removeAttribute(basics, "pendingTpToRequest");
            User user = basics.getUserManager().getUser(tpa, false);
            if (user == null)
            {
                throw new IllegalStateException("User saved in \"pendingTpToRequest\" was not found!");
            }
            user.sendMessage("basics", "&2%s &cdenied your teleport-request!", sender.getName());
            context.sendMessage("basics", "&cYou denied &a%s's &cteleport-request!", user.getName());
        }
        else if (tpahere != null)
        {
            sender.removeAttribute(basics, "pendingTpFromRequest");
            User user = basics.getUserManager().getUser(tpahere, false);
            if (user == null)
            {
                throw new IllegalStateException("User saved in \"pendingTpFromRequest\" was not found!");
            }
            user.sendMessage("basics", "&2%s &cdenied your request!", sender.getName());
            context.sendMessage("basics", "&cYou denied &2%s's &cteleport-request", user.getName());
        }
        else
        {
            invalidUsage(context, "basics", "&cYou don't have any pending requests!");
        }
        Integer taskID = (Integer)sender.getAttribute(basics, "tpRequestCancelTask");
        sender.removeAttribute(basics, "tpRequestCancelTask");
        if (taskID != null)
        {
            context.getCore().getTaskManager().cancelTask(basics, taskID);
        }
    }

    @Command(
    desc = "Jumps to the position you are looking at.",
    max = 0)
    public void jumpTo(CommandContext context)
    {//TODO compass teleport ftw!
        User sender = context.getSenderAsUser("basics", "&eJumping in the console is not allowed! Go play outside!");
        Location loc = sender.getTargetBlock(null, 350).getLocation().add(0.5, 1, 0.5);
        if (loc.getBlock().getType().equals(Material.AIR))
        {
            invalidUsage(context, "basics", "&cNo block in sight!");
        }
        loc.setYaw(sender.getLocation().getYaw());
        loc.setPitch(sender.getLocation().getPitch());
        this.teleport(sender, loc, true, false);
        context.sendMessage("basics", "&aYou just jumped!");
    }

    @Command(
    desc = "Teleports you to your last location",
    max = 0,
    flags =
    {
        @Flag(longName = "unsafe", name = "u")
    })
    public void back(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "You never teleported!");
        Location loc = sender.getAttribute(basics, "lastLocation");
        if (loc == null)
        {
            invalidUsage(context, "basics", "You never teleported!");
        }
        boolean safe = !context.hasFlag("u");
        this.teleport(sender, loc, safe, true);
        sender.sendMessage("basics", "Teleported to your last location!");
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
        this.teleport(sender, world.getSpawnLocation(), true, false);
        context.sendMessage("basics", "&aTeleported to the spawn of world &6%s", world.getName());
    }

    @Command(
    desc = "Teleports you x-amount of blocks into the air and puts a glasblock beneath you.",
    usage = "<height>",
    min = 1,
    max = 1)
    public void up(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
        int height = context.getIndexed(0, Integer.class, -1);
        if ((height < 0))
        {
            illegalParameter(context, "basics", "&cInvalid height. The height has to be a number greater than 0!");
        }
        Location loc = sender.getLocation();
        loc.add(0, height - 1, 0);
        if (loc.getBlockY() > loc.getWorld().getMaxHeight()) // Over highest loc
        {
            loc.setY(loc.getWorld().getMaxHeight());
        }
        Block block = loc.getWorld().getBlockAt(loc);
        if (!(block.getRelative(BlockFace.UP, 1).getType().equals(Material.AIR)
            && block.getRelative(BlockFace.UP, 2).getType().equals(Material.AIR)))
        {
            invalidUsage(context, "basics", "&cYour destination seems to be obstructed!");
        }
        loc.add(0.5, 1, 0.5);
        if (block.getType().equals(Material.AIR))
        {
            block.setType(Material.GLASS);
        }
        this.teleport(sender, loc, true, false); // is save anyway so we do not need to check again
        context.sendMessage("basics", "&aYou just lifted!");
    }

    @Command(
    desc = "Teleports you to the next safe spot upwards.",
    max = 0)
    public void ascend(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
        Location loc = sender.getLocation();
        //go upwards until hitting solid blocks
        while (loc.getBlock().getType().equals(Material.AIR) && loc.getBlockY() < loc.getWorld().getMaxHeight())
        {
            loc.add(0, 1, 0);
        }
        // go upwards until hitting 2 airblocks again
        while (!((loc.getBlock().getType().equals(Material.AIR))
            && (loc.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR)))
            && loc.getBlockY() + 1 < loc.getWorld().getMaxHeight())
        {
            loc.add(0, 1, 0);
        }
        if (loc.getWorld().getHighestBlockYAt(loc) < loc.getBlockY())
        {
            loc.setY(loc.getWorld().getHighestBlockYAt(loc));
        }
        if (loc.getY() <= sender.getLocation().getY())
        {
            invalidUsage(context, "bascics", "&cYou cannot ascend here");
        }
        //reached new location
        context.sendMessage("basics", "&aAscended a level!");
        this.teleport(sender, loc, true, false);
    }

    @Command(
    desc = "Teleports you to the next safe spot downwards.",
    max = 0)
    public void descend(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
        Location loc = sender.getLocation();
        //go downwards until hitting solid blocks
        while (loc.getBlock().getType().equals(Material.AIR) && loc.getBlockY() < loc.getWorld().getMaxHeight())
        {
            loc.add(0, -1, 0);
        }
        // go downwards until hitting 2 airblocks & a solid block again 
        while (!((loc.getBlock().getType().equals(Material.AIR))
            && (loc.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR))
            && (!loc.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR)))
            && loc.getBlockY() + 1 < loc.getWorld().getMaxHeight())
        {
            loc.add(0, -1, 0);
        }
        if ((loc.getY() <= 0) || (loc.getY() >= sender.getLocation().getY()))
        {
            invalidUsage(context, "bascics", "&cYou cannot descend here");
        }
        //reached new location
        context.sendMessage("basics", "%aDescended a level!");
        this.teleport(sender, loc, true, false);
    }
}