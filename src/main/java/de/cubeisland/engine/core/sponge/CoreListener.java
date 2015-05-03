/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 * <p>
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.sponge;

import com.google.common.eventbus.Subscribe;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CoreConfiguration;
import de.cubeisland.engine.core.task.TaskManager;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;

public class CoreListener
{
    private final Core core;
    private final TaskManager scheduler;
    private final CoreConfiguration config;

    CoreListener(Core core)
    {
        this.core = core;
        this.scheduler = this.core.getTaskManager();
        this.config = core.getConfiguration();
    }

    @Subscribe
    public void onJoin(final PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        scheduler.runTaskDelayed(core.getModuleManager().getCoreModule(), () -> {
            AfterJoinEvent afterJoinEvent = new AfterJoinEvent(player, event.getJoinMessage());
            core.getEventManager().fireEvent(afterJoinEvent);
        }, config.usermanager.afterJoinEventDelay);
    }
}
