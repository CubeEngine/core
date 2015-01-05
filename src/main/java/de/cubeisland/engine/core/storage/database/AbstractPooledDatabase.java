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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import de.cubeisland.engine.core.Core;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.exception.DataAccessException;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

/**
 * Abstract Database implementing most of the database methods.
 * Extend this class and complement it to use the database.
 */
public abstract class AbstractPooledDatabase implements Database
{
    private final ListeningExecutorService executorService;

    protected final Core core;
    protected final ThreadFactory threadFactory;

    protected AbstractPooledDatabase(Core core)
    {
        this.core = core;
        this.threadFactory = new DatabaseThreadFactory();
        this.executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor(this.threadFactory));
    }

    @Override
    public ListenableFuture<ResultSet> query(final String query, final Object... params)
    {
        return this.executorService.submit(new Callable<ResultSet>()
        {
            @Override
            public ResultSet call() throws Exception
            {
                try (Connection connection = getConnection())
                {
                    try (PreparedStatement stmt = prepareStatement(connection, query))
                    {
                        bindValues(stmt, params);
                        return stmt.executeQuery();
                    }
                }
                catch (SQLException e)
                {
                    throw new SQLException("SQL-Error while doing a query: " + query, e);
                }
            }
        });
    }

    @Override
    public <R extends Record> ListenableFuture<Result<R>> query(final ResultQuery<R> query)
    {
        return this.executorService.submit(new Callable<Result<R>>()
        {
            @Override
            public Result<R> call() throws Exception
            {
                try
                {
                    return query.fetch();
                }
                catch (DataAccessException e)
                {
                    core.getLog().error("An error occurred while fetching later", e);
                    throw e;
                }
            }
        });
    }

    @Override
    public ListenableFuture<Boolean> execute(final String query, final Object... params)
    {
        return this.executorService.submit(new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                try (Connection connection = getConnection())
                {
                    try (PreparedStatement stmt = prepareStatement(connection, query))
                    {
                        bindValues(stmt, params);
                        return stmt.execute();
                    }
                }
                catch (SQLException e)
                {
                    throw new SQLException("SQL-Error while doing an execute-query: " + query, e);
                }
            }
        });
    }

    @Override
    public ListenableFuture<Integer> update(final String query, final Object... params)
    {
        return this.executorService.submit(new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {
                try  (Connection connection = getConnection())
                {
                    try (PreparedStatement stmt = prepareStatement(connection, query))
                    {
                        bindValues(stmt, params);
                        return stmt.executeUpdate();
                    }
                }
                catch (SQLException e)
                {
                    throw new SQLException("SQL-Error while doing an update-query: " + query, e);
                }
            }
        });
    }

    @Override
    public ListenableFuture<Integer> update(final Query query)
    {
        return this.executorService.submit(new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {
                try
                {
                    return query.execute();
                }
                catch (DataAccessException e)
                {
                    core.getLog().error("An error occurred while executing later", e);
                    throw e;
                }
            }
        });
    }

    @Override
    public ListenableFuture<DatabaseMetaData> getMetaData()
    {
        return this.executorService.submit(new Callable<DatabaseMetaData>()
        {
            @Override
            public DatabaseMetaData call() throws Exception
            {
                try (Connection c = getConnection())
                {
                    return c.getMetaData();
                }
            }
        });
    }

    private PreparedStatement bindValues(PreparedStatement statement, Object... params) throws SQLException
    {
        for (int i = 0; i < params.length; ++i)
        {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    @Override
    public void shutdown()
    {
        this.executorService.shutdown();
    }

    @Override
    public PreparedStatement prepareStatement(Connection connection, String statement) throws SQLException
    {
        expectNotNull(statement, "The statement must not be null!");
        return connection.prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
    }
}
