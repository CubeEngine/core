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

public class UserPermissionsManager extends TripletKeyStorage<Long, Long, String, UserPermission>
{
    private static final int REVISION = 1;

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
            this.database.storeStatement(modelClass, "removeByUserInWorld",
                    builder.deleteFrom(tableName).where().field("userId").isEqual().value().and().field("worldId").isEqual().value().end().end());

            this.database.storeStatement(modelClass, "getallByUserInWorld",
                                         builder.select().cols("perm", "isSet").from(this.tableName)
                                        .where().field("userId").isEqual().value().and().field("worldId").isEqual().value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the role-manager!", e);
        }
    }

    public void removeByUserInWorld(Long userID, long worldID)
    {
        try
        {
            this.database.preparedExecute(modelClass, "removeByUserInWorld", userID,worldID);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while deleting from Database", ex, this.database.getStoredStatement(modelClass,"removeByUserInWorld"));
        }
    }

    public Map<String, Boolean> getPermissionsByUserInWorld(Long userID, long worldID)
    {
        try
        {
            ResultSet resultSet = this.database.preparedQuery(modelClass, "getallByUserInWorld", userID,worldID);
            THashMap<String, Boolean> result = new THashMap<String, Boolean>();
            while (resultSet.next())
            {
                result.put(resultSet.getString("perm"), resultSet.getBoolean("isSet"));
            }
            return result;
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while getting permissions from Database", ex, this.database.getStoredStatement(modelClass,"getallByUserInWorld"));
        }
    }
}
