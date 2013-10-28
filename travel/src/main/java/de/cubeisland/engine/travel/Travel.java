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
package de.cubeisland.engine.travel;

import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.command.CommandManager;
<<<<<<< HEAD
import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.logging.Log;
=======
>>>>>>> refs/remotes/origin/master
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.travel.interactions.HomeAdminCommand;
import de.cubeisland.engine.travel.interactions.HomeCommand;
import de.cubeisland.engine.travel.interactions.HomeListener;
import de.cubeisland.engine.travel.interactions.WarpAdminCommand;
import de.cubeisland.engine.travel.interactions.WarpCommand;
import de.cubeisland.engine.travel.storage.InviteManager;
import de.cubeisland.engine.travel.storage.TableInvite;
import de.cubeisland.engine.travel.storage.TableTeleportPoint;
import de.cubeisland.engine.travel.storage.TelePointManager;

public class Travel extends Module
{
    private TelePointManager telePointManager;
    private InviteManager inviteManager;

    private TravelConfig config;

    @Override
    public void onEnable()
    {
        Profiler.startProfiling("travelEnable");
        this.config = this.loadConfig(TravelConfig.class);
        this.getCore().getDB().registerTable(TableTeleportPoint.initTable(this.getCore().getDB()));
        this.getCore().getDB().registerTable(TableInvite.initTable(this.getCore().getDB()));
        Log log = this.getLog();
        log.trace("{} ms - TelePointManager", Profiler.getCurrentDelta("travelEnable", TimeUnit.MILLISECONDS));
        this.telePointManager = new TelePointManager(this);
        log.trace("{} ms - InviteManager", Profiler.getCurrentDelta("travelEnable", TimeUnit.MILLISECONDS));
        this.inviteManager = new InviteManager(this.getCore().getDB(), this);
        log.trace("{} ms - InviteManager-load", Profiler.getCurrentDelta("travelEnable", TimeUnit.MILLISECONDS));
        this.telePointManager.load(this.inviteManager);
        final CommandManager cm = this.getCore().getCommandManager();
        log.trace("{} ms - register commands", Profiler.getCurrentDelta("travelEnable", TimeUnit.MILLISECONDS));
        cm.registerCommand(new HomeCommand(this));
        cm.registerCommand(new HomeAdminCommand(this), "home");
        cm.registerCommand(new WarpCommand(this));
        cm.registerCommand(new WarpAdminCommand(this), "warp");
        log.trace("{} ms - register listener", Profiler.getCurrentDelta("travelEnable", TimeUnit.MILLISECONDS));
        this.getCore().getEventManager().registerListener(this, new HomeListener(this));
        log.trace("{} ms - Done", Profiler.endProfiling("travelEnable", TimeUnit.MILLISECONDS));
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
