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
package de.cubeisland.cubeengine.shout.interactions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.cubeengine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;

public class ShoutListener implements Listener
{
    private Shout module;
    private AnnouncementManager am;

    public ShoutListener(Shout module)
    {
        this.module = module;
        this.am = module.getAnnouncementManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLanguageReceived(PlayerLanguageReceivedEvent event)
    {
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());

        if (this.module.getCore().isDebug())
        {
            this.module.getLog().debug("Loading user: {0}", user.getName());
        }
        this.am.initializeUser(user);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event)
    {
        this.am.clean(event.getPlayer().getName());
    }
}
