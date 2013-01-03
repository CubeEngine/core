package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;

/**
 * Contains commands for fast movement. 
 * /up 
 * /ascend 
 * /descend 
 * /jumpto 
 * /through /thru 
 * /back 
 * /place /put
 */
public class MovementCommands
{
    private Basics basics;

    public MovementCommands(Basics basics)
    {
        this.basics = basics;
    }

    @Command(desc = "Teleports you x-amount of blocks into the air and puts a glasblock beneath you.", usage = "<height>", min = 1, max = 1)
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
        if (!(block.getRelative(BlockFace.UP, 1).getType().equals(Material.AIR) && block.getRelative(BlockFace.UP, 2).getType().equals(Material.AIR)))
        {
            blockCommand(context, "basics", "&cYour destination seems to be obstructed!");
        }
        loc = loc.getBlock().getLocation();
        loc.add(0.5, 1, 0.5);
        if (block.getType().equals(Material.AIR))
        {
            block.setType(Material.GLASS);
        }
        TeleportCommands.teleport(sender, loc, true, false, true); // is save anyway so we do not need to check again
        context.sendMessage("basics", "&aYou just lifted!");
    }

    @Command(desc = "Teleports you to the next safe spot upwards.", max = 0)
    public void ascend(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
        final Location userLocation = sender.getLocation();
        final Location currentLocation = userLocation.clone();
        //go upwards until hitting solid blocks
        while (currentLocation.getBlock().getType().equals(Material.AIR) && currentLocation.getBlockY() < currentLocation.getWorld().getMaxHeight())
        {
            currentLocation.add(0, 1, 0);
        }
        // go upwards until hitting 2 airblocks again
        while (!((currentLocation.getBlock().getType().equals(Material.AIR))
                && (currentLocation.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR)))
                && currentLocation.getBlockY() + 1 < currentLocation.getWorld().getMaxHeight())
        {
            currentLocation.add(0, 1, 0);
        }
        if (currentLocation.getWorld().getHighestBlockYAt(currentLocation) < currentLocation.getBlockY())
        {
            currentLocation.setY(currentLocation.getWorld().getHighestBlockYAt(currentLocation));
        }
        if (currentLocation.getY() <= userLocation.getY())
        {
            blockCommand(context, "bascics", "&cYou cannot ascend here");
        }
        //reached new location
        context.sendMessage("basics", "&aAscended a level!");
        TeleportCommands.teleport(sender, currentLocation, true, false, true);
    }

    @Command(desc = "Teleports you to the next safe spot downwards.", max = 0)
    public void descend(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eProTip: Teleport does not work IRL!");
        final Location userLocation = sender.getLocation();
        final Location currentLocation = userLocation.clone();
        //go downwards until hitting solid blocks
        while (currentLocation.getBlock().getType().equals(Material.AIR) && currentLocation.getBlockY() < currentLocation.getWorld().getMaxHeight())
        {
            currentLocation.add(0, -1, 0);
        }
        // go downwards until hitting 2 airblocks & a solid block again 
        while (!((currentLocation.getBlock().getType().equals(Material.AIR))
                && (currentLocation.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR))
                && (!currentLocation.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR)))
                && currentLocation.getBlockY() + 1 < currentLocation.getWorld().getMaxHeight())
        {
            currentLocation.add(0, -1, 0);
        }
        if ((currentLocation.getY() <= 0) || (currentLocation.getY() >= userLocation.getY()))
        {
            blockCommand(context, "bascics", "&cYou cannot descend here");
        }
        //reached new location
        context.sendMessage("basics", "&aDescended a level!");
        TeleportCommands.teleport(sender, currentLocation, true, false, true);
    }

    @Command(names = {
        "jumpto", "jump", "j"
    }, desc = "Jumps to the position you are looking at.", max = 0)
    public void jumpTo(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eJumping in the console is not allowed! Go play outside!");
        Location loc = sender.getTargetBlock(null, 350).getLocation();
        if (loc.getBlock().getType().equals(Material.AIR))
        {
            blockCommand(context, "basics", "&cNo block in sight!");
        }
        loc.add(0.5, 1, 0.5);
        TeleportCommands.teleport(sender, loc, true, false, true);
        context.sendMessage("basics", "&aYou just jumped!");
    }

    @Command(names = {
        "through", "thru"
    }, desc = "Jumps to the position you are looking at.", max = 0)
    public void through(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&ePassing through firewalls in the console is not allowed! Go play outside!");
        Location loc = LocationUtil.getBlockBehindWall(sender, 20, 30); //TODO these values in config
        if (loc == null)
        {
            sender.sendMessage("basics", "&cNothing to pass through!");
            return;
        }
        TeleportCommands.teleport(sender, loc, true, false, true);
        context.sendMessage("basics", "&aYou just passed the wall!");
    }

    @Command(desc = "Teleports you to your last location", max = 0, flags = {
        @Flag(longName = "unsafe", name = "u")
    })
    public void back(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&cYou never teleported!");
        Location loc = sender.getAttribute(basics, "lastLocation");
        if (loc == null)
        {
            blockCommand(context, "basics", "&cYou never teleported!");
        }
        boolean safe = !context.hasFlag("u");
        TeleportCommands.teleport(sender, loc, safe, true, true);
        sender.sendMessage("basics", "&aTeleported to your last location!");
    }

    @Command(names = {
        "place", "put"
    }, desc = "Jumps to the position you are looking at.", max = 1, min = 1, usage = "<player>")
    public void place(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&eJumping in the console is not allowed! Go play outside!");
        User user = context.getUser(0);
        if (user == null)
        {
            blockCommand(context, "basics", "&cUser %s not found!", context.getString(0));
        }
        Location loc = sender.getTargetBlock(null, 350).getLocation();
        if (loc.getBlock().getType().equals(Material.AIR))
        {
            blockCommand(context, "basics", "&cNo block in sight!");
        }
        loc.add(0.5, 1, 0.5);
        TeleportCommands.teleport(user, loc, true, false, true);
        context.sendMessage("basics", "&aYou just placed &2%s &awhere you were looking!", user.getName());
        user.sendMessage("basics", "&aYou were placed somewhere!");
    }
}
