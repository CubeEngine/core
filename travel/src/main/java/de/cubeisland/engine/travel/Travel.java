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
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.travel.home.HomeCommand;
import de.cubeisland.engine.travel.home.HomeListener;
import de.cubeisland.engine.travel.home.HomeManager;
import de.cubeisland.engine.travel.warp.WarpCommand;
import de.cubeisland.engine.travel.storage.TableInvite;
import de.cubeisland.engine.travel.storage.TableTeleportPoint;
import de.cubeisland.engine.travel.warp.WarpManager;

public class Travel extends Module
{
    private TravelConfig config;

    private InviteManager inviteManager;
    private HomeManager homeManager;
    private WarpManager warpManager;

    private TravelPerm permissions;

    @Override
    public void onEnable()
    {

        this.config = this.loadConfig(TravelConfig.class);
        Database db = this.getCore().getDB();
        db.registerTable(TableTeleportPoint.class);
        db.registerTable(TableInvite.class);

        Profiler.startProfiling("travelEnable");
        this.getLog().trace("Loading TeleportPoints...");
        this.inviteManager = new InviteManager(db, this);
        this.homeManager = new HomeManager(this, this.inviteManager);
        this.homeManager.load();
        this.warpManager = new WarpManager(this, this.inviteManager);
        this.warpManager.load();
        this.getLog().trace("Loaded TeleportPoints in {} ms", Profiler.endProfiling("travelEnable",TimeUnit.MILLISECONDS));

        final CommandManager cm = this.getCore().getCommandManager();
        HomeCommand homeCmd = new HomeCommand(this);
        cm.registerCommand(homeCmd);
        WarpCommand warpCmd = new WarpCommand(this);
        cm.registerCommand(warpCmd);
        this.getCore().getEventManager().registerListener(this, new HomeListener(this));

        this.permissions = new TravelPerm(this, homeCmd, warpCmd);
    }

    public TravelConfig getConfig()
    {
        return this.config;
    }

    public HomeManager getHomeManager()
    {
        return homeManager;
    }

    public WarpManager getWarpManager()
    {
        return warpManager;
    }

    public InviteManager getInviteManager()
    {
        return this.inviteManager;
    }

    public TravelPerm getPermissions()
    {
        return permissions;
    }
}
