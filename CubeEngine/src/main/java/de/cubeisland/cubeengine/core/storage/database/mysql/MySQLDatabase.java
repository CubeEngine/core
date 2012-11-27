package de.cubeisland.cubeengine.core.storage.database.mysql;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import de.cubeisland.cubeengine.core.storage.database.AbstractDatabase;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConfiguration;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import org.apache.commons.lang.Validate;

import java.sql.SQLException;

/**
 * MYSQLDatabase the MYSQL implementation for the database.
 */
public class MySQLDatabase extends AbstractDatabase
{
    private static final char NAME_QUOTE = '`';
    private static final char STRING_QUOTE = '\'';

    private final String tablePrefix;
    private final MySQLQueryBuilder queryBuilder;
    private final Thread creationThread = Thread.currentThread();

    public MySQLDatabase(DatabaseConfiguration config) throws SQLException
    {
        super(new MysqlConnectionPoolDataSource());
        MySQLDatabaseConfiguration configuration = (MySQLDatabaseConfiguration)config;
        MysqlConnectionPoolDataSource ds = (MysqlConnectionPoolDataSource)this.getDataSource();
        ds.setServerName(configuration.host);
        ds.setPort(configuration.port);
        ds.setUser(configuration.user);
        ds.setPassword(configuration.pass);
        ds.setDatabaseName(configuration.database);
        ds.setMaxReconnects(configuration.connectionPoolSize);
        this.tablePrefix = configuration.tablePrefix;

        this.queryBuilder = new MySQLQueryBuilder(this);
    }

    @Override
    public String getName()
    {
        return "MySQL";
    }

    @Override
    public QueryBuilder getQueryBuilder()
    {
        if (Thread.currentThread() != this.creationThread)
        {
            throw new IllegalStateException("This method may only be called from the thread the database was created in!");
        }
        return this.queryBuilder;
    }

    @Override
    public String prepareTableName(String name)
    {
        Validate.notNull(name, "The name must not be null!");

        return NAME_QUOTE + this.tablePrefix + name + NAME_QUOTE;
    }

    @Override
    public String prepareFieldName(String name)
    {
        Validate.notNull(name, "The name must not be null!");

        int dotOffset = name.indexOf('.');
        if (dotOffset >= 0)
        {
            return this.prepareTableName(name.substring(0, dotOffset)) + '.' + NAME_QUOTE + name.substring(dotOffset + 1) + NAME_QUOTE;
        }
        return NAME_QUOTE + name + NAME_QUOTE;
    }

    @Override
    public String prepareString(String name)
    {
        return STRING_QUOTE + name + STRING_QUOTE;
    }
}
