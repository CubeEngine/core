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
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;

public class TeleportCommands
{
    Basics module;

    public TeleportCommands(Basics module)
    {
        this.module = module;
    }

    private void teleport(User user, Location loc, boolean safe)
    {
        if (safe)
        {
            user.safeTeleport(loc);
            while ((loc.getBlock().getType() != Material.AIR)
                && (new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()).getBlock().getType() != Material.AIR))
            {
                loc.add(0, 1, 0);
            }
            if (!user.isFlying())
            {
                while (loc.clone().add(0, -1, 0).getBlock().getType() == Material.AIR)
                {
                    loc.add(0, -1, 0);
                }
            }
        }
        else
        {
            user.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    @Command(
    desc = "Teleport directly to a player.",
    usage = "<player> [player]",
    min = 1,
    max = 2,
    flags =
    {
        @Flag(longName = "force", name = "f"),
        @Flag(longName = "unsafe", name = "u")
    })
    public void tp(CommandContext context)
    {
        User user1 = context.getSenderAsUser();
        User user2 = context.getUser(0);
        if (user2 == null)
        {
            illegalParameter(context, "basics", "User %s not found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TP_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (!force)
        {
            if (!BasicsPerm.COMMAND_TP_PREVENT_TPTO.isAuthorized(user2))
            {
                denyAccess(context, "basics", "You are not allowed to teleport to %s!", user2.getName());
            }
        }
        if (context.hasIndexed(1))
        {
            user1 = context.getUser(1);
            if (user1 == null)
            {
                illegalParameter(context, "basics", "User %s not found!", context.getString(1));
            }
            if (!force) // if force no need to check
            {
                if (!BasicsPerm.COMMAND_TP_OTHER.isAuthorized(context.getSender()))
                {
                    denyAccess(context, "basics", "You are not allowed to teleport other persons!");
                }
                if (!BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(context.getSender()))
                {
                    denyAccess(context, "basics", "You are not allowed to teleport %s!", user1.getName());
                }
            }
        }
        else
        {
            if (user1 == null)
            {
                invalidUsage(context, "basics", "&cYou are now teleporting yourself into hell!");
            }
        }
        boolean safe = !context.hasFlag("u");
        this.teleport(user1, user2.getLocation(), safe);
        context.sendMessage("basics", "You teleported to %s", user2.getName());
    }

    @Command(
    desc = "Teleport everyone directly to a player.",
    usage = "<player>",
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
            illegalParameter(context, "basics", "User %s not found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TPALL_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (!force)
        {
            if (!BasicsPerm.COMMAND_TP_PREVENT_TPTO.isAuthorized(user))
            {
                denyAccess(context, "basics", "You are not allowed to teleport to %s!", user.getName());
            }
        }
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force)
            {
                if (!BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(player))
                {
                    continue;
                }
            }
            boolean safe = !context.hasFlag("u");
            this.teleport(CubeEngine.getUserManager().getUser(player), user.getLocation(), safe);
        }
        context.sendMessage("basics", "You teleported everyone to %s", user.getName());
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
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "User %s not found!", context.getString(0));
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TPALL_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (!force)
        {
            if (!BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(user))
            {
                denyAccess(context, "bascics", "You are not allowed to teleport %s!", user.getName());
                return;
            }
        }
        boolean safe = !context.hasFlag("u");
        this.teleport(user, sender.getLocation(), safe);
        context.sendMessage("basics", "You teleported %s to you!", user.getName());
    }

    @Command(
    desc = "Teleport every player directly to you.",
    usage = "<player>",
    min = 1,
    max = 1,
    flags =
    {
        @Flag(longName = "force", name = "f"),
        @Flag(longName = "unsafe", name = "u")
    })
    public void tphereall(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TPALL_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        for (Player player : context.getSender().getServer().getOnlinePlayers())
        {
            if (!force)
            {
                if (!BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(player))
                {
                    continue;
                }
            }
            boolean safe = !context.hasFlag("u");
            this.teleport(CubeEngine.getUserManager().getUser(player), sender.getLocation(), safe);
        }
        context.sendMessage("basics", "You teleported everyone to you!");
    }

    @Command(
    desc = "Teleport a directly to you.",
    usage = "<x> [y] <z> [world <world>]",
    min = 2,
    max = 3,
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
        // TODO dynamicly register permissions for each world
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
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
                illegalParameter(context, "basics", "Coordinates have to be numbers");
            }
            y = sender.getWorld().getHighestBlockAt(x, z).getY() + 1;
        }
        if (context.hasNamed("world"))
        {
            world = context.getNamed("world", World.class);
            if (world == null)
            {
                illegalParameter(context, "basics", "World not found!");
            }
        }
        boolean safe = !context.hasFlag("u");
        this.teleport(sender, new Location(world, x, y, z), safe);
        context.sendMessage("basics", "Teleported to Location!");
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
        @Flag(longName = "force", name = "f")
    })
    public void spawn(CommandContext context)
    {
        // TODO make diff. spawns for playergroups possible
        User user = context.getSenderAsUser();
        World world;
        if (user == null && !context.hasIndexed(0))
        {
            invalidUsage(context, "basics", "&eProTip: Teleport does not work IRL!");
        }
        boolean force = false;
        if (context.hasFlag("f"))
        {
            if (BasicsPerm.COMMAND_TP_FORCE.isAuthorized(context.getSender()))
            {
                force = true;
            } // if not allowed ignore flag
        }
        if (context.hasIndexed(0))
        {
            user = context.getUser(0);
            if (user == null)
            {
                illegalParameter(context, "basics", "User not found!");
            }
            if (!force)
            {
                if (!BasicsPerm.COMMAND_TP_PREVENT_TP.isAuthorized(user))
                {
                    denyAccess(context, "basics", "You are not allowed to teleport %s!", user.getName());
                }
            }
        }
        String s_world = module.getConfiguration().spawnMainWorld;
        if (s_world == null)
        {
            world = user.getWorld();
        }
        else
        {
            world = context.getSender().getServer().getWorld(s_world);
        }
        this.teleport(user, world.getSpawnLocation(), true);
    }

    @Command(
    desc = "Requests to teleport to a player.",
    usage = "<player>",
    min = 1,
    max = 1)
    public void tpa(CommandContext context)
    {
        //TODO timeout (configurable)
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        user.sendMessage("basics", "%s wants to teleport to you! Use /tpaccept to accept or /tpdeny to deny the request!", sender.getName());
        user.setAttribute("pendingTpToRequest", sender.getName());
        user.removeAttribute("pendingTpFromRequest");
        context.sendMessage("basics", "Teleport request send to %s!", user.getName());
    }

    @Command(
    desc = "Requests to teleport a player to you.",
    usage = "<player>",
    min = 1,
    max = 1)
    public void tpahere(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
        User user = context.getUser(0);
        if (user == null)
        {
            illegalParameter(context, "basics", "User not found!");
        }
        user.sendMessage("basics", "%s wants to teleport you to him! Use /tpaccept to accept or /tpdeny to deny the request!", sender.getName());
        user.setAttribute("pendingTpFromRequest", sender.getName());
        user.removeAttribute("pendingTpToRequest");
        context.sendMessage("basics", "Teleport request send to %s!", user.getName());
    }

    @Command(
    desc = "Accepts any pending teleport-request.",
    max = 0)
    public void tpaccept(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eNo one wants to teleport to you!");
        String name = sender.getAttribute("pendingTpToRequest");
        if (name == null)
        {
            name = sender.getAttribute("pendingTpFromRequest");
            if (name == null)
            {
                invalidUsage(context, "basics", "You don't have any pending requests!");
            }
            sender.removeAttribute("pendingTpFromRequest");
            User user = module.getUserManager().getUser(name);
            if (!user.isOnline())
            {
                invalidUsage(context, "basics", "%s seems to have disappeared.", user.getName());
            }
            this.teleport(sender, user.getLocation(), true);
            user.sendMessage("bascis", "%s accepted your teleport-request!", sender.getName());
            context.sendMessage("basics", "You accepted to get teleported to %s", user.getName());
        }
        else
        {
            sender.removeAttribute("pendingTpToRequest");
            User user = module.getUserManager().getUser(name);
            if (!user.isOnline())
            {
                invalidUsage(context, "basics", "%s seems to have disappeared.", user.getName());
            }
            this.teleport(user, sender.getLocation(), true);
            user.sendMessage("bascis", "%s accepted your teleport-request!", sender.getName());
            context.sendMessage("basics", "You accepted to teleport to %s", user.getName());
        }
    }

    @Command(
    desc = "Denies any pending teleport-request.",
    max = 0)
    public void tpdeny(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eNo one wants to teleport to you!");
        String tpa = sender.getAttribute("pendingTpToRequest");
        String tpahere = sender.getAttribute("pendingTpFromRequest");
        if (tpa != null)
        {
            sender.removeAttribute("pendingTpToRequest");
            User user = module.getUserManager().getUser(tpa);
            user.sendMessage("basics", "%s denied your teleport-request!", sender.getName());
            context.sendMessage("basics", "You denied %s's teleport-request", user.getName());
        }
        if (tpahere != null)
        {
            sender.removeAttribute("pendingTpFromRequest");
            User user = module.getUserManager().getUser(tpahere);
            user.sendMessage("basics", "%s denied your request!", sender.getName());
            context.sendMessage("basics", "You denied %s's teleport-request", user.getName());
        }
    }

    @Command(
    desc = "Jumps to the position you are looking at.",
    max = 0)
    public void jumpTo(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eJumping in the console is not allowed! Go play outside!");
        this.teleport(sender, sender.getTargetBlock(null, 250).getLocation(), true);
        context.sendMessage("basics", "&aYou just jumped!");
    }

    @Command(
    desc = "Teleports you to your last location",
    max = 0,
    flags =
    {
        @Flag(longName = "force", name = "f"),
        @Flag(longName = "unsafe", name = "u")
    })
    public void back(CommandContext context)
    {
        //TODO back on death
        User sender = context.getSenderAsUser("basics", "You never teleported!");
        Location loc = sender.getAttribute("lastLocation");
        if (loc == null)
        {
            invalidUsage(context, "basics", "You never teleported!");
        }
        boolean safe = !context.hasFlag("u");
        this.teleport(sender, loc, safe);
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
            illegalParameter(context, "basics", "World not found!");
        }
        this.teleport(sender, world.getSpawnLocation(), true);
        context.sendMessage("basics", "Teleported to the spawn of world %s", world.getName());
    }
    
    /*
     * /up
     * /ascend
     * /descend   
      like WE 
     */
}