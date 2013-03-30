package de.cubeisland.cubeengine.basics.command.teleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.LocationUtil;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsAttachment;
import de.cubeisland.cubeengine.basics.BasicsPerm;

/**
 * Contains commands for fast movement. /up /ascend /descend /jumpto /through
 * /thru /back /place /put /swap
 */
public class MovementCommands
{

    private Basics basics;

    public MovementCommands(Basics basics)
    {
        this.basics = basics;
    }

    @Command(desc = "Teleports you x-amount of blocks into the air and puts a glassblock beneath you.", usage = "<height>", min = 1, max = 1)
    public void up(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            int height = context.getArg(0, Integer.class, -1);
            if ((height < 0))
            {
                context.sendTranslated("&cInvalid height. The height has to be a number greater than 0!");
                return;
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
                context.sendTranslated("&cYour destination seems to be obstructed!");
                return;
            }
            loc = loc.getBlock().getLocation();
            loc.add(0.5, 1, 0.5);
            if (block.getType().equals(Material.AIR))
            {
                block.setType(Material.GLASS);
            }
            if (TeleportCommands.teleport(sender, loc, true, false, true)) // is save anyway so we do not need to check again
                context.sendTranslated("&aYou just lifted!");
            return;
        }
        context.sendTranslated("&eProTip: Teleport does not work IRL!");
    }

    @Command(desc = "Teleports you to the next safe spot upwards.", max = 0)
    public void ascend(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
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
                context.sendTranslated("&cYou cannot ascend here"); // TODO check why this comes sometimes but shouldn't
                return;
            }
            //reached new location
            context.sendTranslated("&aAscended a level!");
            TeleportCommands.teleport(sender, currentLocation, true, false, true);
            return;
        }
        context.sendTranslated("&eProTip: Teleport does not work IRL!");
    }

    @Command(desc = "Teleports you to the next safe spot downwards.", max = 0)
    public void descend(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
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
                context.sendTranslated("&cYou cannot descend here");
                return;
            }
            //reached new location
            context.sendTranslated("&aDescended a level!");
            TeleportCommands.teleport(sender, currentLocation, true, false, true);
            return;
        }
        context.sendTranslated("&eProTip: Teleport does not work IRL!");
    }

    @Command(names = {
        "jumpto", "jump", "j"
    }, desc = "Jumps to the position you are looking at.", max = 0)
    public void jumpTo(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Location loc = sender.getTargetBlock(null, this.basics.getConfiguration().jumpToMaxRange).getLocation();
            if (loc.getBlock().getType().equals(Material.AIR))
            {
                context.sendTranslated("&cNo block in sight!");
                return;
            }
            loc.add(0.5, 1, 0.5);
            if (TeleportCommands.teleport(sender, loc, true, false, true))
                context.sendTranslated("&aYou just jumped!");
            return;
        }
        context.sendTranslated("&eJumping in the console is not allowed! Go play outside!");
    }

    @Command(names = {
        "through", "thru"
    }, desc = "Jumps to the position you are looking at.", max = 0)
    public void through(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Location loc = LocationUtil.getBlockBehindWall(sender,
                    this.basics.getConfiguration().jumpThruMaxRange,
                    this.basics.getConfiguration().jumpThruMaxWallThickness);
            if (loc == null)
            {
                sender.sendTranslated("&cNothing to pass through!");
                return;
            }
            if (TeleportCommands.teleport(sender, loc, true, false, true))
                context.sendTranslated("&aYou just passed the wall!");
            return;
        }
        context.sendTranslated("&ePassing through firewalls in the console is not allowed! Go play outside!");
    }

    @Command(desc = "Teleports you to your last location", max = 0, flags =
        @Flag(longName = "unsafe", name = "u") , checkPerm = false
    )
    public void back(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Location loc;
            boolean backPerm = BasicsPerm.COMMAND_BACK.isAuthorized(sender);
            if (BasicsPerm.COMMAND_BACK_ONDEATH.isAuthorized(sender))
            {
                loc = sender.get(BasicsAttachment.class).getDeathLocation();
                if (!backPerm && loc == null)
                {
                    context.sendTranslated("&cNo death point found!");
                    return;
                }
            }
            if (BasicsPerm.COMMAND_BACK.isAuthorized(sender))
            {
                loc = sender.get(BasicsAttachment.class).getLastLocation();
                if (loc == null)
                {
                    context.sendTranslated("&cYou never teleported!");
                    return;
                }
            }
            else
            {
                context.sendTranslated("&cYou are not allowed to teleport back!");
                return;
            }
            boolean safe = !context.hasFlag("u");
            if (TeleportCommands.teleport(sender, loc, safe, true, true))
                sender.sendTranslated("&aTeleported to your last location!");
            return;
        }
        context.sendTranslated("&cUnfortunatly teleporting is still not implemented in the game &6'Life'&c!");
    }

    @Command(names = {
        "place", "put"
    }, desc = "Jumps to the position you are looking at.", max = 1, min = 1, usage = "<player>")
    public void place(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            User user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
                return;
            }
            Location loc = sender.getTargetBlock(null, 350).getLocation();
            if (loc.getBlock().getType().equals(Material.AIR))
            {
                context.sendTranslated("&cNo block in sight!");
                return;
            }
            loc.add(0.5, 1, 0.5);
            if (TeleportCommands.teleport(user, loc, true, false, true))
            {
                context.sendTranslated("&aYou just placed &2%s &awhere you were looking!", user.getName());
                user.sendTranslated("&aYou were placed somewhere!");
            }
            return;
        }
        context.sendTranslated("&eJumping in the console is not allowed! Go play outside!");

    }

    @Command(desc = "Swaps your and another players position", min = 1, max = 2, usage = "<player> [player]")
    public void swap(CommandContext context)
    {
        User sender;
        if (context.hasArg(1))
        {
            sender = context.getUser(1);
            if (sender == null)
            {
                context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
                return;
            }
        }
        else
        {
            sender = null;
            if (context.getSender() instanceof User)
            {
                sender = (User)context.getSender();
            }
            if (sender == null)
            {
                context.sendTranslated("&cSuccesfully swapped your socks!\n"
                                           + "&eAs console you have to provide both players!");
                return;
            }
        }
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
            return;
        }
        if (user == sender)
        {
            if (context.getSender() instanceof Player)
            {
                context.sendTranslated("&aSwapped position with &cyourself!? &eAre you kidding me?");
                return;
            }
            context.sendTranslated("&aTruely a hero! &eTrying to swap a users position with himself...");
            return;
        }
        Location userLoc = user.getLocation();
        TeleportCommands.teleport(user, sender.getLocation(), true, false, false);
        TeleportCommands.teleport(sender, userLoc, true, false, false);
        //TODO handle if tp fails
        if (context.hasArg(1))
        {
            context.sendTranslated("&aSwapped position of &2%s &aand &2%s&a!", user.getName(), sender.getName());
            return;
        }
        context.sendTranslated("&aSwapped position with &2%s&a!", user.getName());
    }
}
