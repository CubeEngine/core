package de.cubeisland.cubeengine.log.tool;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.lookup.BlockLog;
import de.cubeisland.cubeengine.log.lookup.BlockLookup;
import de.cubeisland.cubeengine.log.storage.BlockData;
import de.cubeisland.cubeengine.log.storage.LogManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.Timestamp;

public class ToolListener implements Listener
{
    private final Log module;

    public ToolListener(Log module)
    {
        this.module = module;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() != null && event.getPlayer().getItemInHand().getTypeId() == 7)
        {
            Location loc = event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                    ? event.getClickedBlock().getLocation()
                    : event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
            BlockLookup lookup = this.module.getLogManager().getBlockLogs(
                    loc.getWorld(), loc, null,
                    new Integer[]
                    {
                            LogManager.BLOCK_BREAK, LogManager.BLOCK_CHANGE, LogManager.BLOCK_PLACE,
                            LogManager.BLOCK_SIGN, LogManager.BLOCK_CHANGE_WE, LogManager.BLOCK_GROW_BP,
                            LogManager.BLOCK_EXPLODE
                    },
                    new Long[]
                    {
                    }, false,
                    new BlockData[]
                    {
                    }, false,
                    null, false,
                    new Timestamp(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7),
                    new Timestamp(System.currentTimeMillis()));
            event.setCancelled(true);
            User user = this.module.getUserManager().getExactUser(event.getPlayer());
            if (!lookup.getEntries().isEmpty())
            {
                user.sendMessage("log", "&6%d &elogs found at &6%d:%d:%d &ein &6%s&e:", lookup.getEntries().size(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
                for (BlockLog entry : lookup.getEntries())
                {
                    entry.sendToUser(user, false);
                }
            }
            else
            {
                user.sendMessage("log", "&cNo logs found at &6%d:%d:%d &cin &6%s&c!", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
            }
        }
    }
}
