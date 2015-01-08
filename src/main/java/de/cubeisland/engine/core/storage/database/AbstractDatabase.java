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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;
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
public abstract class AbstractDatabase implements Database
{
    private final ExecutorService executor;

    protected final Core core;
    protected final ThreadFactory threadFactory;

    protected AbstractDatabase(Core core)
    {
        this.core = core;
        this.threadFactory = new DatabaseThreadFactory();
        this.executor = Executors.newSingleThreadExecutor(this.threadFactory);
    }

    @Override
    public CompletableFuture<ResultSet> query(final String query, final Object... params)
    {
        return CompletableFuture.supplyAsync(() -> {
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
                throw new DataAccessException("SQL-Error while doing a query: " + query, e);
            }
        });
    }

    @Override
    public <R extends Record> CompletableFuture<Result<R>> query(final ResultQuery<R> query)
    {
        return CompletableFuture.supplyAsync(query::fetch);
    }

    @Override
    public CompletableFuture<Boolean> execute(final String query, final Object... params)
    {
        return CompletableFuture.supplyAsync(() -> {
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
                throw new DataAccessException("SQL-Error while doing an execute-query: " + query, e);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> execute(final Query query)
    {
        return CompletableFuture.supplyAsync(query::execute);
    }

    @Override
    public CompletableFuture<Integer> update(final String query, final Object... params)
    {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection())
            {
                try (PreparedStatement stmt = prepareStatement(connection, query))
                {
                    bindValues(stmt, params);
                    return stmt.executeUpdate();
                }
            }
            catch (SQLException e)
            {
                throw new DataAccessException("SQL-Error while doing an update-query: " + query, e);
            }
        }, this.executor);
    }

    @Override
    public CompletableFuture<Integer> update(final Query query)
    {
        return execute(query);
    }

    @Override
    public CompletableFuture<DatabaseMetaData> getMetaData()
    {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection c = getConnection())
            {
                return c.getMetaData();
            }
            catch (SQLException e)
            {
                throw new DataAccessException(e.getMessage(), e);
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
        this.executor.shutdown();
    }

    @Override
    public PreparedStatement prepareStatement(Connection connection, String statement) throws SQLException
    {
        expectNotNull(statement, "The statement must not be null!");
        return connection.prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
    }
}
