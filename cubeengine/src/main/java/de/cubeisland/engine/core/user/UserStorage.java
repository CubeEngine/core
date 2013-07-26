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
package de.cubeisland.engine.core.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.storage.SingleKeyStorage;
import de.cubeisland.engine.core.storage.StorageException;
import de.cubeisland.engine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;
import static de.cubeisland.engine.core.storage.database.querybuilder.ComponentBuilder.LESS;

public class UserStorage extends SingleKeyStorage<Long, User>
{
    private static final int REVISION = 1;
    private final Core core;
    private Set<Long> allKeys;

    UserStorage(Core core)
    {
        super(core.getDB(), User.class, REVISION);
        this.core = core;
        this.initialize();
    }


    @Override
    public void initialize()
    {
        super.initialize();
        try
        {
            this.database.storeStatement(User.class, "get_by_name", this.database.getQueryBuilder().select().wildcard().from(this.tableName).where().field("player").is(ComponentBuilder.EQUAL).value().end().end());

            this.database.storeStatement(User.class, "cleanup", database.getQueryBuilder().select(dbKey).from(tableName).where().field("lastseen").is(LESS).value().and().field("nogc").is(EQUAL).value(false).end().end());

            this.database.storeStatement(User.class, "clearpw", database.getQueryBuilder().update(tableName).set("passwd").end().end());

            this.database.storeStatement(User.class, "getAllKeys", database.getQueryBuilder().select(dbKey).from(tableName).end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the user-manager!", e);
        }
    }

    public void resetAllPasswords()
    {
        try
        {
            this.database.preparedUpdate(modelClass, "clearpw", (Object)null);
        }
        catch (SQLException ex)
        {
            throw new StorageException("An SQL-Error occurred while clearing passwords", ex);
        }
    }

    /**
     * Custom Getter for getting User from DB by Name
     *
     * @param playerName the name
     * @return the User OR null if not found
     */
    public User loadUser(String playerName)
    {
        User loadedModel = null;
        try
        {
            ResultSet resultSet = this.database.preparedQuery(modelClass, "get_by_name", playerName);
            Map<String, Object> values = new HashMap<String, Object>();
            if (resultSet.next())
            {
                for (String name : this.allFields)
                {
                    values.put(name, resultSet.getObject(name));
                }
                loadedModel = this.modelConstructor.newInstance(values);
            }
        }
        catch (SQLException e)
        {
            throw new StorageException("An SQL-Error occurred while creating a new Model from database", e,this.database.getStoredStatement(modelClass, "get_by_name"));
        }
        catch (Exception e)
        {
            throw new StorageException("An unknown error occurred while creating a new Model from database", e);
        }
        return loadedModel;
    }

    /**
     * Searches for too old UserData and remove it.
     */
    public void cleanup()
    {
        this.database.queueOperation(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Timestamp time = new Timestamp(System.currentTimeMillis() - StringUtils.convertTimeToMillis(core.getConfiguration().userManagerCleanupDatabase));
                    ResultSet result = database.preparedQuery(User.class, "cleanup", time);

                    while (result.next())
                    {
                        deleteByKey(result.getLong("key"));
                    }
                }
                catch (SQLException e)
                {
                    // TODO this exception will be uncaught
                    throw new StorageException("An SQL-Error occurred while cleaning the user-table", e, database.getStoredStatement(modelClass, "cleanup"));
                }
                catch (Exception e)
                {
                    // TODO this exception will be uncaught
                    throw new StorageException("An unknown Error occurred while cleaning the user-table", e);
                }
            }
        });
    }

    public Set<Long> getAllKeys()
    {
        try
        {
            ResultSet resultSet = database.preparedQuery(User.class,"getAllKeys");
            Set<Long> result = new HashSet<Long>();
            while (resultSet.next())
            {
                result.add(resultSet.getLong(dbKey));
            }
            return result;
        }
        catch (SQLException ex)
        {
            throw new StorageException("An SQL-Error occured while getting all user-keys!", ex, database.getStoredStatement(User.class, "gettAllKeys"));
        }
    }
}
