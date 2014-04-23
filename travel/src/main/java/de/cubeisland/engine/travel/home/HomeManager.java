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
package de.cubeisland.engine.travel.home;

import org.bukkit.Location;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.InviteManager;
import de.cubeisland.engine.travel.TelePointManager;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.TeleportPointModel;

import static de.cubeisland.engine.travel.storage.TableTeleportPoint.TABLE_TP_POINT;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.*;

public class HomeManager extends TelePointManager<Home>
{
    public HomeManager(Travel module, InviteManager iManager)
    {
        super(module, iManager);
    }

    @Override
    public void load()
    {
        for (TeleportPointModel teleportPoint : this.dsl.selectFrom(TABLE_TP_POINT).where(TABLE_TP_POINT.TYPE.eq(TYPE_HOME)).fetch())
        {
            this.addPoint(new Home(teleportPoint, this.module));
        }
        module.getLog().info("{} Homes loaded", this.getCount());
    }

    @Override
    public Home create(User owner, String name, Location location, boolean publicVisibility)
    {
        if (this.has(owner, name))
        {
            throw new IllegalArgumentException("Tried to create duplicate home!");
        }
        TeleportPointModel model = this.dsl.newRecord(TABLE_TP_POINT).newTPPoint(location, name, owner, null, TYPE_HOME, publicVisibility ? VISIBILITY_PUBLIC : VISIBILITY_PRIVATE);
        Home home = new Home(model, this.module);
        model.insert();
        this.addPoint(home);
        return home;
    }
}
