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
package de.cubeisland.engine.core.storage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.storage.Storage;
import de.cubeisland.engine.core.util.worker.AsyncTaskQueue;


/**
 * Abstract Database implementing most of the database methods.
 * Extend this class and complement it to use the database.
 */
public abstract class AbstractPooledDatabase implements Database
{
    private final ConcurrentMap<String, String> statements = new ConcurrentHashMap<String, String>();
    private final ExecutorService executorService;
    private final AsyncTaskQueue taskQueue;

    protected final Core core;

    protected AbstractPooledDatabase(Core core)
    {
        this.core = core;
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
        try (Connection connection = this.getConnection())
        {
            return this.createAndBindValues(connection, query, params).executeQuery();
        }
        catch (SQLException e)
        {
            throw new SQLException("SQL-Error while doing a query: " + query, e);
        }
    }


    @Override
    public ResultSet preparedQuery(Class owner, String name, Object... params) throws SQLException
    {
        try (Connection connection = this.getConnection())
        {
            return this.bindValues(getStoredStatement(owner, name, connection), params).executeQuery();
        }
        catch (SQLException e)
        {
            throw new SQLException("SQL-Error while doing a stored query: " + this.getRawStoredStatement(owner, name), e);
        }
    }

    @Override
    public int update(String query, Object... params) throws SQLException
    {
        try  (Connection connection = this.getConnection())
        {
            return this.createAndBindValues(connection, query, params).executeUpdate();
        }
        catch (SQLException e)
        {
            throw new SQLException("SQL-Error while doing an update-query: " + query, e);
        }
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
                    core.getLog().error("An asynchronous query failed!", e);
                }
            }
        });
    }

    @Override
    public int preparedUpdate(Class owner, String name, Object... params) throws SQLException
    {
        try (Connection connection = this.getConnection())
        {
            return this.bindValues(getStoredStatement(owner, name, connection), params).executeUpdate();
        }
        catch (SQLException e)
        {
            throw new SQLException("SQL-Error while doing a stored update-query: " + this.getRawStoredStatement(owner, name), e);
        }
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
                    core.getLog().error("An asynchronous query failed!", e);
                }
            }
        });
    }

    @Override
    public boolean execute(String query, Object... params) throws SQLException
    {
        try (Connection connection = this.getConnection())
        {
            return this.createAndBindValues(query, params, connection).execute();
        }
        catch (SQLException e)
        {
            throw new SQLException("SQL-Error while doing an execute-query: " + query, e);
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
                    core.getLog().error("An asynchronous query failed!", e);
                }
            }
        });
    }

    @Override
    public boolean preparedExecute(Class owner, String name, Object... params) throws SQLException
    {
        try (Connection connection = this.getConnection())
        {
            return this.bindValues(getStoredStatement(owner, name, connection), params).execute();
        }
        catch (SQLException e)
        {
            throw new SQLException("SQL-Error while doing a stored update-query: " + this.getRawStoredStatement(owner, name), e);
        }
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

    private PreparedStatement createAndBindValues(Connection withConnection, String query, Object[] params) throws SQLException
    {
        return this.bindValues(this.prepareStatement(query, withConnection), params);
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
        assert statement != null : "The statement must not be null!";
        return this.getConnection().prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
    }

    @Override
    public PreparedStatement prepareStatement(String statement, Connection withConnection) throws SQLException
    {
        assert statement != null : "The statement must not be null!";
        return withConnection.prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
    }

    @Override
    public PreparedStatement getStoredStatement(Class owner, String name)
    {
        assert owner != null: "The owner must not be null!";
        assert name != null: "The name must not be null!";

        String raw = this.getRawStoredStatement(owner, name);
        if (raw == null)
        {
            throw new IllegalArgumentException("Statement not found!");
        }
        try
        {
            return this.prepareStatement(this.statements.get(name));
        }
        catch (SQLException e)
        {
            CubeEngine.getLog().error("A statement could not be prepared!", e);
        }
        return null;
    }

    @Override
    public PreparedStatement getStoredStatement(Class owner, String name, Connection connection)
    {
        assert owner != null: "The owner must not be null!";
        assert name != null: "The name must not be null!";

        String raw = this.getRawStoredStatement(owner, name);
        if (raw == null)
        {
            throw new IllegalArgumentException("Statement not found!");
        }
        try
        {
            return this.prepareStatement(this.statements.get(name), connection);
        }
        catch (SQLException e)
        {
            CubeEngine.getLog().error("A statement could not be prepared!", e);
        }
        return null;
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
    public void queueOperation(Runnable operation)
    {
        this.taskQueue.addTask(operation);
    }

    @Override
    public void shutdown()
    {
        this.taskQueue.shutdown();
    }

    @Override
    public String getRawStoredStatement(Class owner, String name)
    {
        return this.statements.get(owner.getName() + "_" + name.toLowerCase(Locale.ENGLISH));
    }
}
