package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.TripletKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            this.database.storeStatement(modelClass, "getallByUser",
                    builder.select().cols("worldId", "key", "value").from(this.tableName).where().field("userId").isEqual().value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the role-manager!", e);
        }
    }

    public TLongObjectHashMap<THashMap<String, String>> getForUser(long key)
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
            return result;
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while getting Model from Database", ex);
        }
    }
}
