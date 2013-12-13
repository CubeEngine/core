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
package de.cubeisland.engine.basics.command.teleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.LocationUtil;
import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.basics.BasicsPerm;

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
            {
                context.sendTranslated("&aYou just lifted!");
            }
            return;
        }
        context.sendTranslated("&eProTip: Teleport does not work IRL!");
    }

    @Command(desc = "Teleports to the highest point at your position.")
    public void top(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            Location loc = sender.getLocation();
            loc.getWorld().getHighestBlockAt(loc).getLocation(loc);
            if (TeleportCommands.teleport(sender, loc, true, false, true)) // is save anyway so we do not need to check again
            {
                context.sendTranslated("&aYou are now on top!");
            }
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
            currentLocation.add(0,2,0);
            //go upwards until hitting solid blocks
            while (currentLocation.getBlock().getType().equals(Material.AIR) && currentLocation.getBlockY() < currentLocation.getWorld().getMaxHeight()+1)
            {
                currentLocation.add(0, 1, 0);
            }
            // go upwards until hitting 2 airblocks again
            while (!((currentLocation.getBlock().getType().equals(Material.AIR))
                    && (currentLocation.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR)))
                    && currentLocation.getBlockY() + 1 < currentLocation.getWorld().getMaxHeight()+1)
            {
                currentLocation.add(0, 1, 0);
            }
            if (currentLocation.getY() > currentLocation.getWorld().getMaxHeight() // currentLocation is higher than the world
                && currentLocation.getWorld().getHighestBlockYAt(currentLocation) < currentLocation.getBlockY())
            {
                currentLocation.setY(currentLocation.getWorld().getHighestBlockYAt(currentLocation)); // set to highest point
            }
            if (currentLocation.getY() <= userLocation.getY()) // highest point is equal/below current location
            {
                context.sendTranslated("&cYou cannot ascend here");
                return;
            }
            //reached new location
            if (TeleportCommands.teleport(sender, currentLocation, true, false, true))
            {
                context.sendTranslated("&aAscended a level!");
            }
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
            if (TeleportCommands.teleport(sender, currentLocation, true, false, true))
            {
                context.sendTranslated("&aDescended a level!");
            }
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
            Location loc = sender.getTargetBlock(null, this.basics.getConfiguration().navigation.jumpToMaxRange).getLocation();
            if (loc.getBlock().getType().equals(Material.AIR))
            {
                context.sendTranslated("&cNo block in sight!");
                return;
            }
            loc.add(0.5, 1, 0.5);
            if (TeleportCommands.teleport(sender, loc, true, false, true))
            {
                context.sendTranslated("&aYou just jumped!");
            }
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
                    this.basics.getConfiguration().navigation.thru.maxRange,
                    this.basics.getConfiguration().navigation.thru.maxWallThickness);
            if (loc == null)
            {
                sender.sendTranslated("&cNothing to pass through!");
                return;
            }
            if (TeleportCommands.teleport(sender, loc, true, false, true))
            {
                context.sendTranslated("&aYou just passed the wall!");
            }
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
            boolean backPerm = BasicsPerm.COMMAND_BACK.isAuthorized(sender);
            boolean safe = !context.hasFlag("u");
            if (BasicsPerm.COMMAND_BACK_ONDEATH.isAuthorized(sender))
            {
                Location loc = sender.get(BasicsAttachment.class).getDeathLocation();
                if (!backPerm && loc == null)
                {
                    context.sendTranslated("&cNo death point found!");
                    return;
                }
                if (loc != null)
                {
                    if (TeleportCommands.teleport(sender, loc, safe, true, true))
                    {
                        sender.sendTranslated("&aTeleported to your death point!");
                    }
                    else
                    {
                        sender.get(BasicsAttachment.class).setDeathLocation(loc);
                    }
                    return;
                }
            }
            if (backPerm)
            {
                Location loc = sender.get(BasicsAttachment.class).getLastLocation();
                if (loc == null)
                {
                    context.sendTranslated("&cYou never teleported!");
                    return;
                }
                if (TeleportCommands.teleport(sender, loc, safe, true, true))
                {
                    sender.sendTranslated("&aTeleported to your last location!");
                }
                return;
            }
            context.sendTranslated("&cYou are not allowed to teleport back!");
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
            if (!user.isOnline())
            {
                context.sendTranslated("&cYou cannot moove an offline player!");
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
        if (!user.isOnline() || !sender.isOnline())
        {
            context.sendTranslated("&cYou cannot moove an offline player!");
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
        if (TeleportCommands.teleport(user, sender.getLocation(), true, false, false))
        {
            if (TeleportCommands.teleport(sender, userLoc, true, false, false))
            {
                if (context.hasArg(1))
                {
                    context.sendTranslated("&aSwapped position of &2%s &aand &2%s&a!", user.getName(), sender.getName());
                    return;
                }
                context.sendTranslated("&aSwapped position with &2%s&a!", user.getName());
            }
            else
            {
                TeleportCommands.teleport(user, userLoc, false, true, false);
            }
        }
        context.sendTranslated("&cCould not teleport both players!");
    }
}
