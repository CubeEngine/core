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
package de.cubeisland.engine.roles.role;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.bukkit.World;

import de.cubeisland.engine.core.user.UserAttachment;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.role.resolved.ResolvedMetadata;

public class RolesAttachment extends UserAttachment
{
    private Roles roles;

    private Map<World, UserDatabaseStore> dataStores = new HashMap<>();

    private World workingWorld;

    private boolean offlineMsgReceived = false;


    public UserDatabaseStore getCurrentDataHolder()
    {
        return this.getDataHolder(this.getHolder().getWorld());
    }

    public UserDatabaseStore getDataHolder(World world)
    {
        if (world == null)
        {
            return null;
        }
        UserDatabaseStore store = this.dataStores.get(world);
        if (store == null)
        {
            store = new UserDatabaseStore(this, this.roles.getRolesManager().getProvider(world), this.roles.getRolesManager(), world);
            this.dataStores.put(world, store);
        }
        if (store.isDirty())
        {
            store.calculate(new Stack<String>());
        }
        return store;
    }

    public ResolvedMetadata getCurrentMetadata(String key)
    {
        return this.getCurrentDataHolder().getMetadata().get(key);
    }

    public String getCurrentMetadataString(String key)
    {
        ResolvedMetadata meta = this.getCurrentMetadata(key);
        if (meta == null)
        {
            return null;
        }
        return meta.getValue();
    }

    public void reload()
    {
        for (UserDatabaseStore userDatabaseStore : this.dataStores.values())
        {
            userDatabaseStore.resetTempData();
            userDatabaseStore.loadFromDatabase();
        }
    }

    public WorldRoleProvider getCurrentWorldProvider()
    {
        if (this.getHolder().getWorld() == null)
        {
            return null;
        }
        return this.roles.getRolesManager().getProvider(this.getHolder().getWorld());
    }

    /**
     * Returns the role with highest priority value assigned to the holder in the current world
     *
     * @return
     */
    public Role getDominantRole()
    {
       return this.getDominantRole(this.getHolder().getWorld());
    }

    public Role getDominantRole(World world)
    {
        Role dominantRole = null;
        for (Role role : this.getDataHolder(world).getRoles())
        {
            if (dominantRole == null)
            {
                dominantRole = role;
            }
            else if (dominantRole.getPriorityValue() <= role.getPriorityValue())
            {
                dominantRole = role;
            }
        }
        return dominantRole;
    }

    public void setWorkingWorld(World world)
    {
        this.workingWorld = world;
    }

    public World getWorkingWorld()
    {
        return this.workingWorld;
    }

    @Override
    public void onAttach()
    {
        if (this.getModule() instanceof Roles)
        {
            this.roles = (Roles)this.getModule();
        }
        else
        {
            throw new IllegalArgumentException("The module has to be Roles");
        }
    }

    public void flushData()
    {
        this.dataStores = new HashMap<>();
    }

    public boolean isOfflineMsgReceived()
    {
        return offlineMsgReceived;
    }

    public void setOfflineMsgReceived(boolean offlineMsgReceived)
    {
        this.offlineMsgReceived = offlineMsgReceived;
    }
}
