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

import com.avaje.ebean.EbeanServer;
import de.cubeisland.engine.core.storage.Storage;

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
     * Returns the database metadata.
     *
     * @return the metadata
     * @throws SQLException
     */
    DatabaseMetaData getMetaData() throws SQLException;



    /**
     * Gets a stored statement by name
     *
     * @param owner the owner of the statement
     * @param name  the name of the statement
     * @return the prepared Statement
     */
    PreparedStatement getStoredStatement(Class owner, String name);

    /**
     * Gets a stored statement by name with given connection
     *
     * @param owner the owner of the statement
     * @param name  the name of the statement
     * @param connection the connection to get the statement with
     * @return the prepared Statement
     */
    PreparedStatement getStoredStatement(Class owner, String name, Connection connection);

    /**
     * Gets the raw string used for a stored statement
     *
     * @param owner the owner of the statement
     * @param name the name of the statement
     * @return the stored String
     */
    String getRawStoredStatement(Class owner, String name);

    /**
     * Prepares and stores a statement for given name.
     *
     * @param owner     the owner
     * @param name      the name
     * @param statement the statment to store
     * @throws SQLException
     */
    void storeStatement(Class owner, String name, String statement) throws SQLException;

    /**
     * Prepares the statement
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
     * Executes a prepared query.
     *
     * @param owner  the owner
     * @param name   the stored name
     * @param params the params
     * @return the ResultSet
     * @throws SQLException
     */
    ResultSet preparedQuery(Class owner, String name, Object... params) throws SQLException;

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
     * Executes a prepared query.
     *
     * @param owner  the owner
     * @param name   the name
     * @param params the params
     * @return true if it succeeded
     * @throws SQLException
     */
    boolean preparedExecute(Class owner, String name, Object... params) throws SQLException;

    /**
     * Executes a query asynchronous.
     *
     * @param query  the query
     * @param params the params
     */
    void asyncExecute(String query, Object... params);

    /**
     * Executes a prepared query asynchronous.
     *
     * @param owner  the owner
     * @param name   the name
     * @param params the params
     */
    void asyncPreparedExecute(Class owner, String name, Object... params);

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
     * Executes a prepared update query.
     *
     * @param owner  the owner
     * @param name   the name
     * @param params the params
     * @return the affected rows
     * @throws SQLException
     */
    int preparedUpdate(Class owner, String name, Object... params) throws SQLException;

    /**
     * Executes an update query asynchronous.
     *
     * @param query  the query
     * @param params the params
     */
    void asyncUpdate(String query, Object... params);

    /**
     * Executes a prepared update query asynchronous.
     *
     * @param owner  the owner
     * @param name   the name
     * @param params the params
     */
    void asnycPreparedUpdate(Class owner, String name, Object... params);

    /**
     * Starts a transaction
     *
     * @throws SQLException
     */
    void startTransaction() throws SQLException;

    /**
     * Commits and ends an transaction.
     *
     * @throws SQLException
     */
    void commit() throws SQLException;

    /**
     * Rollbacks a transaction.
     *
     * @throws SQLException
     */
    void rollback() throws SQLException;

    /**
     * Updates a table.
     *
     * @param manager the manager
     */
    void updateStructure(Storage manager);

    /**
     * Executes an insert query and returns the last inserted id.
     *
     * @param owner  the owner
     * @param name   the name
     * @param params the params
     * @return the last inserted id
     * @throws SQLException
     */
    Object getLastInsertedId(Class owner, String name, Object... params) throws SQLException;

    /**
     * Queues in an operation to execute later.
     *
     * @param runnable the operation to execute.
     */
    void queueOperation(Runnable runnable);

    void shutdown();

    EbeanServer getEbeanServer();

    public void createTableForModel(Class<?> modelClass);

    public DatabaseConfiguration getDatabaseConfig();
}
