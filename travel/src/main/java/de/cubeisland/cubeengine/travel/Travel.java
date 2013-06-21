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

import java.util.concurrent.TimeUnit;

import de.cubeisland.cubeengine.core.bukkit.EventManager;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.Profiler;
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
        Profiler.startProfiling("travelEnable");
        this.config = Configuration.load(TravelConfig.class, this);
        this.getLog().trace("{} ms - TelePointManager", Profiler.getCurrentDelta("travelEnable", TimeUnit.MILLISECONDS));
        this.telePointManager = new TelePointManager(this);
        this.getLog().trace("{} ms - InviteManager", Profiler.getCurrentDelta("travelEnable", TimeUnit.MILLISECONDS));
        this.inviteManager = new InviteManager(this.getCore().getDB(), this);
        this.getLog().trace("{} ms - InviteManager-load", Profiler.getCurrentDelta("travelEnable", TimeUnit.MILLISECONDS));
        this.telePointManager.load(this.inviteManager);

        final CommandManager cm = this.getCore().getCommandManager();
        this.getLog().trace("{} ms - register commands", Profiler.getCurrentDelta("travelEnable", TimeUnit.MILLISECONDS));
        cm.registerCommand(new HomeCommand(this));
        cm.registerCommand(new HomeAdminCommand(this), "home");
        cm.registerCommand(new WarpCommand(this));
        cm.registerCommand(new WarpAdminCommand(this), "warp");
        this.getLog().trace("{} ms - register listener", Profiler.getCurrentDelta("travelEnable", TimeUnit.MILLISECONDS));
        final EventManager em = this.getCore().getEventManager();
        em.registerListener(this, new HomeListener(this));
        this.getLog().trace("{} ms - Done", Profiler.endProfiling("travelEnable", TimeUnit.MILLISECONDS));
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
