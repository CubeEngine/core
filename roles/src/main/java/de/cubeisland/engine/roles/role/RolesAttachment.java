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
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;


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
    private TLongObjectHashMap<UserDatabaseStore> rawUserData = new TLongObjectHashMap<>();
    // roleMirrors
    // assignedRoleMirrors
    // userMirrors

    // mirrors are NOT active for temporary Roles
    private TLongObjectHashMap<UserDataStore> temporaryData = new TLongObjectHashMap<>();

    private TLongObjectHashMap<ResolvedData_old> resolvedDataStores = new TLongObjectHashMap<>();

    private Map<String, String> currentMetaData;

    private Long workingWorldID = null;
    protected RolesManager manager;
    protected RolesConfig config;

    /**
     * Gets the resolved data-store.
     * If not yet calculated or dirty the data-store gets calculated.
     *
     * @return
     */
    protected ResolvedData_old getCurrentResolvedData()
    {
        return this.getResolvedData(this.getHolder().getWorldId());
    }

    /**
     * Gets the resolved data-store.
     * If not yet calculated or dirty the data-store gets calculated.
     *
     * @param worldID
     * @return
     */
    protected ResolvedData_old getResolvedData(long worldID)
    {
        ResolvedData_old dataStore = resolvedDataStores.get(worldID);
        if (dataStore == null || dataStore.isDirty())
        {
            Set<Role_old> assignedRoles = new THashSet<>();
            WorldRoleProvider provider = ((Roles)this.getModule()).getRolesManager().getProvider(worldID);
            for (String roleName : this.getRawData(worldID).getRawRoles())
            {
                Role_old role = provider.getRole(roleName);
                if (role == null)
                {
                    this.getModule().getLog().warn("The role {} is not available in {}", roleName, provider.getMainWorld());
                    this.getHolder().sendTranslated("&cYour role &6%s&c is not available in &6%s", roleName, provider.getMainWorld());
                    this.getHolder().sendTranslated("&4You should report this to an administrator!");
                    continue;
                }
                assignedRoles.add(role);
            }
            dataStore = new ResolvedData_old(this.getRawData(worldID));
            UserDataStore tempStore = this.temporaryData.get(worldID);
            if (tempStore != null)
            {
                for (String roleName : this.temporaryData.get(worldID).getRawRoles())
                {
                    Role_old role = provider.getRole(roleName);
                    if (role == null)
                    {
                        this.getModule().getLog().warn("The temporary role {} is not available in {}", roleName, provider.getMainWorld());
                        this.getHolder().sendTranslated("&cYour temporary role &6%s&c is not available in &6%s", roleName, provider.getMainWorld());
                        this.getHolder().sendTranslated("&4You should report this to an administrator!");
                        continue;
                    }
                    assignedRoles.add(role);
                }
                dataStore.calculate(tempStore,assignedRoles);
            }
            else
            {
                dataStore.calculate(assignedRoles);
            }
            resolvedDataStores.put(worldID,dataStore);
            TLongObjectHashMap<Triplet<Boolean, Boolean, Boolean>> worldMirrors = provider.getWorldMirrors();
            for (long world : worldMirrors.keys()) // Apply to fully mirrored too!
            {
                if (worldMirrors.get(world).getSecond() && worldMirrors.get(world).getThird())
                {
                    resolvedDataStores.put(world,dataStore);
                }
            }
        }
        return dataStore;
    }

    /**
     * Gets the currently assigned metadatavalue for given key
     *
     * @param key
     * @return
     */
    public String getCurrentMetadata(String key)
    {
        if (currentMetaData == null)
        {
            if (this.getHolder().isOnline())
            {
                this.apply();
            }
            else
            {
                return null;
            }
        }
        return currentMetaData.get(key);
    }

    /**
     * Removes all currently calculated data forcing to recalculate the resolved data stores when needed.
     */
    public void flushResolvedData()
    {
        this.resolvedDataStores = new TLongObjectHashMap<>();
        this.currentMetaData = null;
    }

    /**
     * Removes all currently set temporary data invalidates all current data and reloads from database
     */
    public void reload()
    {
        this.temporaryData = new TLongObjectHashMap<>();
        this.flushResolvedData();
        this.reloadFromDatabase();
    }

    public void reloadFromDatabase()
    {
        for (UserDatabaseStore userDatabaseStore : this.rawUserData.valueCollection())
        {
            userDatabaseStore.loadFromDatabase();
        }
        this.resolvedDataStores.clear(); // Assume they are all dirty
    }

    /**
     * Gets the DataStore containing the data saved in database for the holder in given world.
     *
     * @param worldID
     * @return
     */
    public DataStore getRawData(long worldID)
    {
        UserDatabaseStore rawDataStore = this.rawUserData.get(worldID);
        if (rawDataStore == null)
        {
            rawDataStore = new UserDatabaseStore(this, worldID, manager);
            this.rawUserData.put(worldID, rawDataStore);
        }
        return rawDataStore;
    }

    /**
     * Gets the DataStore containing only temporary data that will automatically be removed when disconnection or reloading
     *
     * @param worldID
     * @return
     */
    public DataStore getTemporaryRawData(long worldID)
    {
        UserDataStore rawDataStore = this.temporaryData.get(worldID);
        if (rawDataStore == null)
        {
            rawDataStore = new UserDataStore(this, worldID, UInteger.valueOf(this.manager.assignedRolesMirrors.get(worldID)));
            this.temporaryData.put(worldID,rawDataStore);
        }
        return rawDataStore;
    }

    public WorldRoleProvider getCurrentWorldProvider()
    {
        return this.manager.getProvider(this.getHolder().getWorldId());
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
            if (this.getRawData(user.getWorldId()).getRawRoles().isEmpty())
            {
                Set<Role_old> defaultRoles = this.getCurrentWorldProvider().getDefaultRoles();
                this.getTemporaryRawData(user.getWorldId()).setRawRoles(defaultRoles);
            }
            ResolvedData_old resolvedData = this.getCurrentResolvedData();
            this.currentMetaData = resolvedData.getResolvedMetadata();
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
            user.setPermission(resolvedData.getResolvedPermissions());
            this.getModule().getLog().debug("Calculating Roles of Player {}...", user.getName());
            for (Role_old assignedRole : resolvedData.assignedRoles)
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
        this.resolvedDataStores.remove(worldID);
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
