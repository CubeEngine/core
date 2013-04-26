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
import java.util.Map;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.TripletKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public class UserMetaDataManager extends TripletKeyStorage<Long, Long, String, UserMetaData>
{
    private static final int REVISION = 1;

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
            this.database.storeStatement(modelClass, "getallByUserInWorld",
                    builder.select().cols("key", "value").from(this.tableName).
                        where().field("userId").isEqual().value().and().field("worldId").isEqual().value().end().end());
            this.database.storeStatement(modelClass, "deleteAllByUserInWorld",
                                         builder.deleteFrom(this.tableName).where().field("userId").isEqual().value().
                                             and().field("worldId").isEqual().value().end().end());
            this.database.storeStatement(modelClass, "deleteAllByUser",
                    builder.deleteFrom(this.tableName).where().field("userId").isEqual().value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the role-manager!", e);
        }
    }

    public void clearByUserInWorld(Long userID, Long worldID)
    {
        try
        {
            this.database.preparedExecute(modelClass, "deleteAllByUserInWorld", userID, worldID);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while deleting Models in Database", ex,
                                       this.database.getStoredStatement(modelClass,"deleteAllByUserInWorld"));
        }
    }

    public void clearByUser(long userID)
    {
        try
        {
            this.database.preparedExecute(modelClass, "deleteAllByUser", userID);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while deleting Models in Database", ex,
                                       this.database.getStoredStatement(modelClass,"deleteAllByUser"));
        }
    }

    public Map<String, String> getMetadataByUserInWorld(Long userID, long worldID)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getallByUserInWorld", userID, worldID);
            THashMap<String, String> result = new THashMap<String, String>();
            while (resulsSet.next())
            {
                result.put(resulsSet.getString("key"), resulsSet.getString("value"));
            }
            return result;
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while getting Model from Database", ex,
                           this.database.getStoredStatement(modelClass,"getallByUserInWorld"));
        }
    }
}
