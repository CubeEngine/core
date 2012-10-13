package de.cubeisland.cubeengine.core.storage.database;

import de.cubeisland.cubeengine.core.storage.Storage;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Phillip Schichtel
 */
public interface Database
{
    String getName();

    String prepareName(String name);

    String prepareFieldName(String name);

    String prepareString(String name);

    QueryBuilder getQueryBuilder();

    PreparedStatement bindValues(PreparedStatement statement, Object... params) throws SQLException;

    PreparedStatement createAndBindValues(String query, Object... params) throws SQLException;

    PreparedStatement getStoredStatement(Class owner, String name);

    void prepareAndStoreStatement(Class owner, String name, String statement) throws SQLException;

    PreparedStatement prepareStatement(String statement) throws SQLException;

    void storePreparedStatement(Class owner, String name, PreparedStatement statement);

    ResultSet query(String query, Object... params) throws SQLException;

    ResultSet preparedQuery(Class owner, String name, Object... params) throws SQLException;

    boolean execute(String query, Object... params) throws SQLException;

    boolean preparedExecute(Class owner, String name, Object... params) throws SQLException;

    void asyncExecute(String query, Object... params);

    void asyncPreparedExecute(Class owner, String name, Object... params);

    int update(String query, Object... params) throws SQLException;

    int preparedUpdate(Class owner, String name, Object... params) throws SQLException;

    void asyncUpdate(String query, Object... params);

    void asnycPreparedUpdate(Class owner, String name, Object... params);

    void startTransaction() throws SQLException;

    void commmit() throws SQLException;

    void rollback() throws SQLException;

    void update(Storage manager);

    int getLastInsertedId(Class owner, String name, Object... params) throws SQLException;

    void queueOperation(Runnable runnable);
}
