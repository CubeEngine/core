package de.cubeisland.cubeengine.log.tool;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAttachment;
import de.cubeisland.cubeengine.log.commands.LogCommands;
import de.cubeisland.cubeengine.log.storage.Lookup;

public class ToolListener implements Listener
{
    private final Log module;

    public ToolListener(Log module)
    {
        this.module = module;
    }

    //TODO when dropping a logging tool destroy it

    @EventHandler
    public void onClick(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() != null)
        {
            ItemStack item = event.getPlayer().getItemInHand();
            if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() ||
                    !item.getItemMeta().getDisplayName().equals(LogCommands.toolName))
            {
                return;
            }
            User user = this.module.getCore().getUserManager().getUser(event.getPlayer());
            Lookup lookup = user.attachOrGet(LogAttachment.class,this.module).getLookup(item.getType());
            if (lookup == null)
            {
                user.sendTranslated("&cInvalid LoggingTool-Block!");
                return;
            }
            Location loc = event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                    ? event.getClickedBlock().getLocation()
                    : event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
            lookup.setLocation(loc);
            //-----------
            lookup.since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)); // 7 days default //TODO this in block creation
            //-----------
            this.module.getLogManager().fillLookup(lookup);
            lookup.show(user);
            event.getPlayer().sendMessage("Used LoggingTool-Block but not fully implemented yet :/");
            event.setCancelled(true);
            event.setUseItemInHand(Result.DENY);
            event.setUseInteractedBlock(Result.DENY);
            /*

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
            */
        }
    }
}
