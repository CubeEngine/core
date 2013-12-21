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
package de.cubeisland.engine.core.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitScheduler;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CoreConfiguration;

public class CoreListener implements Listener
{
    private final BukkitCore bukkitCore;
    private final BukkitScheduler scheduler;
    private final CoreConfiguration config;

    CoreListener(Core core)
    {
        this.bukkitCore = (BukkitCore)core;
        this.scheduler = this.bukkitCore.getServer().getScheduler();
        this.config = core.getConfiguration();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        this.scheduler.scheduleSyncDelayedTask(this.bukkitCore, new Runnable()
        {
            @Override
            public void run()
            {
                AfterJoinEvent afterJoinEvent = new AfterJoinEvent(player, event.getJoinMessage());
                bukkitCore.getEventManager().fireEvent(afterJoinEvent);
            }
        }, config.usermanager.afterJoinEventDelay);
    }
}
