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
package de.cubeisland.cubeengine.travel;

import de.cubeisland.cubeengine.core.bukkit.EventManager;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.travel.interactions.*;
import de.cubeisland.cubeengine.travel.storage.InviteManager;
import de.cubeisland.cubeengine.travel.storage.TelePointManager;

public class Travel extends Module
{
    private TelePointManager telePointManager;
    private InviteManager inviteManager;

    private TravelConfig config;

    @Override
    public void onEnable()
    {
        this.telePointManager = new TelePointManager(this);
        this.inviteManager = new InviteManager(this.getCore().getDB(), this);
        this.telePointManager.load(this.inviteManager);

        final CommandManager cm = this.getCore().getCommandManager();
        cm.registerCommand(new HomeCommand(this));
        cm.registerCommand(new HomeAdminCommand(this), "home");
        cm.registerCommand(new WarpCommand(this));

        final EventManager em = this.getCore().getEventManager();
        em.registerListener(this, new HomeListener(this));
    }

    public TravelConfig getConfig()
    {
        return this.config;
    }

    public TelePointManager getTelepointManager()
    {
        return this.telePointManager;
    }

    public InviteManager getInviteManager()
    {
        return this.inviteManager;
    }
}
