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
package de.cubeisland.cubeengine.log.action.logaction.block.player;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;
import static org.bukkit.Material.*;

/**
 * Breaking Item-Frames or Painting
 * <p>Events: {@link HangingBreakEvent} ({@link #logAttachedBlocks preplanned external}),
 * {@link HangingBreakByEntityEvent}</p>
 */
public class HangingBreak extends BlockActionType
{
    // TODO Hanging place Item into frame stuff!!!
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, PLAYER);
    }

    @Override
    public String getName()
    {
        return "hanging-break";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event)
    {
        if (event.getCause().equals(HangingBreakEvent.RemoveCause.PHYSICS))
        {
            if (!this.isActive(event.getEntity().getWorld())) return;
            Location location = event.getEntity().getLocation();
            Entity causer = this.plannedHangingBreak.get(location);
            if (causer != null)
            {
                if (event.getEntity() instanceof ItemFrame)
                {
                    ItemStack itemStack = ((ItemFrame) event.getEntity()).getItem();
                    String itemInFrame = itemStack == null ? null : new ItemData(itemStack).serialize(this.om);
                    this.logBlockChange(location,causer,ITEM_FRAME,AIR,itemInFrame);
                }
                else if (event.getEntity() instanceof Painting)
                {
                    this.queueLog(location,causer,PAINTING.name(),1L*((Painting)event.getEntity()).getArt().getId(),AIR.name(),(byte)0,null);
                }
            }
            else
            {
                System.out.print("Unexpected HangingBreakEvent");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event)
    {
        if (!this.isActive(event.getEntity().getWorld())) return;
        Location location = event.getEntity().getLocation();
        if (event.getRemover() instanceof Player || event.getRemover() instanceof Projectile)
        {
            Entity causer = null;
            if (event.getRemover() instanceof Projectile)
            {
                Projectile projectile = (Projectile) event.getRemover();
                if (projectile.getShooter() != null)
                {
                    causer = projectile.getShooter();
                }
            }
            else if (event.getRemover() instanceof Player)
            {
               causer = event.getRemover();
            }
            if (event.getEntity() instanceof ItemFrame)
            {
                ItemStack itemStack = ((ItemFrame) event.getEntity()).getItem();
                String itemInFrame = itemStack == null ? null : new ItemData(itemStack).serialize(this.om);
                this.logBlockChange(location,causer,ITEM_FRAME,AIR,itemInFrame);
            }
            else if (event.getEntity() instanceof Painting)
            {
                this.queueLog(location,causer,PAINTING.name(),1L*((Painting)event.getEntity()).getArt().getId(),AIR.name(),(byte)0,null);
            }
        }
        else
            System.out.print("Not a player breaking Hanging?");
    }

    private volatile boolean clearPlanned = false;
    private Map<Location,Entity> plannedHangingBreak = new ConcurrentHashMap<Location,Entity>();
    public void preplanHangingBreak(Location location, Entity player)
    {
        plannedHangingBreak.put(location, player);
        if (!clearPlanned)
        {
            clearPlanned = true;
            HangingBreak.this.logModule.getCore().getTaskManager().runTask(logModule, new Runnable()
            {
                @Override
                public void run()
                {
                    clearPlanned = false;
                    HangingBreak.this.plannedHangingBreak.clear();
                }
            });
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.getItemData() == null)
        {
            user.sendTranslated("%s&6%s&a got removed by &2%s%s",
                                time,logEntry.getOldBlock(),
                                logEntry.getCauserUser().getDisplayName(),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s &abroke an&6 item-frame &acontaining &6%s%s",
                                time,logEntry.getCauserUser().getDisplayName(),
                                logEntry.getItemData(),loc);
        }
    }



    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).HANGING_BREAK_enable;
    }
}
