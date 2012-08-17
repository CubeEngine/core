package de.cubeisland.cubeengine.core.storage.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Phillip Schichtel
 */
public interface Database
{
    public String getName();
    
    public String prepareName(String name, boolean isTableName);

    public QueryBuilder getQueryBuilder();

    public PreparedStatement bindValues(PreparedStatement statement, Object... params) throws SQLException;

    public PreparedStatement createAndBindValues(String query, Object... params) throws SQLException;

    public boolean execute(String query, Object... params) throws SQLException;

    public PreparedStatement getStoredStatement(Class owner, String name);

    public void prepareAndStoreStatement(Class owner, String name, String statement) throws SQLException;

    public PreparedStatement prepareStatement(String statement) throws SQLException;

    public boolean preparedExecute(Class owner, String name, Object... params) throws SQLException;

    public ResultSet preparedQuery(Class owner, String name, Object... params) throws SQLException;

    public int preparedUpdate(Class owner, String name, Object... params) throws SQLException;

    public ResultSet query(String query, Object... params) throws SQLException;

    public void storePreparedStatement(Class owner, String name, PreparedStatement statement);

    public int update(String query, Object... params) throws SQLException;
    
    public int getLastInsertedId(Statement statement) throws SQLException;
}
