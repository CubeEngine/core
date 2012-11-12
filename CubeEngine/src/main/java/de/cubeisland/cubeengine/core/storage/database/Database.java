package de.cubeisland.cubeengine.core.storage.database;

import de.cubeisland.cubeengine.core.storage.Storage;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
     * Prepares a name. (Quoting)
     * 
     * @param name the name to prepare
     * @return the prepared name
     */
    String prepareTableName(String name);

    /**
     * Prepares a field name. (Quoting).
     * 
     * @param name the fieldname to prepare
     * @return the prepared fieldname
     */
    String prepareFieldName(String name);

    /**
     * Prepares a string. (Quoting).
     * 
     * @param name the string to prepare
     * @return the prepared string
     */
    String prepareString(String name);

    /**
     * Returns the QueryBuilder.
     * 
     * @return the querybuilder
     */
    QueryBuilder getQueryBuilder();

    /**
     * Binds the params to the given statement.
     * 
     * @param statement the statement
     * @param params the values to bind
     * @return the prepared statement with values
     * @throws SQLException 
     */
    PreparedStatement bindValues(PreparedStatement statement, Object... params) throws SQLException;

    /**
     * Creates a new Query and binds the params.
     * 
     * @param query the query
     * @param params the values to bind
     * @return the prepared Statement with values
     * @throws SQLException 
     */
    PreparedStatement createAndBindValues(String query, Object... params) throws SQLException;

    /**
     * Gets a stored statement by name
     * 
     * @param owner the owner of the statement
     * @param name the name of the statement
     * @return the prepared Statement
     */
    PreparedStatement getStoredStatement(Class owner, String name);

    /**
     * Prepares and stores a statement for given name.
     * 
     * @param owner the owner
     * @param name the name
     * @param statement the statment to store
     * @throws SQLException 
     */
    void prepareAndStoreStatement(Class owner, String name, String statement) throws SQLException;

    /**
     * Prepares the statement
     * 
     * @param statement the statement
     * @return the prepared statement
     * @throws SQLException 
     */
    PreparedStatement prepareStatement(String statement) throws SQLException;

    /**
     * Stores a prepared statement
     * 
     * @param owner the owner
     * @param name the name
     * @param statement the prepared statement to store
     */
    void storePreparedStatement(Class owner, String name, PreparedStatement statement);

    /**
     * Executes a query.
     * 
     * @param query the query to execute
     * @param params the params
     * @return the ResultSet
     * @throws SQLException 
     */
    ResultSet query(String query, Object... params) throws SQLException;

    /**
     * Excecutes a prepared query.
     * 
     * @param owner the owner
     * @param name the stored name
     * @param params the params
     * @return the ResultSet
     * @throws SQLException 
     */
    ResultSet preparedQuery(Class owner, String name, Object... params) throws SQLException;

    /**
     * Executes a query.
     * 
     * @param query the query
     * @param params the params
     * @return
     * @throws SQLException 
     */
    boolean execute(String query, Object... params) throws SQLException;

    /**
     * Excecutes a prepared query.
     * 
     * @param owner the owner
     * @param name the name
     * @param params the params
     * @return
     * @throws SQLException 
     */
    boolean preparedExecute(Class owner, String name, Object... params) throws SQLException;

    /**
     * Executes a query asynchronious.
     * 
     * @param query the query
     * @param params the params
     */
    void asyncExecute(String query, Object... params);

    /**
     * Excecutes a prepared query asynchronious.
     * 
     * @param owner the owner
     * @param name the name
     * @param params the params
     */
    void asyncPreparedExecute(Class owner, String name, Object... params);

    /**
     * Executes an update query.
     * 
     * @param query the query
     * @param params the params
     * @return
     * @throws SQLException 
     */
    int update(String query, Object... params) throws SQLException;

    /**
     * Executes a prepared update query.
     * 
     * @param owner the owner
     * @param name the name
     * @param params the params
     * @return
     * @throws SQLException 
     */
    int preparedUpdate(Class owner, String name, Object... params) throws SQLException;

    /**
     * Executes an update query asynchronious.
     * 
     * @param query the query
     * @param params the params
     */
    void asyncUpdate(String query, Object... params);

    /**
     * Executes a prepared update query asynchronious.
     * 
     * @param owner the owner
     * @param name the name
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
     * Commits a transaction.
     * 
     * @throws SQLException 
     */
    void commmit() throws SQLException;

    /**
     * Rollbacks a transaction.
     * 
     * @throws SQLException 
     */
    void rollback() throws SQLException;

    /**
     * Updates a table.
     * 
     * @param manager 
     */
    void update(Storage manager);

    /**
     * Executes an insert query and returns the last inserted id.
     * 
     * @param owner the owner
     * @param name the name
     * @param params the params
     * @return the last inserted id
     * @throws SQLException 
     */
    int getLastInsertedId(Class owner, String name, Object... params) throws SQLException;

    /**
     * Queues in an operation to execute later.
     * 
     * @param runnable the operation to execute.
     */
    void queueOperation(Runnable runnable);
}
