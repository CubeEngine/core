package de.cubeisland.cubeengine.core.storage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class AbstractDatabase implements Database
{
    protected Connection connection;
    private final ConcurrentMap<String, PreparedStatement> preparedStatements = new ConcurrentHashMap<String, PreparedStatement>();
    private boolean inTransaction;

    public int getLastInsertedId(Class owner, String name, Object... params) throws SQLException
    {
        PreparedStatement statement = this.bindValues(this.getStoredStatement(owner, name), params);
        statement.execute();
        final ResultSet result = statement.getGeneratedKeys();
        if (result.next())
        {
            return result.getInt("GENERATED_KEY");
        }
        throw new SQLException("Failed to retrieve the last inserted ID!");
    }

    public ResultSet query(String query, Object... params) throws SQLException
    {
        return this.createAndBindValues(query, params).executeQuery();
    }

    public ResultSet preparedQuery(Class owner, String name, Object... params) throws SQLException
    {
        return this.bindValues(getStoredStatement(owner, name), params).executeQuery();
    }

    public int update(String query, Object... params) throws SQLException
    {
        return this.createAndBindValues(query, params).executeUpdate();
    }

    public int preparedUpdate(Class owner, String name, Object... params) throws SQLException
    {
        return this.bindValues(getStoredStatement(owner, name), params).executeUpdate();
    }

    public boolean execute(String query, Object... params) throws SQLException
    {
        return this.createAndBindValues(query, params).execute();
    }

    public boolean preparedExecute(Class owner, String name, Object... params) throws SQLException
    {
        return this.bindValues(getStoredStatement(owner, name), params).execute();
    }

    public PreparedStatement createAndBindValues(String query, Object... params) throws SQLException
    {
        return this.bindValues(this.prepareStatement(query), params);
    }

    public PreparedStatement bindValues(PreparedStatement statement, Object... params) throws SQLException
    {
        for (int i = 0; i < params.length; ++i)
        {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    public void storePreparedStatement(Class owner, String name, PreparedStatement statement)
    {
        this.preparedStatements.put(owner.getName() + "_" + name, statement);
    }

    public void prepareAndStoreStatement(Class owner, String name, String statement) throws SQLException
    {
        this.storePreparedStatement(owner, name, this.prepareStatement(statement));
    }

    public PreparedStatement prepareStatement(String statement) throws SQLException
    {
        return this.connection.prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
    }

    public PreparedStatement getStoredStatement(Class owner, String name)
    {
        PreparedStatement statement = this.preparedStatements.get(owner.getName() + "_" + name);
        if (statement == null)
        {
            throw new IllegalArgumentException("Statement not found!");
        }
        return statement;
    }

    public void startTransaction() throws SQLException
    {
        this.inTransaction = true;
        this.prepareStatement(this.getQueryBuilder().startTransaction().end()).execute();
    }

    public void commmit() throws SQLException
    {
        this.inTransaction = false;
        this.prepareStatement(this.getQueryBuilder().commit().end()).execute();
    }

    public void rollback() throws SQLException
    {
        this.inTransaction = false;
        this.prepareStatement(this.getQueryBuilder().rollback().end()).execute();
    }
}