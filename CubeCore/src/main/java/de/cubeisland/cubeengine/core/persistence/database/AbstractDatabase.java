package de.cubeisland.cubeengine.core.persistence.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class AbstractDatabase implements Database
{
    protected Connection connection;
    private String tablePrefix = "";
    protected QueryBuilder queryBuilder = null;
    private final ConcurrentMap<String, PreparedStatement> preparedStatements = new ConcurrentHashMap<String, PreparedStatement>();
    
    public AbstractDatabase()
    {}

    public QueryBuilder buildQuery()
    {
        return this.queryBuilder;
    }

    @Override
    public String getTablePrefix()
    {
        return this.tablePrefix;
    }

    @Override
    public void setTablePrefix(String prefix)
    {
        if (prefix != null)
        {
            this.tablePrefix = prefix;
        }
    }
    
    @Override
    public String prefix(String tableName)
    {
        return this.prefix(tableName, true);
    }
    
    @Override
    public String prefix(String tableName, boolean addQuotes)
    {
        if (addQuotes)
        {
            return this.quote(this.tablePrefix + tableName);
        }
        return this.tablePrefix + tableName;
    }
    
    public int getLastInsertedId(Statement statement) throws SQLException
    {
        final ResultSet result = statement.getGeneratedKeys();
        if (result.next())
        {
            return result.getInt("GENERATED_KEY");
        }
        throw new SQLException("Failed to retrieve the last inserted ID!");
    }

    @Override
    public ResultSet query(String query, Object... params) throws SQLException
    {
        return this.createAndBindValues(query, params).executeQuery();
    }

    @Override
    public ResultSet preparedQuery(Class owner, String name, Object... params) throws SQLException
    {
        return this.bindValues(getStoredStatement(owner, name), params).executeQuery();
    }

    @Override
    public int update(String query, Object... params) throws SQLException
    {
        return this.createAndBindValues(query, params).executeUpdate();
    }

    @Override
    public int preparedUpdate(Class owner, String name, Object... params) throws SQLException
    {
        return this.bindValues(getStoredStatement(owner, name), params).executeUpdate();
    }

    @Override
    public boolean execute(String query, Object... params) throws SQLException
    {
        return this.createAndBindValues(query, params).execute();
    }

    @Override
    public boolean preparedExecute(Class owner, String name, Object... params) throws SQLException
    {
        return this.bindValues(getStoredStatement(owner, name), params).execute();
    }

    @Override
    public PreparedStatement createAndBindValues(String query, Object... params) throws SQLException
    {
        return this.bindValues(this.prepareStatement(query), params);
    }

    @Override
    public PreparedStatement bindValues(PreparedStatement statement, Object... params) throws SQLException
    {
        for (int i = 0; i < params.length; ++i)
        {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
    
    @Override
    public void storePreparedStatement(Class owner, String name, PreparedStatement statement)
    {
        this.preparedStatements.put(owner.getName() + "_" + name, statement);
    }

    @Override
    public void prepareAndStoreStatement(Class owner, String name, String statement) throws SQLException
    {
        this.storePreparedStatement(owner, name, this.prepareStatement(statement));
    }

    @Override
    public PreparedStatement prepareStatement(String statement) throws SQLException
    {
        return this.connection.prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
    }

    @Override
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
