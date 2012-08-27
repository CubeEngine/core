package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.DatabaseConfiguration;
import de.cubeisland.cubeengine.core.storage.database.AbstractDatabase;
import de.cubeisland.cubeengine.core.storage.database.DriverNotFoundException;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.util.Validate;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Phillip Schichtel
 */
public class MySQLDatabase extends AbstractDatabase
{
    private static final char NAME_QUOTE = '`';
    private static final char STRING_QUOTE = '\'';
    
    private final String host;
    private final short port;
    private final String user;
    private final String pass;
    private final String name;
    private final String tablePrefix;
    private final MySQLQueryBuilder queryBuilder;
    private final Thread creationThread = Thread.currentThread();

    public MySQLDatabase(DatabaseConfiguration config) throws SQLException, DriverNotFoundException
    {
        this(config.host, config.port, config.user, config.pass, config.database, config.tableprefix);
    }

    public MySQLDatabase(String host, short port, String user, String pass, String name, String tablePrefix) throws SQLException, DriverNotFoundException
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            throw new DriverNotFoundException("Couldn't find the MySQL driver!");
        }
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.name = name;
        this.tablePrefix = tablePrefix;
        this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + String.valueOf(this.port) + "/" + this.name, this.user, this.pass);
        this.queryBuilder = new MySQLQueryBuilder(this);
    }
    
    public String getName()
    {
        return "MySQL";
    }

    public QueryBuilder getQueryBuilder()
    {
        if (Thread.currentThread() != this.creationThread)
        {
            throw new IllegalStateException("This method may only be called from the thread the database was created in!");
        }
        return this.queryBuilder;
    }
    
    @Override
    public String prepareName(String name)
    {
        Validate.notNull(name, "The name must not be null!");
        
        return NAME_QUOTE + this.tablePrefix + name + NAME_QUOTE;
    }
    
    public String prepareFieldName(String name)
    {
        Validate.notNull(name, "The name must not be null!");
        
        int dotOffset = name.indexOf('.');
        if (dotOffset >= 0)
        {
            return NAME_QUOTE + this.tablePrefix + name.substring(0, dotOffset) + NAME_QUOTE + '.' + NAME_QUOTE + name.substring(dotOffset + 1) + NAME_QUOTE;
        }
        return this.prepareName(name);
    }
}