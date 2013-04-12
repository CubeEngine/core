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

public class UserMetaDataManager extends TripletKeyStorage<Long, Long, String, UserMetaData>
{
    private static final int REVISION = 1;
    private TLongObjectHashMap<TLongObjectHashMap<THashMap<String, String>>> usermeta = new TLongObjectHashMap<TLongObjectHashMap<THashMap<String, String>>>();

    public UserMetaDataManager(Database database)
    {
        super(database, UserMetaData.class, REVISION);
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
                    builder.select().cols("worldId", "key", "value").from(this.tableName).where().field("userId").isEqual().value().end().end());
            this.database.storeStatement(modelClass, "deleteAllByUser",
                    builder.deleteFrom(this.tableName).where().field("userId").isEqual().value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the role-manager!", e);
        }
    }

    public TLongObjectHashMap<THashMap<String, String>> getForUser(long key, boolean reload)
    {
        if (reload)
        {
            try
            {
                ResultSet resulsSet = this.database.preparedQuery(modelClass, "getallByUser", key);
                TLongObjectHashMap<THashMap<String, String>> result = new TLongObjectHashMap<THashMap<String, String>>();
                while (resulsSet.next())
                {
                    int worldId = resulsSet.getInt("worldId");

                    THashMap<String, String> map = result.get(worldId);
                    if (map == null)
                    {
                        map = new THashMap<String, String>();
                        result.put(worldId, map);
                    }
                    map.put(resulsSet.getString("key"), resulsSet.getString("value"));
                }
                this.usermeta.put(key,result);
                return result;
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException("Error while getting Model from Database", ex);
            }
        }
        else
        {
            if (this.usermeta.containsKey(key))
            {
                return this.usermeta.get(key);
            }
            return this.getForUser(key, true);
        }
    }

    public void clearByUser(Long key)
    {
        try
        {
            this.database.preparedExecute(modelClass, "deleteAllByUser", key);
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while deleting Models in Database", ex);
        }
    }
}
