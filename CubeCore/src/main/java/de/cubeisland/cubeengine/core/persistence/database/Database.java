package de.cubeisland.cubeengine.core.persistence.database;

import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.core.persistence.StorageException;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 *
 * @author Phillip Schichtel
 */
public class Database
{
    private static final Pattern PREFIX_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}", Pattern.CASE_INSENSITIVE);
    private String replacement;

    private final String host;
    private final short port;
    private final String user;
    private final String pass;
    private final String name;
    private String tablePrefix;
    private final THashMap<String, PreparedStatement> preparedStatements;

    private final Connection connection;

    public Database(String user, String pass, String name, String tablePrefix)
    {
        this("localhost", (short)3306, user, pass, name, tablePrefix);
    }

    public Database(String host, short port, String user, String pass, String name, String tablePrefix)
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (Throwable t)
        {
            throw new IllegalStateException("Couldn't find the MySQL driver!", t);
        }
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.name = name;
        this.tablePrefix = tablePrefix;
        this.replacement = this.tablePrefix + "$1";
        try
        {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + String.valueOf(this.port) + "/" + this.name, this.user, this.pass);
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Failed to connect to the database server!", e);
        }
        this.preparedStatements = new THashMap<String, PreparedStatement>();
    }

    public String getTablePrefix()
    {
        return this.tablePrefix;
    }

    public Database setTablePrefix(String prefix)
    {
        if (prefix != null)
        {
            this.tablePrefix = prefix;
            this.replacement = prefix + "$1";
        }
        return this;
    }

    public ResultSet query(String query, Object... params) throws SQLException
    {
        return createStatement(query, params).executeQuery();
    }

    public ResultSet preparedQuery(String name, Object... params) throws SQLException
    {
        return createStatement(getStatement(name), params).executeQuery();
    }

    public int update(String query, Object... params) throws SQLException
    {
        return createStatement(query, params).executeUpdate();
    }

    public int preparedUpdate(String name, Object... params) throws SQLException
    {
        return createStatement(getStatement(name), params).executeUpdate();
    }

    public boolean exec(String query, Object... params) throws SQLException
    {
        return createStatement(query, params).execute();
    }

    public boolean preparedExec(String name, Object... params) throws SQLException
    {
        return createStatement(getStatement(name), params).execute();
    }

    public PreparedStatement createStatement(String query, Object... params) throws SQLException
    {
        return this.createStatement(this.prepareStatement(query), params);
    }

    public PreparedStatement createStatement(PreparedStatement statement, Object... params) throws SQLException
    {
        for (int i = 0; i < params.length; ++i)
        {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    public void prepareStatement(String name, String statement) throws SQLException
    {
        this.preparedStatements.put(name, this.prepareStatement(statement));
    }

    public PreparedStatement prepareStatement(String statement) throws SQLException
    {
        return this.connection.prepareStatement(PREFIX_PATTERN.matcher(statement).replaceAll(this.replacement),PreparedStatement.RETURN_GENERATED_KEYS);
    }

    public PreparedStatement getStatement(String name)
    {
        PreparedStatement statement = this.preparedStatements.get(name);
        if (statement == null)
        {
            throw new IllegalArgumentException("Statement not found!");
        }
        return statement;
    }
    
    public void assignId(PreparedStatement ps, Model model)
    {
        try
        {
            if (ps.executeUpdate() > 0) 
            {
                final ResultSet result = ps.getGeneratedKeys();
                if (result.next())
                {
                    model.setId(result.getInt("GENERATED_KEY"));
                }
            }
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to store the user!", e);
        }
        
    }
}
