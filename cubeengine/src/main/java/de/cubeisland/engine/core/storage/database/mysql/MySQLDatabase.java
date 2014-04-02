/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.storage.database.mysql;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import com.avaje.ebean.config.MatchingNamingConvention;
import com.avaje.ebean.config.TableName;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.storage.database.AbstractPooledDatabase;
import de.cubeisland.engine.core.storage.database.DatabaseConfiguration;
import de.cubeisland.engine.core.storage.database.Table;
import de.cubeisland.engine.core.storage.database.TableCreator;
import de.cubeisland.engine.core.storage.database.TableUpdateCreator;
import de.cubeisland.engine.core.task.ListenableExecutorService;
import de.cubeisland.engine.core.task.ListenableFuture;
import de.cubeisland.engine.core.util.Version;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

public class MySQLDatabase extends AbstractPooledDatabase
{
    private final MySQLDatabaseConfiguration config;

    private static final char NAME_QUOTE = '`';
    private static final char STRING_QUOTE = '\'';
    private static String tableprefix;

    private final ListenableExecutorService fetchExecutorService;
    private final ComboPooledDataSource cpds;

    private final Settings settings;

    private DatabaseSchema schema;

    public MySQLDatabase(Core core, MySQLDatabaseConfiguration config) throws SQLException
    {
        super(core);
        this.fetchExecutorService = new ListenableExecutorService();

        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException(e);
        }
        this.config = config;


        cpds = new ComboPooledDataSource();
        cpds.setJdbcUrl("jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database);
        cpds.setUser(config.user);
        cpds.setPassword(config.password);
        cpds.setMinPoolSize(5);
        cpds.setMaxPoolSize(20);
        cpds.setAcquireIncrement(5);
        cpds.setDataSourceName("CubeEngine");
        cpds.setInitialPoolSize(3);
        cpds.setMaxStatements(0); // No Caching jOOQ will do this if needed
        cpds.setMaxStatementsPerConnection(0); // No Caching jOOQ will do this if needed
        Connection connection = cpds.getConnection();
        ResultSet resultSet = connection.prepareStatement("SHOW variables where Variable_name='wait_timeout'").executeQuery();
        if (resultSet.next())
        {
            int second = resultSet.getInt("Value") - 5;
            if (second <= 0)
            {
                second += 5;
            }
            cpds.setMaxIdleTime(second);
        }
        connection.close();
        this.schema = new DatabaseSchema(config.database);
        tableprefix = this.config.tablePrefix;

        settings = new Settings();
        settings.setExecuteLogging(false);
    }

    public static MySQLDatabase loadFromConfig(Core core, Path file)
    {
        MySQLDatabaseConfiguration config = core.getConfigFactory().load(MySQLDatabaseConfiguration.class, file.toFile());
        try
        {
            return new MySQLDatabase(core, config);
        }
        catch (SQLException ex)
        {
            core.getLog().error(ex, "Could not establish connection with the database!");
        }
        return null;
    }

