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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.TripletKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.WorldRoleProvider;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.THashSet;

import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;

public class AssignedRoleManager extends TripletKeyStorage<Long, Long, String, AssignedRole>
{
    private static final int REVISION = 1;

    public AssignedRoleManager(Database database)
    {
        super(database, AssignedRole.class, REVISION);
        this.initialize();
    }

    @Override
    protected void prepareStatements() throws SQLException
    {
        super.prepareStatements();
        QueryBuilder builder = this.database.getQueryBuilder();
        this.database.storeStatement(modelClass, "getallByUser",
                                     builder.select().cols("worldID", "roleName").from(this.tableName).where().field("userId").is(EQUAL).value().end().end());
        this.database.storeStatement(modelClass, "getallByUserAndWorld",
                                     builder.select().cols("roleName").from(this.tableName).
                                         where().field("userId").is(EQUAL).value().and().field("worldID").isEqual().value().end().end());

        this.database.storeStatement(modelClass, "deleteByUserAndWorld",
                                     builder.deleteFrom(this.tableName).where().
                                         field("userId").is(EQUAL).value().and().
                                                field("worldId").is(EQUAL).value().end().end());
        this.database.storeStatement(modelClass, "deleteByWorldAndRole", builder.deleteFrom(this.tableName).where().
            field("worldId").is(EQUAL).value().and().
                                                                                    field("roleName").is(EQUAL)
                                                                                .value().end().end());
        this.database.storeStatement(modelClass, "rename",
                                     builder.update(this.tableName).set("roleName").
                                         where().field("worldId").isEqual().value().
                                                and().field("roleName").isEqual().value().end().end());

        this.database.storeStatement(modelClass, "renameGlobal",
                                     builder.update(this.tableName).set("roleName").
                                         where().field("roleName").isEqual().value().end().end());

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
            throw new StorageException("Error while getting Model from Database", ex,
                                       this.database.getStoredStatement(modelClass,"getallByUser"));
        }
    }

    public void delete(long userid, String roleName, long worldId)
    {
        this.deleteByKey(new Triplet<Long, Long, String>(userid, worldId, roleName));
    }

    public void clearByUserAndWorld(long userid, long worldId)
    {
        try
        {
            this.database.preparedExecute(modelClass, "deleteByUserAndWorld", userid, worldId);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while deleting assigned Role (multi)", ex,
                                       this.database.getStoredStatement(modelClass,"deleteByUserAndWorld"));
        }
    }

    public void rename(WorldRoleProvider provider, String name, String newName)
    {
        try
        {
            PreparedStatement stm = this.database.getStoredStatement(modelClass, "rename");
            for (long worldId : provider.getWorldMirrors().keys())
            {
                if (provider.getWorldMirrors().get(worldId).getFirst())
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
            throw new StorageException("Error while renaming roles in database!", ex,
                                       this.database.getStoredStatement(modelClass,"rename"));
        }
    }

    public void renameGlobal(String name, String newName)
    {
        try
        {
            this.database.preparedExecute(modelClass,"renameGlobal","g:"+newName,"g:"+name);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while renaming global roles in database!", ex,
                                       this.database.getStoredStatement(modelClass,"renameGlobal"));
        }
    }

    public void deleteRole(long worldID, String name)
    {
        try
        {
            this.database.preparedExecute(modelClass, "deleteByWorldAndRole", worldID, name);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while deleting removed Role (multi)", ex,
                                       this.database.getStoredStatement(modelClass,"deleteByWorldAndRole"));
        }
    }

    public Set<String> getRolesByUserInWorld(long userID, long worldId)
    {
        try
        {
            ResultSet resultSet = this.database.preparedQuery(modelClass, "getallByUserAndWorld", userID, worldId);
            Set<String> result = new THashSet<String>();
            while (resultSet.next())
            {
                result.add(resultSet.getString("roleName"));
            }
            return result;
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while getting Model from Database", ex,
                                       this.database.getStoredStatement(modelClass,"getallByUserAndWorld"));
        }
    }

    public void setAssigned(Long user, long worldID, Set<Role> roles)
    {
        try
        {
            PreparedStatement stm = this.database.getStoredStatement(modelClass, "store");
            for (Role role : roles)
            {
                String roleName = role.getName();
                if (role.isGlobal())
                {
                    roleName = "g:" + roleName;
                }
                stm.setObject(1, user);
                stm.setObject(2, worldID);
                stm.setObject(3, roleName);
                stm.addBatch();
            }
            stm.executeBatch();
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while storing Models into Database", ex,
                                       this.database.getStoredStatement(modelClass,"store"));
        }
    }
}
