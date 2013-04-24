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
package de.cubeisland.cubeengine.roles.role.newRole;

import de.cubeisland.cubeengine.core.user.UserAttachment;

import de.cubeisland.cubeengine.roles.Roles;

import gnu.trove.map.hash.TLongObjectHashMap;

public class RolesAttachment extends UserAttachment
{

    private TLongObjectHashMap<UserDatabaseStore> rawUserData = new TLongObjectHashMap<UserDatabaseStore>();
    // roleMirrors
    // assignedRoleMirrors
    // userMirrors

    // mirrors are NOT active for temporary Roles
    private TLongObjectHashMap<UserDataStore> temporaryData = new TLongObjectHashMap<UserDataStore>();

    private TLongObjectHashMap<ResolvedDataStore> resolvedDataStores;

    public ResolvedDataStore getResolvedData(long worldID)
    {
        ResolvedDataStore dataStore = resolvedDataStores.get(worldID);
        if (dataStore == null)
        {
            dataStore = null; // TODO calculate
            resolvedDataStores.put(worldID,dataStore);
            //TODO mirrors
        }
        return dataStore;
    }

    public ResolvedDataStore getResolvedData()
    {
        return this.getResolvedData(this.getHolder().getWorldId());
    }

    public void flushResolvedData()
    {

    }

    public void reloadFromDatabase()
    {

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
}
