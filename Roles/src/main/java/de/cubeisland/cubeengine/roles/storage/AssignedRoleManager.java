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
{
    private static final int REVISION = 1;

    public AssignedRoleManager(Database database)
    {
        super(database, AssignedRole.class, REVISION);
        this.initialize();
    }

    @Override
    protected void initialize()
    {
        try
        {
            super.initialize();
            QueryBuilder builder = this.database.getQueryBuilder();
            this.database.storeStatement(modelClass, "getallByUser",
                    builder.select().wildcard().from(this.table).where().field("userId").is(EQUAL).value().end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the role-manager!", e);
        }
    }

    public TIntObjectHashMap<List<String>> getRolesByUser(User user)
    {
        List<AssignedRole> loadedModels = new ArrayList<AssignedRole>();
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getallByUser", user.key);
            // TODO perhaps do this better without creating the models
            while (resulsSet.next())
            {
                ArrayList<Object> values = new ArrayList<Object>();
                values.add(resulsSet.getObject(this.dbKey));
                for (String name : this.dbAttributes)
                {
                    values.add(resulsSet.getObject(name));
                }
                AssignedRole loadedModel = this.modelConstructor.newInstance(values);
                loadedModels.add(loadedModel);
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while getting Model from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
        TIntObjectHashMap<List<String>> result = new TIntObjectHashMap<List<String>>();
        for (AssignedRole role : loadedModels)
        {
            List<String> list = result.get(role.worldId);
            if (list == null)
            {
                list = new ArrayList<String>();
                result.put(role.worldId, list);
            }
            list.add(role.roleName);
        }
        return result;
    }
}
