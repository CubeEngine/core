package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;

public class MovementCommands
{
    private Basics basics;

    public MovementCommands(Basics basics)
    {
        this.basics = basics;
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
            blockCommand(context, "basics", "&cYour destination seems to be obstructed!");
        }
        loc.add(0.5, 1, 0.5);
        if (block.getType().equals(Material.AIR))
        {
            block.setType(Material.GLASS);
        }
        TeleportCommands.teleport(sender, loc, true, false); // is save anyway so we do not need to check again
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
            blockCommand(context, "bascics", "&cYou cannot ascend here");
        }
        //reached new location
        context.sendMessage("basics", "&aAscended a level!");
        TeleportCommands.teleport(sender, loc, true, false);
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
            blockCommand(context, "bascics", "&cYou cannot descend here");
        }
        //reached new location
        context.sendMessage("basics", "&aDescended a level!");
        TeleportCommands.teleport(sender, loc, true, false);
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
            blockCommand(context, "basics", "&cNo block in sight!");
        }
        loc.setYaw(sender.getLocation().getYaw());
        loc.setPitch(sender.getLocation().getPitch());
        TeleportCommands.teleport(sender, loc, true, false);
        context.sendMessage("basics", "&aYou just jumped!");
    }

    @Command(
        desc = "Teleports you to your last location",
        max = 0,
        flags = { @Flag(longName = "unsafe", name = "u") })
    public void back(CommandContext context)
    {
        User sender = context.getSenderAsUser("basics", "&cYou never teleported!");
        Location loc = sender.getAttribute(basics, "lastLocation");
        if (loc == null)
        {
            blockCommand(context, "basics", "&cYou never teleported!");
        }
        boolean safe = !context.hasFlag("u");
        TeleportCommands.teleport(sender, loc, safe, true);
        sender.sendMessage("basics", "&aTeleported to your last location!");
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
        TeleportCommands.teleport(sender, world.getSpawnLocation(), true, false);
        context.sendMessage("basics", "&aTeleported to the spawn of world &6%s", world.getName());
    }
}