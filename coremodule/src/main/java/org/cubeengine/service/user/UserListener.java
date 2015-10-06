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
package org.cubeengine.service.user;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.Optional;
import org.cubeengine.module.core.sponge.CoreModule;
import org.cubeengine.module.core.util.Profiler;
import org.cubeengine.service.task.TaskManager;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class UserListener
{
    private final UserManager um;
    private final TaskManager tm;
    private final CoreModule core;

    public UserListener(UserManager um, TaskManager tm, CoreModule core)
    {
        this.um = um;
        this.tm = tm;
        this.core = core;
    }

    /**
     * Removes the user from loaded UserList when quitting the server and
     * updates lastseen in database
     *
     * @param event the PlayerQuitEvent
     */
    @Listener(order = Order.POST)
    public void onQuit(final ClientConnectionEvent.Disconnect event)
    {
        UUID uuid = event.getTargetEntity().getUniqueId();
        CachedUser byUUID = um.getByUUID(uuid);
        UUID taskId = tm.runTaskDelayed(core, () -> {
            um.scheduledForRemoval.remove(uuid);
            byUUID.getEntity().setValue(TableUser.TABLE_USER.LASTSEEN, new Timestamp(System.currentTimeMillis()));
            byUUID.getEntity().updateAsync();
            if (byUUID.getUser().isOnline())
            {
                um.removeCached(uuid);
            }
        }, core.getConfiguration().usermanager.keepInMemory);

        um.scheduledForRemoval.put(uuid, taskId);
    }

    @Listener(order = Order.EARLY)
    public void onJoin(final ClientConnectionEvent.Join event)
    {
        CachedUser byUUID = um.getByUUID(event.getTargetEntity().getUniqueId());
        um.updateLastName(byUUID);
        byUUID.refreshIP();
        UUID removalTask = um.scheduledForRemoval.get(event.getTargetEntity().getUniqueId());
        if (removalTask != null)
        {
            tm.cancelTask(core, removalTask);
        }
    }
}
