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
package de.cubeisland.engine.log.tool;

import org.bukkit.Location;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.commands.LogCommands;
import de.cubeisland.engine.log.storage.Lookup;
import de.cubeisland.engine.log.storage.ShowParameter;

public class ToolListener implements Listener
{
    private final Log module;
    private final Permission toolPerm;

    public ToolListener(Log module)
    {
        this.module = module;
        toolPerm = module.getBasePermission().child("use-logtool");
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.PHYSICAL)) return;
        if (!toolPerm.isAuthorized(event.getPlayer())) return;
        if (event.getClickedBlock() != null)
        {
            User user = this.module.getCore().getUserManager().getUser(event.getPlayer().getName());
            ItemStack item = event.getPlayer().getItemInHand();
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            {
                if (item.getItemMeta().getDisplayName().equals(LogCommands.toolName))
                {
                    LogAttachment attachment = user.attachOrGet(LogAttachment.class,this.module);
                    Lookup lookup = attachment.getLookup(item.getType());
                    if (lookup == null)
                    {
                        user.sendTranslated(MessageType.NEGATIVE, "Invalid LoggingTool-Block!");
                        return;
                    }
                    Location loc = event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                                   ? event.getClickedBlock().getLocation()
                                   : event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
                    lookup.getQueryParameter().setSingleLocations(loc);

                    ShowParameter show = new ShowParameter();
                    show.showCoords = false;
                    attachment.queueShowParameter(show);
                    this.module.getLogManager().fillLookupAndShow(lookup, user);
                    event.setCancelled(true);
                    event.setUseItemInHand(Result.DENY);
                    event.setUseInteractedBlock(Result.DENY);
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event)
    {
        ItemStack item = event.getItemDrop().getItemStack();
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() ||
            !item.getItemMeta().getDisplayName().equals(LogCommands.toolName))
        {
            return;
        }
        event.getItemDrop().remove();
    }
}
