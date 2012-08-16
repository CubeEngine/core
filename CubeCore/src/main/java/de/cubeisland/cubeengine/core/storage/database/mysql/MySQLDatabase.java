package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.DatabaseConfiguration;
import de.cubeisland.cubeengine.core.storage.database.AbstractDatabase;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Phillip Schichtel
 */
public class MySQLDatabase extends AbstractDatabase
{
    private static final char QUOTE = '`';
    private final String host;
    private final short port;
    private final String user;
    private final String pass;
    private final String name;

    public MySQLDatabase(DatabaseConfiguration config) throws SQLException
    {
        this(config.host, config.port, config.user, config.pass, config.database, config.tableprefix);
    }

    public MySQLDatabase(String host, short port, String user, String pass, String name, String tablePrefix) throws SQLException
    {
        super();
        this.queryBuilder = new MySQLBuilder(this);
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
        this.setTablePrefix(tablePrefix);
        this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + String.valueOf(this.port) + "/" + this.name, this.user, this.pass);
    }
    
    @Override
    public String quote(String name)
    {
        return QUOTE + name + QUOTE;
    }
}