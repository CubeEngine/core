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
package de.cubeisland.engine.travel.storage;

import java.util.Locale;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;

import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;

public class Home extends TeleportPoint
{
    public Home(TeleportPointModel teleportPoint, TelePointManager telePointManager, InviteManager inviteManager, Travel module)
    {
        super(teleportPoint, telePointManager, inviteManager, module);
        if (teleportPoint.getVisibility() == VISIBILITY_PUBLIC)
        {
            this.permission = module.getBasePermission().
                childWildcard("publichomes").childWildcard("access").
                child(parent.getName().toLowerCase(Locale.ENGLISH), PermDefault.TRUE);
            module.getCore().getPermissionManager().registerPermission(module, this.permission);
        }
        else
        {
            this.permission = null;
        }
    }


    public void setVisibility(short visibility)
    {
        super.setVisibility(visibility);
        parent.update();
        telePointManager.removeHomeFromUser(this, this.getOwner());
        if (this.invited != null)
        {
            for (String name : this.invited)
            {
                User user = CubeEngine.getUserManager().findOnlineUser(name);
                if (user != null)
                {
                    telePointManager.removeHomeFromUser(this, user);
                }
            }
        }
        if (visibility == VISIBILITY_PUBLIC)
        {
            this.permission = module.getBasePermission().
                childWildcard("publichomes").childWildcard("access").
                child(parent.getName().toLowerCase(Locale.ENGLISH), PermDefault.TRUE);
            module.getCore().getPermissionManager().registerPermission(module, this.permission);
            this.inviteManager.removeInvites(this);
        }
        else
        {
            module.getCore().getPermissionManager().removePermission(this.module, permission);
            this.permission = null;
        }
    }

    @Override
    public void invite(User user)
    {
        super.invite(user);
        telePointManager.putHomeToUser(this, user);
    }

    @Override
    public void unInvite(User user)
    {
        super.unInvite(user);
        telePointManager.removeHomeFromUser(this, user);
    }
}
