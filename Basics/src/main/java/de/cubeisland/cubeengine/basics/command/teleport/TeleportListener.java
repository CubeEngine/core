package de.cubeisland.cubeengine.basics.command.teleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.LocationUtil;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsAttachment;
import de.cubeisland.cubeengine.basics.BasicsPerm;

public class TeleportListener implements Listener
{
    private Basics basics;

    public TeleportListener(Basics basics)
    {
        this.basics = basics;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event)
    {
        User user = basics.getCore().getUserManager().getExactUser(event.getPlayer());
        switch (event.getCause())
        {
            case COMMAND:
            case PLUGIN:
            case UNKNOWN:
                user.get(BasicsAttachment.class).setLastLocation(event.getFrom());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        User user = this.basics.getCore().getUserManager().getExactUser(event.getEntity());
        if (BasicsPerm.COMMAND_BACK_ONDEATH.isAuthorized(user))
        {
            user.get(BasicsAttachment.class).setDeathLocation(user.getLocation());
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event)
    {
        if (event.getPlayer().getItemInHand().getType().equals(Material.COMPASS))
        {
            if (event.useItemInHand().equals(Event.Result.DENY) || event.useInteractedBlock().equals(Event.Result.DENY))
                return;
            event.setUseItemInHand(Event.Result.DENY);
            event.setUseInteractedBlock(Event.Result.DENY);
            switch (event.getAction())
            {
                case LEFT_CLICK_AIR:
                case LEFT_CLICK_BLOCK:
                    if (BasicsPerm.COMPASS_JUMPTO_LEFT.isAuthorized(event.getPlayer()))
                    {
                        Block block = event.getPlayer().getTargetBlock(null, this.basics.getConfiguration().jumpToMaxRange);
                        if (block.getTypeId() != 0)
                        {
                            User user = this.basics.getCore().getUserManager().getExactUser(event.getPlayer());
                            Location loc = block.getLocation().add(0.5, 1, 0.5);

                            user.safeTeleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN, true);
                            user.sendMessage("basics", "&ePoof!");
                            event.setCancelled(true);
                        }
                    }
                    return;
                case RIGHT_CLICK_AIR:
                case RIGHT_CLICK_BLOCK:
                    if (BasicsPerm.COMPASS_JUMPTO_RIGHT.isAuthorized(event.getPlayer()))
                    {
                        User user = this.basics.getCore().getUserManager().getExactUser(event.getPlayer());
                        Location loc = LocationUtil.getBlockBehindWall(user, this.basics.getConfiguration().jumpThruMaxRange,
                                this.basics.getConfiguration().jumpThruMaxWallThickness);
                        if (loc == null)
                        {
                            user.sendMessage("basics", "&cNothing to pass through!");
                            return;
                        }
                        user.safeTeleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN, true);
                        user.sendMessage("basics", "&eYou passed the wall");
                        event.setCancelled(true);
                    }
            }
        }
    }
}
