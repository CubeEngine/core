package de.cubeisland.cubeengine.core.persistence.database;

import de.cubeisland.cubeengine.core.DatabaseConfiguration;
import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Phillip Schichtel
 */
public class Database
{
    private static final char QUOTE = '`';
    private final String host;
    private final short port;
    private final String user;
    private final String pass;
    private final String name;
    private String tablePrefix;
    private final ConcurrentMap<String, PreparedStatement> preparedStatements;
    private final Connection connection;

    public Database(DatabaseConfiguration config) throws SQLException
    {
        this(config.host, config.port, config.user, config.pass, config.database, config.tableprefix);
    }

    public Database(String host, short port, String user, String pass, String name, String tablePrefix) throws SQLException
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Couldn't find the MySQL driver!", e);
        }
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.name = name;
        this.tablePrefix = tablePrefix;
        this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + String.valueOf(this.port) + "/" + this.name, this.user, this.pass);
        this.preparedStatements = new ConcurrentHashMap<String, PreparedStatement>();
    }

    public String getTablePrefix()
    {
        return this.tablePrefix;
    }
    
    public String quote(String name)
    {
        return QUOTE + name + QUOTE;
    }
    
    public String prefix(String tableName)
    {
        return this.prefix(tableName, true);
    }
    
    public String prefix(String tableName, boolean addQuotes)
    {
        if (addQuotes)
        {
            return this.quote(this.tablePrefix + tableName);
        }
        return this.tablePrefix + tableName;
    }

    public void setTablePrefix(String prefix)
    {
        if (prefix != null)
        {
            this.tablePrefix = prefix;
        }
    }

    public ResultSet query(String query, Object... params) throws SQLException
    {
        return this.createAndBindValues(query, params).executeQuery();
    }

    public ResultSet preparedQuery(String name, Object... params) throws SQLException
    {
        return this.bindValues(getStoredStatement(name), params).executeQuery();
    }

    public int update(String query, Object... params) throws SQLException
    {
        return this.createAndBindValues(query, params).executeUpdate();
    }

    public int preparedUpdate(String name, Object... params) throws SQLException
    {
        return this.bindValues(getStoredStatement(name), params).executeUpdate();
    }

    public boolean execute(String query, Object... params) throws SQLException
    {
        return this.createAndBindValues(query, params).execute();
    }

    public boolean preparedExecute(String name, Object... params) throws SQLException
    {
        return this.bindValues(getStoredStatement(name), params).execute();
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
    
    public void storePreparedStatement(String name, PreparedStatement statement)
    {
        this.preparedStatements.put(name, statement);
    }

    public void prepareAndStoreStatement(String name, String statement) throws SQLException
    {
        this.storePreparedStatement(name, this.prepareStatement(statement));
    }

    public PreparedStatement prepareStatement(String statement) throws SQLException
    {
        return this.connection.prepareStatement(statement, PreparedStatement.RETURN_GENERATED_KEYS);
    }

    public PreparedStatement getStoredStatement(String name)
    {
        PreparedStatement statement = this.preparedStatements.get(name);
        if (statement == null)
        {
            throw new IllegalArgumentException("Statement not found!");
        }
        return statement;
    }

    // TODO remove this
    public void assignId(PreparedStatement ps, Model<Integer> model)
    {
        try
        {
            if (ps.executeUpdate() > 0)
            {
                final ResultSet result = ps.getGeneratedKeys();
                if (result.next())
                {
                    model.setKey(result.getInt("GENERATED_KEY"));
                }
            }
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to store the user!", e);
        }
    }
}