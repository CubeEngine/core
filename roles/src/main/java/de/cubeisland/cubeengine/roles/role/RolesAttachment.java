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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserAttachment;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.cubeengine.roles.role.resolved.ResolvedPermission;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.THashSet;

public class RolesAttachment extends UserAttachment
{
    private TLongObjectHashMap<UserDatabaseStore> rawUserData = new TLongObjectHashMap<UserDatabaseStore>();
    // roleMirrors
    // assignedRoleMirrors
    // userMirrors

    // mirrors are NOT active for temporary Roles
    private TLongObjectHashMap<UserDataStore> temporaryData = new TLongObjectHashMap<UserDataStore>();

    private TLongObjectHashMap<ResolvedDataStore> resolvedDataStores = new TLongObjectHashMap<ResolvedDataStore>();

    private Map<String, String> currentMetaData;

    private Long workingWorldID = null;

    /**
     * Gets the resolved data-store.
     * If not yet calculated or dirty the data-store gets calculated.
     *
     * @return
     */
    protected ResolvedDataStore getResolvedData()
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
    protected ResolvedDataStore getResolvedData(long worldID)
    {
        ResolvedDataStore dataStore = resolvedDataStores.get(worldID);
        if (dataStore == null || dataStore.isDirty())
        {
            Set<Role> assignedRoles = new THashSet<Role>();
            WorldRoleProvider provider = ((Roles)this.getModule()).getRolesManager().getProvider(worldID);
            for (String roleName : this.getRawData(worldID).getRawAssignedRoles())
            {
                Role role = provider.getRole(roleName);
                if (role == null)
                {
                    System.out.print("WARNING NULL-Role! "+ roleName);
                }
                assignedRoles.add(role);
            }
            dataStore = new ResolvedDataStore(this.getRawData(worldID));
            UserDataStore tempStore = this.temporaryData.get(worldID);
            if (tempStore != null)
            {
                for (String roleName : this.temporaryData.get(worldID).getRawAssignedRoles())
                {
                    Role role = provider.getRole(roleName);
                    if (role == null)
                    {
                        System.out.print("WARNING NULL-Role! "+ roleName);
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
        this.resolvedDataStores = new TLongObjectHashMap<ResolvedDataStore>();
        this.currentMetaData = null;
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
    public RawDataStore getRawData(long worldID)
    {
        UserDatabaseStore rawDataStore = this.rawUserData.get(worldID);
        if (rawDataStore == null)
        {
            rawDataStore = new UserDatabaseStore(this,worldID,((Roles)this.getModule()).getRolesManager());
            this.rawUserData.put(worldID,rawDataStore);
        }
        return rawDataStore;
    }

    /**
     * Gets the DataStore containing only temporary data that will automatically be removed when disconnection or reloading
     *
     * @param worldID
     * @return
     */
    public RawDataStore getTemporaryRawData(long worldID)
    {
        UserDataStore rawDataStore = this.temporaryData.get(worldID);
        if (rawDataStore == null)
        {
            rawDataStore = new UserDataStore(this,worldID);
            this.temporaryData.put(worldID,rawDataStore);
        }
        return rawDataStore;
    }

    /**
     * Sets all permissions metadata and roles for the players world
     */
    public void apply()
    {
        User user = this.getHolder();
        if (user.isOnline())
        {
            if (!Bukkit.getServer().getOnlineMode() && ((Roles)this.getModule()).getConfiguration().doNotAssignPermIfOffline && !user.isLoggedIn())
            {
                user.sendTranslated("&cThe server is currently running in offline-mode. Permissions will not be applied until logging in! Contact an Administrator if you think this is an error.");
                this.getModule().getLog().warning("Role-permissions not applied! Server is running in unsecured offline-mode!");
                return;
            }
            this.getModule().getLog().log(LogLevel.DEBUG, user.getName()+ ": resolved UserData set!");
            if (this.getRawData(user.getWorldId()).getRawAssignedRoles().isEmpty())
            {
                this.getRawData(user.getWorldId()).setAssignedRoles(((Roles)this.getModule()).getRolesManager().getProvider(user.getWorldId()).getDefaultRoles());
            }
            ResolvedDataStore resolvedData = this.getResolvedData();
            user.setPermission(resolvedData.getResolvedPermissions());
            this.currentMetaData = resolvedData.getResolvedMetadata();
        }
        // else user is offline ignore
    }

    /**
     * Returns the role with highest priority value assigned to the holder in the current world
     *
     * @return
     */
    public Role getDominantRole()
    {
       return this.getDominantRole(this.getHolder().getWorldId());
    }

    /**
     * Returns the role with highest priority value assigned to the holder in the given world
     *
     * @param worldID
     * @return
     */
    public Role getDominantRole(long worldID)
    {
        Role dominantRole = null;
        for (Role role : this.getResolvedData(worldID).assignedRoles)
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

    public Set<Role> getAssignedRoles(long worldID)
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
        if (this.getHolder().getWorldId() == worldID)
        {
            this.currentMetaData = null;
        }
        this.resolvedDataStores.remove(worldID);
    }
}