    private boolean updateTableStructure(TableUpdateCreator updater)
    {
        try
        {
            Connection connection = this.getConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT table_name, table_comment \n" +
                                                                          "FROM INFORMATION_SCHEMA.TABLES \n" +
                                                                          "WHERE table_schema = ?\n" +
                                                                          "AND table_name = ?");
            ResultSet resultSet = this.bindValues(stmt, this.config.database, updater.getName()).executeQuery();
            if (resultSet.next())
            {
                Version dbVersion = Version.fromString(resultSet.getString("table_comment"));
                Version version = updater.getTableVersion();
                if (dbVersion.isNewerThan(version))
                {
                    this.core.getLog().info("table-version is newer than expected! {}: {} expected version: {}",
                                            updater.getName(), dbVersion.toString(), version.toString());
                }
                else if (dbVersion.isOlderThan(updater.getTableVersion()))
                {

                    this.core.getLog().info("table-version is too old! Updating {} from {} to {}",
                                            updater.getName(), dbVersion.toString(), version.toString());
                    try
                    {
                        updater.update(connection, dbVersion);
                    }
                    catch (SQLException ex)
                    {
                        throw new IllegalStateException(ex);
                    }
                    this.core.getLog().info("{} got updated to {}", updater.getName(), version.toString());
                    this.bindValues(this.prepareStatement("ALTER TABLE " + updater.getName() + " COMMENT = ?", connection), version.toString()).execute();
                    connection.close(); // return the connection to the pool
                }
                return true;
            }
        }
        catch (Exception e)
        {
            this.core.getLog().warn(e, "Could not execute structure update for the table {}", updater.getName());
        }
        return false;
    }

    /**
     * Creates or updates the table for given entity
     *
     * @param table
     * @param <T>
     */
    public <T extends TableCreator> void registerTable(T table)
    {
        if (table instanceof TableUpdateCreator && this.updateTableStructure((TableUpdateCreator)table))
        {
            return;
        }
        try
        {
            Connection connection = this.getConnection();
            table.createTable(connection);
            connection.close();
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Cannot create table " + table.getName(), ex);
        }
        this.schema.addTable(table);
        this.core.getLog().debug("Database-Table {0} registered!", table.getName());
    }

    @Override
    public <T extends Table> void registerTable(Class<T> clazz)
    {
        try
        {
            Constructor<T> constructor = clazz.getDeclaredConstructor(String.class);
            T table = constructor.newInstance(this.config.tablePrefix);
            this.registerTable(table);
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalStateException("Unable to instantiate Table!", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return this.cpds.getConnection();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return this.getConnection().getMetaData();
    }

    @Override
    public DSLContext getDSL()
    {
        return DSL.using(new DefaultConfiguration().set(SQLDialect.MYSQL)
                                                   .set(new DataSourceConnectionProvider(this.cpds))
                                                   .set(new JooqLogger())
                                                   .set(settings));
    }

    public <R extends Record> ListenableFuture<Result<R>> fetchLater(final ResultQuery<R> query)
    {
        return this.fetchExecutorService.submit(new Callable<Result<R>>()
        {
            @Override
            public Result<R> call() throws Exception
            {
                try
                {
                    return query.fetch();
                }
                catch (DataAccessException e)
                {
                    core.getLog().error("An error occurred while fetching later", e);
                    throw e;
                }
            }
        });
    }

    @Override
    public ListenableFuture<Integer> executeLater(final Query query)
    {
        return this.fetchExecutorService.submit(new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {
                try
                {
                    return query.execute();
                }
                catch (DataAccessException e)
                {
                    core.getLog().error("An error occurred while executing later", e);
                    throw e;
                }
            }
        });
    }

    /**
     * Prepares a table name. (Quoting)
     *
     * @param name the name to prepare
     * @return the prepared name
     */
    public static String prepareTableName(String name)
    {
        expectNotNull(name, "The name must not be null!");

        return NAME_QUOTE + tableprefix + name + NAME_QUOTE;
    }

    /**
     * Prepares a field name. (Quoting).
     *
     * @param name the fieldname to prepare
     * @return the prepared fieldname
     */
    public static String prepareColumnName(String name)
    {
        expectNotNull(name, "The name must not be null!");

        int dotOffset = name.indexOf('.');
        if (dotOffset >= 0)
        {
            return prepareTableName(name.substring(0, dotOffset)) + '.' + NAME_QUOTE + name.substring(dotOffset + 1) + NAME_QUOTE;
        }
        return NAME_QUOTE + name + NAME_QUOTE;
    }

    /**
     * Prepares a string. (Quoting).
     *
     * @param name the string to prepare
     * @return the prepared string
     */
    public static String prepareString(String name)
    {
        return STRING_QUOTE + name + STRING_QUOTE;
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
    }

    @Override
    public String getName()
    {
        return "MySQL";
    }

    class NamingConvention extends MatchingNamingConvention
    {
        @Override
        public TableName getTableName(Class<?> beanClass)
        {
            TableName tableName = super.getTableName(beanClass);
            return new TableName(tableName.getCatalog(), tableName.getSchema(), prepareTableName(tableName.getName()));
        }
    }

    @Override
    public DatabaseConfiguration getDatabaseConfig()
    {
        return this.config;
    }

    @Override
    public String getTablePrefix()
    {
        return this.config.tablePrefix;
    }
}
