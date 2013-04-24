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
package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.storage.UserMetaData;
import de.cubeisland.cubeengine.roles.storage.UserMetaDataManager;
import de.cubeisland.cubeengine.roles.storage.UserPermission;
import de.cubeisland.cubeengine.roles.storage.UserPermissionsManager;
import gnu.trove.map.hash.THashMap;

public class UserSpecificRole extends MergedRole
{
    public final User user;
    private Roles module;
    private UserMetaDataManager umManager;
    private UserPermissionsManager upManager;
    private long worldId;

    private UserSpecificRole(Roles module, User user, long worldId, THashMap<String, Boolean> perms, THashMap<String, String> meta)
    {
        super(user.getName(), perms, meta);
        this.user = user;
        this.module = module;
        this.worldId = worldId;
        this.umManager = module.getDbUserMeta();
        this.upManager = module.getDbUserPerm();
    }

    public UserSpecificRole(Roles module, User user, long worldId)
    {
        this(module,user,worldId, module.getDbUserPerm().getForUser(user.key,false).get(worldId), module.getDbUserMeta().getForUser(user.key,false).get(worldId));
    }


    public ConfigRole getDominantRole()
    {
        ConfigRole role = null;
        for (ConfigRole cf : getParentRoles())
        {
            if (role == null)
            {
                role = cf;
            }
            else if (role.getPriority().value >= cf.getPriority().value)
            {
                role = cf;
            }
        }
        return role;
    }
}
