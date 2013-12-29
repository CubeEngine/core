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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserAttachment;
import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.roles.RoleAppliedEvent;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.RolesConfig;
import de.cubeisland.engine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.engine.roles.role.resolved.ResolvedPermission;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.THashSet;
import org.jooq.types.UInteger;

public class RolesAttachment extends UserAttachment
{
    private Map<World, UserDatabaseStore> dataStores = new HashMap<>();

    protected RolesManager manager;
    protected RolesConfig config;

    public ResolvedDataHolder getCurrentDataHolder()
    {
        return this.getDataHolder(this.getHolder().getWorld());
    }

    public ResolvedDataHolder getDataHolder(World world)
    {
        UserDatabaseStore store = this.dataStores.get(world);
        if (store == null)
        {
            store = new UserDatabaseStore(this, this.manager.getProvider(world), manager, world);
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

    public void reload()
    {
        for (UserDatabaseStore userDatabaseStore : this.dataStores.values())
        {
            userDatabaseStore.loadFromDatabase();
        }
    }

    public WorldRoleProvider getCurrentWorldProvider()
    {
        return this.manager.getProvider(this.getHolder().getWorld());
    }

    private boolean offlineMsgReceived = false;

    /**
     * Sets all permissions metadata and roles for the players world
     */
    public void apply()
    {
        User user = this.getHolder();
        if (user.isOnline())
        {
            this.getModule().getLog().debug("Calculating Roles of Player {}...", user.getName());
            ResolvedDataHolder dataHolder = this.getDataHolder(user.getWorld());
            if (dataHolder.getRawRoles().isEmpty())
            {
                for (Role role : this.getCurrentWorldProvider().getDefaultRoles())
                {
                    dataHolder.assignTempRole(role);
                }
            }
            dataHolder.calculate(new Stack<String>());
            if (!Bukkit.getServer().getOnlineMode() && this.config.doNotAssignPermIfOffline && !user.isLoggedIn())
            {
                if (!offlineMsgReceived)
                {
                    user.sendTranslated("&cThe server is currently running in offline-mode. Permissions will not be applied until logging in! Contact an Administrator if you think this is an error.");
                    offlineMsgReceived = true;
                }
                this.getModule().getLog().warn("Role-permissions not applied! Server is running in unsecured offline-mode!");
                return;
            }
            user.setPermission(dataHolder.getResolvedPermissions());
            for (Role assignedRole : dataHolder.getRoles())
            {
                this.getModule().getLog().debug(" - {}", assignedRole.getName());
            }
        }
        this.getModule().getCore().getEventManager().fireEvent(new RoleAppliedEvent((Roles)this.getModule(), user, this));
        // else user is offline ignore
    }

    /**
     * Returns the role with highest priority value assigned to the holder in the current world
     *
     * @return
     */
    public Role_old getDominantRole()
    {
       return this.getDominantRole(this.getHolder().getWorldId());
    }

    /**
     * Returns the role with highest priority value assigned to the holder in the given world
     *
     * @param worldID
     * @return
     */
    public Role_old getDominantRole(long worldID)
    {
        Role_old dominantRole = null;
        for (Role_old role : this.getResolvedData(worldID).assignedRoles)
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

    public void setWorkingWorldId(Long workingWorldId)
    {
        this.workingWorldID = workingWorldId;
    }

    public Long getWorkingWorldId()
    {
        return workingWorldID;
    }

    public Set<Role_old> getAssignedRoles(long worldID)
    {
        return Collections.unmodifiableSet(this.getResolvedData(worldID).assignedRoles);
    }

    public Map<String,ResolvedPermission> getPermissions(long worldID)
    {
        return Collections.unmodifiableMap(this.getResolvedData(worldID).permissions);
    }

    public Map<String,ResolvedMetadata> getMetadata(long worldID)
    {
        return Collections.unmodifiableMap(this.getResolvedData(worldID).metadata);
    }

    protected void makeDirty(long worldID)
    {
        if (this.getHolder().getWorld() != null && this.getHolder().getWorldId() == worldID)
        {
            this.currentMetaData = null;
        }
        this.dataStores.remove(worldID);
    }

    @Override
    public void onAttach()
    {
        if (this.getModule() instanceof Roles)
        {
            this.manager = ((Roles)this.getModule()).getRolesManager();
            this.config = ((Roles)this.getModule()).getConfiguration();
        }
        else
        {
            throw new IllegalArgumentException("The module has to be Roles");
        }
    }
}
