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
package de.cubeisland.cubeengine.roles;

import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.provider.WorldRoleProvider;
import de.cubeisland.cubeengine.roles.role.ConfigRole;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;

import gnu.trove.map.hash.TLongObjectHashMap;

public class RolesAPI
{
    private Roles module;
    private WorldManager worldManager;
    private RoleManager manager;

    public RolesAPI(Roles module)
    {
        this.module = module;
        this.worldManager = module.getCore().getWorldManager();
        this.manager = module.getRoleManager();
    }

    public String getMetaData(Player player, World world, String metaKey)
    {
        if (player == null || world == null || metaKey == null)
        {
            return null;
        }
        User user = this.module.getCore().getUserManager().getExactUser(player);
        TLongObjectHashMap<UserSpecificRole> roleContainer = user.get(RolesAttachment.class).getRoleContainer();
        if (roleContainer == null)
        {
            if (user.isOnline())
            {
                throw new IllegalStateException("User has no rolecontainer!");
            }
            else
            {
                this.manager.preCalculateRoles(user.getName(), true);
                roleContainer = user.get(RolesAttachment.class).getRoleContainer();
            }
        }
        UserSpecificRole role = roleContainer.get(this.worldManager.getWorldId(world));
        RoleMetaData data = role.getMetaData().get(metaKey);
        if (data == null)
        {
            return null;
        }
        return data.getValue();
    }

    public ConfigRole getRole(World world, String name)
    {
        WorldRoleProvider provider = this.manager.getProvider(world);
        return provider.getRole(name);
    }

    public void recalculateDiryRoles()
    {
        this.manager.recalculateAllDirtyRoles();

    }
}
