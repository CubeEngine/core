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
package org.cubeengine.libcube.service.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.modularity.asm.marker.Service;
import de.cubeisland.engine.modularity.asm.marker.Version;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.ResultQuery;

/**
 * The Database interface.
 */
@Service
@Version(1)
public interface Database
{
    /**
     * Returns the name of this database.
     *
     * @return the databases name
     */
    String getName();

    /**
     * Returns a/the connection this database is using.
     *
     * @return a JDBC connection
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * Returns the statement created with given connection
     *
     * @param statement the statement
     * @return the prepared statement
     * @throws SQLException
     */
    PreparedStatement prepareStatement(Connection c, String statement) throws SQLException;

    /**
     * Executes a query.
     *
     * @param query  the query to execute
     * @param params the params
     * @return the ResultSet
     */
    CompletableFuture<ResultSet> query(String query, Object... params);

    <R extends Record> CompletableFuture<Result<R>> query(final ResultQuery<R> query);

    <R extends Record> CompletableFuture<R> queryOne(final ResultQuery<R> query);

    /**
     * Executes a query.
     *
     * @param query  the query
     * @param params the params
     * @return true if it succeeded
     * @throws SQLException
     */
    CompletableFuture<Boolean> execute(String query, Object... params);

    CompletableFuture<Integer> execute(Query query);

    /**
     * Executes an update query.
     *
     * @param query  the query
     * @param params the params
     * @return the affected rows
     * @throws SQLException
     */
    CompletableFuture<Integer> update(String query, Object... params);

    CompletableFuture<Integer> update(Query query);

    void shutdown();

    CompletableFuture<DatabaseMetaData> getMetaData();

    void registerTable(TableCreator<?> table);

    void registerTable(Class<? extends Table<?>> table);

    DatabaseConfiguration getDatabaseConfig();

    DSLContext getDSL();

    String getTablePrefix();

    Log getLog();
}
