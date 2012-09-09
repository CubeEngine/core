package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.DatabaseConfiguration;
import de.cubeisland.cubeengine.core.storage.database.AbstractDatabase;
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
    private final String database;
    private final String tablePrefix;
    private final MySQLQueryBuilder queryBuilder;
    private final Thread creationThread = Thread.currentThread();

    public MySQLDatabase(DatabaseConfiguration config) throws SQLException
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Couldn't find the MySQL driver!");
        }
        MySQLDatabaseConfiguration configuration = (MySQLDatabaseConfiguration)config;
        this.host = configuration.host;
        this.port = configuration.port;
        this.user = configuration.user;
        this.pass = configuration.pass;
        this.database = configuration.database;
        this.tablePrefix = configuration.tablePrefix;
        this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + String.valueOf(this.port) + "/" + this.database, this.user, this.pass);
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
            return this.prepareName(name.substring(0, dotOffset)) + '.' + NAME_QUOTE + name.substring(dotOffset + 1) + NAME_QUOTE;
        }
        return NAME_QUOTE + name + NAME_QUOTE;
    }

    public String prepareString(String name)
    {
        return STRING_QUOTE + name + STRING_QUOTE;
    }
}