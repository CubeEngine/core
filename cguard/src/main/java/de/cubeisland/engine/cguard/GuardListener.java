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
package de.cubeisland.engine.cguard;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.cguard.storage.Guard;
import de.cubeisland.engine.cguard.storage.GuardManager;
import de.cubeisland.engine.core.user.User;

public class GuardListener implements Listener
{
    private GuardManager manager;
    private Cguard module;

    public GuardListener(Cguard module, GuardManager manager)
    {
        this.module = module;
        this.manager = manager;
        this.module.getCore().getEventManager().registerListener(module, this);
    }

    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event)
    {
        if (!(event.getPlayer() instanceof Player)) return;
        Guard guard;
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Entity)
        {
            guard = this.manager.getGuardForEntityUID(((Entity)holder).getUniqueId());
        }
        else
        {
            Location location;
            if (holder instanceof BlockState)
            {
                location = ((BlockState)holder).getLocation();
            }
            else if (holder instanceof DoubleChest)
            {
                location = ((DoubleChest)holder).getLocation();
            }
            else return;
            guard = this.manager.getGuardAtLocation(location);
        }
        if (guard != null)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            guard.handleInventoryOpen(event, user);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        Guard guard = this.manager.getGuardAtLocation(event.getBlock().getLocation());
        if (guard != null)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            guard.handleBlockBreak(event, user);
        }
    }

    // TODO placing chest next to protected chest merge OR prevent
}
