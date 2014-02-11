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

import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;

import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;

public class Warp extends TeleportPoint
{
    public Warp(TeleportPointModel teleportPoint, TelePointManager telePointManager, InviteManager inviteManager, Travel module)
    {
        super(teleportPoint, telePointManager, inviteManager, module);
        if (teleportPoint.getVisibility() == VISIBILITY_PUBLIC)
        {
            this.permission = module.getBasePermission().
                childWildcard("warps").childWildcard("access").
                child(parent.getName().toLowerCase(Locale.ENGLISH), PermDefault.TRUE);
            module.getCore().getPermissionManager().registerPermission(module, this.permission);
        }
        else
        {
            this.permission = null;
        }
    }

    public void invite(User user)
    {
        super.invite(user);
        telePointManager.putWarpToUser(this, user);
    }

    public void unInvite(User user)
    {
        super.unInvite(user);
        telePointManager.removeWarpFromUser(this, user);
    }

    public String getStorageName()
    {
        if (isPublic())
        {
            return "public:" + this.getName();
        }
        else
        {
            return super.getStorageName();
        }
    }

    public boolean canAccess(User user)
    {
        return this.isPublic() ? this.permission.isAuthorized(user) : (this.isInvited(user) || this.isOwner(user));
    }
}
