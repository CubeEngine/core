package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.TripletKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserPermissionsManager extends TripletKeyStorage<Integer, Integer, String, UserPermission>
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
            this.database.storeStatement(modelClass, "getallByUser",
                    builder.select().cols("worldId", "perm", "isSet").from(this.tableName).where().field("userId").isEqual().value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the role-manager!", e);
        }
    }

    public TIntObjectHashMap<THashMap<String, Boolean>> getForUser(int key)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getallByUser", key);
            TIntObjectHashMap<THashMap<String, Boolean>> result = new TIntObjectHashMap<THashMap<String, Boolean>>();
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
            return result;
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while getting Model from Database", ex);
        }
    }
}
