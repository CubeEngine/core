package de.cubeisland.cubeengine.core.storage.database;

import de.cubeisland.cubeengine.CubeEngine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class AbstractDatabase implements Database
{
    protected Connection connection;
    private final ConcurrentMap<String, PreparedStatement> preparedStatements = new ConcurrentHashMap<String, PreparedStatement>();
    
    public int getLastInsertedId(Statement statement) throws SQLException
    {
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
        CubeEngine.getLogger().log(Level.INFO, "[SQL] " + statement);//TODO das wird 2x geloggt einmal mit SEVERE Warum????
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
}