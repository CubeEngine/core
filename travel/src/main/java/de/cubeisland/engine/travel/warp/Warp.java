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
package de.cubeisland.engine.travel.warp;

import java.util.Locale;

import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.travel.TeleportPoint;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.TeleportPointModel;

import static de.cubeisland.engine.travel.storage.TeleportPointModel.VISIBILITY_PUBLIC;

public class Warp extends TeleportPoint
{
    public Warp(TeleportPointModel teleportPoint, Travel module)
    {
        super(teleportPoint, module);
        if (teleportPoint.getVisibility() == VISIBILITY_PUBLIC)
        {
            this.permission = generatePublicPerm();
            return;
        }
        this.permission = null;
    }

    public void setVisibility(short visibility)
    {
        super.setVisibility(visibility);
        model.update();
        if (visibility == VISIBILITY_PUBLIC)
        {
            this.permission = generatePublicPerm();
            this.iManager.removeInvites(this);
            return;
        }
        module.getCore().getPermissionManager().removePermission(this.module, permission);
        this.permission = null;
    }

    @Override
    protected Permission generatePublicPerm()
    {
        Permission perm =  module.getBasePermission().childWildcard("warps").childWildcard("access").child(
            model.getName().toLowerCase(Locale.ENGLISH), PermDefault.TRUE);
        module.getCore().getPermissionManager().registerPermission(module, perm);
        return perm;
    }
}
