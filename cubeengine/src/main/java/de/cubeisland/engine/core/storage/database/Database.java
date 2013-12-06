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

import de.cubeisland.engine.core.task.ListenableFuture;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.ResultQuery;

/**
 * The Database interface.
 */
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
     * Prepares the statement
     * <p>remember to close the connection to give it back to the connection-pool
     *
     * @param statement the statement
     * @return the prepared statement
     * @throws SQLException
     */
    PreparedStatement prepareStatement(String statement) throws SQLException;

    /**
     * Returns the statement created with given connection
     *
     * @param statement the statement
     * @param withConnection the connection
     * @return the prepared statement
     * @throws SQLException
     */
    PreparedStatement prepareStatement(String statement, Connection withConnection) throws SQLException;

    /**
     * Executes a query.
     *
     * @param query  the query to execute
     * @param params the params
     * @return the ResultSet
     * @throws SQLException
     */
    ResultSet query(String query, Object... params) throws SQLException;

    /**
     * Executes a query.
     *
     * @param query  the query
     * @param params the params
     * @return true if it succeeded
     * @throws SQLException
     */
    boolean execute(String query, Object... params) throws SQLException;

    /**
     * Executes a query asynchronous.
     *
     * @param query  the query
     * @param params the params
     */
    void asyncExecute(String query, Object... params);

    /**
     * Executes an update query.
     *
     * @param query  the query
     * @param params the params
     * @return the affected rows
     * @throws SQLException
     */
    int update(String query, Object... params) throws SQLException;

    /**
     * Executes an update query asynchronous.
     *
     * @param query  the query
     * @param params the params
     */
    void asyncUpdate(String query, Object... params);

    /**
     * Queues in an operation to execute later.
     *
     * @param runnable the operation to execute.
     */
    void queueOperation(Runnable runnable);

    void shutdown();

    DatabaseMetaData getMetaData() throws SQLException;

    public <T extends TableCreator> void registerTable(T table);

    public <T extends Table> void registerTable(Class<T> table);

    public DatabaseConfiguration getDatabaseConfig();

    DSLContext getDSL();

    ListenableFuture<Integer> executeLater(Query query);

    <R extends Record> ListenableFuture<Result<R>> fetchLater(final ResultQuery<R> query);

    String getTablePrefix();
}
