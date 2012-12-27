package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.TripletKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.role.WorldRoleProvider;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AssignedRoleManager extends TripletKeyStorage<Long, Long, String, AssignedRole>
{
    private static final int REVISION = 1;

    public AssignedRoleManager(Database database)
    {
        super(database, AssignedRole.class, REVISION);
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
                    builder.select().cols("worldID", "roleName").from(this.tableName).where().field("userId").is(EQUAL).value().end().end());
            this.database.storeStatement(modelClass, "deleteByUserAndWorld",
                    builder.delete().from(this.tableName).where().
                    field("userId").is(EQUAL).value().and().
                    field("worldId").is(EQUAL).value().end().end());
            this.database.storeStatement(modelClass, "rename",
                    builder.update(this.tableName).set("roleName").
                    where().field("worldId").isEqual().value().
                    and().field("roleName").isEqual().value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the role-manager!", e);
        }
    }

    public TLongObjectHashMap<List<String>> getRolesByUser(User user)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getallByUser", user.key);
            TLongObjectHashMap<List<String>> result = new TLongObjectHashMap<List<String>>();
            while (resulsSet.next())
            {
                int worldId = resulsSet.getInt("worldId");

                List<String> list = result.get(worldId);
                if (list == null)
                {
                    list = new ArrayList<String>();
                    result.put(worldId, list);
                }
                list.add(resulsSet.getString("roleName"));
            }
            return result;
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while getting Model from Database", ex);
        }
    }

    public void delete(long userid, String name, long worldId)
    {
        this.deleteByKey(new Triplet<Long, Long, String>(userid, worldId, name));
    }

    public void clear(long userid, long worldId)
    {
        try
        {
            this.database.preparedExecute(modelClass, "deleteByUserAndWorld", userid, worldId);
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while deleting assigned Role (multi)", ex);
        }
    }

    public void rename(WorldRoleProvider provider, String name, String newName)
    {
        try
        {
            PreparedStatement stm = this.database.getStoredStatement(modelClass, "rename");
            for (long worldId : provider.getWorlds().keys())
            {
                if (provider.getWorlds().get(worldId).getLeft())
                {
                    stm.setObject(1, newName);
                    stm.setObject(2, worldId);
                    stm.setObject(3, name);
                    stm.addBatch();
                }
            }
            stm.executeBatch();
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while renaming roles in database!",ex);
        }
    }
}
