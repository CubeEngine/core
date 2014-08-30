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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.task.worker.AsyncTaskQueue;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

/**
 * Abstract Database implementing most of the database methods.
 * Extend this class and complement it to use the database.
 */
public abstract class AbstractPooledDatabase implements Database
{
    private final ExecutorService executorService;
    private final AsyncTaskQueue taskQueue;

    private final ExecutorService executor;

    protected final Core core;

    protected AbstractPooledDatabase(Core core)
    {
        this.core = core;
        this.executorService = Executors.newSingleThreadExecutor(core.getTaskManager().getThreadFactory());
        this.executor = Executors.newSingleThreadExecutor(core.getTaskManager().getThreadFactory());
        this.taskQueue = new AsyncTaskQueue(this.executorService);
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
                catch (SQLException ex)
                {
                    core.getLog().error(ex, "An asynchronous query failed!");
                }
            }
        });
    }

    @Override
    public boolean execute(String query, Object... params) throws SQLException
    {
        try (Connection connection = this.getConnection())
        {
            return this.createAndBindValues(connection, query, params).execute();
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
                catch (SQLException ex)
                {
                    core.getLog().error(ex, "An asynchronous query failed!");
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
    public PreparedStatement prepareStatement(String statement) throws SQLException
    {
        expectNotNull(statement, "The statement must not be null!");
        return this.getConnection().prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
    }

    @Override
    public PreparedStatement prepareStatement(String statement, Connection withConnection) throws SQLException
    {
        expectNotNull(statement, "The statement must not be null!");
        return withConnection.prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
    }

    @Override
    public ExecutorService getExecutor()
    {
        return this.executor;
    }
}
