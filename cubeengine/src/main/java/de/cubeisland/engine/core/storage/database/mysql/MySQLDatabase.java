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

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.avaje.ebean.config.MatchingNamingConvention;
import com.avaje.ebean.config.TableName;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.storage.database.AbstractPooledDatabase;
import de.cubeisland.engine.core.storage.database.DatabaseConfiguration;
import de.cubeisland.engine.core.storage.database.TableCreator;
import de.cubeisland.engine.core.storage.database.TableUpdateCreator;
import de.cubeisland.engine.core.util.Version;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import snaq.db.DBPoolDataSource;

public class MySQLDatabase extends AbstractPooledDatabase
{
    private final MySQLDatabaseConfiguration config;

    private static final char NAME_QUOTE = '`';
    private static final char STRING_QUOTE = '\'';
    private static String tableprefix;

    private final DBPoolDataSource datasource;

    private DatabaseSchema schema;

    public MySQLDatabase(Core core, MySQLDatabaseConfiguration config) throws SQLException
    {
        super(core);
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException(e);
        }
        this.config = config;

        datasource = new DBPoolDataSource();
        datasource.setDriverClassName("com.mysql.jdbc.Driver");
        datasource.setUrl("jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database);
        datasource.setUser(config.user);
        datasource.setPassword(config.pass);
        datasource.setMinPool(5);
        datasource.setMaxPool(20);
        datasource.setIdleTimeout(60); // TODO more
        datasource.setName("CubeEngine");

        this.schema = new DatabaseSchema(config.database);
        tableprefix = this.config.tablePrefix;
    }

    public static MySQLDatabase loadFromConfig(Core core, File file)
    {
        MySQLDatabaseConfiguration config = Configuration.load(MySQLDatabaseConfiguration.class, file);
        try
        {
            return new MySQLDatabase(core, config);
        }
        catch (SQLException e)
        {
            core.getLog().error("Could not establish connection with the database!", e);
        }
        return null;
    }

    private boolean updateTableStructure(TableUpdateCreator updater)
    {
        try
        {
            ResultSet resultSet = this.query("SELECT table_name, table_comment \n" +
                                             "FROM INFORMATION_SCHEMA.TABLES \n" +
                                             "WHERE table_schema = ?" +
                                             "\nAND table_name = ?",
                                             this.config.database,
                                             updater.getName());
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
                    Connection connection = this.getConnection();
                    this.core.getLog().info("table-version is too old! Updating {} from {} to {}",
                                            updater.getName(), dbVersion.toString(), version.toString());
                    try
                    {
                        connection.setAutoCommit(false);
                        updater.update(connection, dbVersion);
                        connection.commit();
                    }
                    catch (SQLException ex)
                    {
                        connection.rollback();
                        throw ex;
                    }
                    connection.setAutoCommit(true);
                    this.core.getLog().info("{} got updated to {}", updater.getName(), version.toString());
                    this.bindValues(this.prepareStatement("ALTER TABLE " + updater.getName() + " COMMENT = ?", connection), version.toString()).execute();
                    connection.close(); // return the connection to the pool
                }
                return true;
            }
        }
        catch (Exception e)
        {
            this.core.getLog().warn("Could not execute structure update for the table {}", updater.getName(), e);
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
        if (table instanceof TableUpdateCreator && this.updateTableStructure((TableUpdateCreator)table)) return;
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
        this.core.getLog().debug("Database-Table {} registered!", table.getName());
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return this.datasource.getConnection();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return this.getConnection().getMetaData();
    }

    @Override
    public DSLContext getDSL()
    {
        return DSL.using(this.datasource, SQLDialect.MYSQL);
    }

    /**
     * Prepares a table name. (Quoting)
     *
     * @param name the name to prepare
     * @return the prepared name
     */
    public static String prepareTableName(String name)
    {
        assert name != null: "The name must not be null!";

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
        assert name != null: "The name must not be null!";

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
}
