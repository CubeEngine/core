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
package de.cubeisland.cubeengine.core.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.Storage;
import de.cubeisland.cubeengine.core.util.worker.AsyncTaskQueue;



/**
 * Abstract Database implementing most of the database methods.
 * Extend this class and complement it to use the database.
 */
public abstract class AbstractDatabase implements Database
{
    private final ConcurrentMap<String, String> statements = new ConcurrentHashMap<String, String>();
    private final ConcurrentMap<String, PreparedStatement> preparedStatements = new ConcurrentHashMap<String, PreparedStatement>();
    private final ExecutorService executorService;
    private final AsyncTaskQueue taskQueue;

    protected AbstractDatabase()
    {
        this.executorService = Executors.newSingleThreadExecutor(CubeEngine.getCore().getTaskManager().getThreadFactory());
        this.taskQueue = new AsyncTaskQueue(this.executorService);
    }

    @Override
    public Object getLastInsertedId(Class owner, String name, Object... params) throws SQLException
    {
        PreparedStatement statement = this.bindValues(this.getStoredStatement(owner, name), params);
        statement.execute();
        final ResultSet result = statement.getGeneratedKeys();
        if (result.next())
        {
            return result.getObject("GENERATED_KEY");
        }
        throw new SQLException("Failed to retrieve the last inserted ID!");
    }

    @Override
    public ResultSet query(String query, Object... params) throws SQLException
    {
        this.getConnection();
        return this.createAndBindValues(query, params).executeQuery();
    }

    @Override
    public ResultSet preparedQuery(Class owner, String name, Object... params) throws SQLException
    {
        this.getConnection();
        return this.bindValues(getStoredStatement(owner, name), params).executeQuery();
    }

    @Override
    public int update(String query, Object... params) throws SQLException
    {
        return this.createAndBindValues(query, params).executeUpdate();
    }

    @Override
    public void asyncUpdate(final String query, final Object... params)
    {
        this.taskQueue.addTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    update(query, params);
                }
                catch (SQLException e)
                {
                    CubeEngine.getLog().error("An asynchronous query failed!", e);
                }
            }
        });
    }

    @Override
    public int preparedUpdate(Class owner, String name, Object... params) throws SQLException
    {
        this.getConnection();
        return this.bindValues(getStoredStatement(owner, name), params).executeUpdate();
    }

    @Override
    public void asnycPreparedUpdate(final Class owner, final String name, final Object... params)
    {
        this.taskQueue.addTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    preparedUpdate(owner, name, params);
                }
                catch (SQLException e)
                {
                    CubeEngine.getLog().error("An asynchronous query failed!", e);
                }
            }
        });
    }

    @Override
    public boolean execute(String query, Object... params) throws SQLException
    {
        try
        {
            return this.createAndBindValues(query, params).execute();
        }
        catch (SQLException e)
        {
            throw new SQLException("SQL-Error while executing: " + query, e);
        }
    }

    @Override
    public void asyncExecute(final String query, final Object... params)
    {
        this.taskQueue.addTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    execute(query, params);
                }
                catch (SQLException e)
                {
                    CubeEngine.getLog().error("An asynchronous query failed!", e);
                }
            }
        });
    }

    @Override
    public boolean preparedExecute(Class owner, String name, Object... params) throws SQLException
    {
        this.getConnection();
        return this.bindValues(getStoredStatement(owner, name), params).execute();
    }

    @Override
    public void asyncPreparedExecute(final Class owner, final String name, final Object... params)
    {

        this.taskQueue.addTask(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    preparedExecute(owner, name, params);
                }
                catch (SQLException e)
                {
                    CubeEngine.getLog().error("An asynchronous query failed!", e);
                }
            }
        });
    }

    protected PreparedStatement createAndBindValues(String query, Object... params) throws SQLException
    {
        return this.bindValues(this.prepareStatement(query), params);
    }

    protected PreparedStatement bindValues(PreparedStatement statement, Object... params) throws SQLException
    {
        for (int i = 0; i < params.length; ++i)
        {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    @Override
    public void storeStatement(Class owner, String name, String statement) throws SQLException
    {
        assert owner != null: "The owner must not be null!";
        assert name != null: "The name must not be null!";
        assert statement != null: "The statement must not be null!";

        this.statements.put(owner.getName() + "_" + name.toLowerCase(Locale.ENGLISH), statement);
    }

    @Override
    public PreparedStatement prepareStatement(String statement) throws SQLException
    {
        if (statement == null)
        {
            return null;
        }
        return this.getConnection().prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
    }

    @Override
    public PreparedStatement getStoredStatement(Class owner, String name)
    {
        assert owner != null: "The owner must not be null!";
        assert name != null: "The name must not be null!";

        name = owner.getName() + "_" + name.toLowerCase(Locale.ENGLISH);
        PreparedStatement statement = this.preparedStatements.get(name);
        if (statement == null)
        {
            String raw = this.statements.get(name);
            if (raw != null)
            {
                try
                {
                    statement = this.prepareStatement(raw);
                    if (statement != null)
                    {
                        this.preparedStatements.put(name, statement);
                    }
                }
                catch (SQLException e)
                {
                    CubeEngine.getLog().error("A statement could not be prepared!", e);
                }
            }
        }
        if (statement == null)
        {
            throw new IllegalArgumentException("Statement not found!");
        }
        return statement;
    }

    @Override
    public void startTransaction() throws SQLException
    {
        this.getConnection().setAutoCommit(false);
    }

    @Override
    public void commit() throws SQLException
    {
        this.getConnection().commit();
        this.getConnection().setAutoCommit(true);
    }

    @Override
    public void rollback() throws SQLException
    {
        this.getConnection().rollback();
    }

    @Override
    public void updateStructure(Storage manager)
    {
        manager.updateStructure();
    }

    @Override
    public void clearStatementCache()
    {
        this.preparedStatements.clear();
    }

    @Override
    public void queueOperation(Runnable operation)
    {
        this.taskQueue.addTask(operation);
    }

    @Override
    public void shutdown()
    {
        this.taskQueue.shutdown();
    }
}
