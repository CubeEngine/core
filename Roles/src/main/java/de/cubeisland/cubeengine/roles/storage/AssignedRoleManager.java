package de.cubeisland.cubeengine.roles.storage;

import de.cubeisland.cubeengine.core.storage.BasicStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.Database;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AssignedRoleManager extends BasicStorage<AssignedRole>
{//TODO perhaps change this to a tripletKeyStroage
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
            this.database.storeStatement(modelClass, "deleteByVals",
                    builder.delete().from(this.tableName).where().
                    field("userId").is(EQUAL).value().and().
                    field("worldId").is(EQUAL).value().and().
                    field("roleName").is(EQUAL).value().end().end());
            this.database.storeStatement(modelClass, "deleteByUserAndWorld",
                    builder.delete().from(this.tableName).where().
                    field("userId").is(EQUAL).value().and().
                    field("worldId").is(EQUAL).value().end().end());

        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the role-manager!", e);
        }
    }

    public TIntObjectHashMap<List<String>> getRolesByUser(User user)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getallByUser", user.key);
            TIntObjectHashMap<List<String>> result = new TIntObjectHashMap<List<String>>();
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

    public void delete(int userid, String name, int worldId)
    {
        try
        {
            this.database.preparedExecute(modelClass, "deleteByVals", userid, worldId, name);
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while deleting assigned Role (single)", ex);
        }
    }

    public void clear(int userid, int worldId)
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
}
