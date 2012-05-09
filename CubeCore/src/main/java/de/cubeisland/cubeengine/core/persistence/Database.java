package de.cubeisland.cubeengine.core.persistence;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author CodeInfection
 */
public class Database
{
    private final String host;
    private final short port;
    private final String user;
    private final String pass;
    private final String name;
    private String prefix;

    private final Connection connection;

    public Database(String user, String pass, String name)
    {
        this("localhost", (short)3306, user, pass, name);
    }

    public Database(String host, short port, String user, String pass, String name)
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
        this.prefix = "";
        try
        {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + String.valueOf(this.port) + "/" + this.name, this.user, this.pass);
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Failed to connect to the database server!", e);
        }
    }

    public String getTablePrefix()
    {
        return this.prefix;
    }

    public Database setTablePrefix(String prefix)
    {
        this.prefix = prefix;
        return this;
    }

    public void setupStructure(Plugin plugin)
    {
        Class clazz = plugin.getClass();
        InputStream inputStream = clazz.getResourceAsStream("/sql/structure.sql");
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[512];
        int bytesRead;

        try
        {
            while ((bytesRead = inputStream.read(buffer)) > 0)
            {
                sb.append(new String(buffer, 0, bytesRead, "UTF-8"));
            }
            inputStream.close();

            this.exec(sb.toString().replace("{{PREFIX}}", this.prefix));
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }

    public ResultSet query(String query, Object... params)
    {
        try
        {
            return createStatement(query, params).executeQuery();
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Failed to execute a query!", e);
        }
    }

    public int execUpdate(String query, Object... params)
    {
        try
        {
            return createStatement(query, params).executeUpdate();
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Failed to execute a query!", e);
        }
    }

    public boolean exec(String query, Object... params)
    {
        try
        {
            return createStatement(query, params).execute();
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Failed to execute a query!", e);
        }
    }

    private PreparedStatement createStatement(String query, Object... params) throws SQLException
    {
        PreparedStatement statement = this.connection.prepareStatement(query);
        for (int i = 0; i < params.length; ++i)
        {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
}
