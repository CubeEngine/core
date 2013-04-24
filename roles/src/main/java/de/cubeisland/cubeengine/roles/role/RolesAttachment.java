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

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserAttachment;
import de.cubeisland.cubeengine.roles.Roles;

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

    protected ResolvedDataStore getResolvedData(long worldID)
    {
        ResolvedDataStore dataStore = resolvedDataStores.get(worldID);
        if (dataStore == null || dataStore.isDirty())
        {
            Set<Role> assignedRoles = new THashSet<Role>();
            RoleProvider provider = ((Roles)this.getModule()).getRolesManager().getProvider(worldID);
            for (String roleName : this.getRawData(worldID).getRawParents())
            {
                assignedRoles.add(provider.getRole(roleName));
            }
            dataStore = new ResolvedDataStore(this.getRawData(worldID));
            UserDataStore tempStore = this.temporaryData.get(worldID);
            if (tempStore != null)
            {
                for (String roleName : this.temporaryData.get(worldID).getRawParents())
                {
                    assignedRoles.add(provider.getRole(roleName));
                }
                dataStore.calculate(tempStore,assignedRoles);
            }
            else
            {
                dataStore.calculate(assignedRoles);
            }
            resolvedDataStores.put(worldID,dataStore);
            //TODO mirrors
        }
        return dataStore;
    }

    protected ResolvedDataStore getResolvedData()
    {
        return this.getResolvedData(this.getHolder().getWorldId());
    }

    public void flushResolvedData()
    {
        this.resolvedDataStores = new TLongObjectHashMap<ResolvedDataStore>();
    }

    public void reloadFromDatabase()
    {
        //TODO
    }

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
        if (!Bukkit.getServer().getOnlineMode() && ((Roles)this.getModule()).getConfiguration().doNotAssignPermIfOffline && !user.isLoggedIn())
        {
            user.sendTranslated("&cThe server is currently running in offline-mode. Permissions will not be applied until logging in! Contact an Administrator if you think this is an error.");
            this.getModule().getLog().warning("Role-permissions not applied! Server is running in unsecured offline-mode!");
            return;
        }
        this.getModule().getLog().log(LogLevel.DEBUG, user.getName()+ ": UserRole set!");
        if (this.getRawData(user.getWorldId()).getRawParents().isEmpty())
        {
            // TODO no roles set -> add default roles to that user!
            // null resolvedDataStore
        }
        ResolvedDataStore resolvedData = this.getResolvedData();
        user.setPermission(resolvedData.getResolvedPermissions());
        this.currentMetaData = resolvedData.getResolvedMetadata();
    }
}
