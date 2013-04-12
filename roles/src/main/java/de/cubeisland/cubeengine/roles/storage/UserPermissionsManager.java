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
package de.cubeisland.cubeengine.roles.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.TripletKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public class UserPermissionsManager extends TripletKeyStorage<Long, Long, String, UserPermission>
{
    private static final int REVISION = 1;
    private TLongObjectHashMap<TLongObjectHashMap<THashMap<String, Boolean>>> userperms = new TLongObjectHashMap<TLongObjectHashMap<THashMap<String, Boolean>>>();

    public UserPermissionsManager(Database database)
    {
        super(database, UserPermission.class, REVISION);
        this.initialize();
    }

    @Override
    public void initialize()
    {
        try
        {
            super.initialize();
            QueryBuilder builder = this.database.getQueryBuilder();
            this.database.storeStatement(modelClass, "getallByUser",
                    builder.select().cols("worldId", "perm", "isSet").from(this.tableName).where().field("userId").isEqual().value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the role-manager!", e);
        }
    }

    public TLongObjectHashMap<THashMap<String, Boolean>> getForUser(long key, boolean reload)
    {
        if (reload)
        {
            try
            {
                ResultSet resulsSet = this.database.preparedQuery(modelClass, "getallByUser", key);
                TLongObjectHashMap<THashMap<String, Boolean>> result = new TLongObjectHashMap<THashMap<String, Boolean>>();
                while (resulsSet.next())
                {
                    int worldId = resulsSet.getInt("worldId");

                    THashMap<String, Boolean> map = result.get(worldId);
                    if (map == null)
                    {
                        map = new THashMap<String, Boolean>();
                        result.put(worldId, map);
                    }
                    map.put(resulsSet.getString("perm"), resulsSet.getBoolean("isSet"));
                }
                this.userperms.put(key,result);
                return result;
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException("Error while getting Model from Database", ex);
            }
        }
        else
        {
            if (this.userperms.containsKey(key))
            {
                return this.userperms.get(key);
            }
            return this.getForUser(key, true);
        }
    }
}
