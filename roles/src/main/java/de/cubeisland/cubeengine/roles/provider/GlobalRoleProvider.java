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
package de.cubeisland.cubeengine.roles.provider;

import java.io.File;
import java.util.Locale;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;

import gnu.trove.map.hash.TLongObjectHashMap;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;

public class GlobalRoleProvider extends RoleProvider
{
    public GlobalRoleProvider(Roles module)
    {
        super(module, true, module.getBasePermission().createAbstractChild("global"));
    }

    @Override
    public void reapplyDirtyRoles()
    {
        for (User user : this.module.getCore().getUserManager().getOnlineUsers())
        {
            boolean isDirty = false;
            TLongObjectHashMap<UserSpecificRole> roleContainer = this.manager.getRoleContainer(user);
            Long userWorld = user.isOnline() ? user.getWorldId() : null;
            for (long worldId : roleContainer.keys())
            {
                UserSpecificRole userRole = roleContainer.get(worldId);
                for (Role role : userRole.getParentRoles())
                {
                    if (role.isDirty())
                    {
                        isDirty = true; // found a dirty role recalculate!
                        break;
                    }
                }
                if (isDirty)
                {
                    UserSpecificRole newRole = this.manager.recalculateDirtyUserRole(user,worldId);
                    roleContainer.put(worldId,newRole);
                    if (userWorld == worldId) // if user is in that world
                    {
                        this.module.getRoleManager().applyRole(user.getPlayer()); // reapply freshly generated userrole
                    }
                }
                isDirty = false; // check next world
            }
        }
    }

}
