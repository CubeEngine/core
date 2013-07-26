package de.cubeisland.engine.core.storage.database.mysql;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.storage.database.AbstractPooledDatabase;

public class MySQLDatabase extends AbstractPooledDatabase
{
    private final MySQLDatabaseConfiguration config;
    private BoneCP connectionPool;
    private BoneCPConfig poolConfig;

    private static final char NAME_QUOTE = '`';
    private static final char STRING_QUOTE = '\'';
    private static String tableprefix;

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
        this.poolConfig = new BoneCPConfig();
        this.poolConfig.setJdbcUrl("jdbc:mysql://" + config.host+ ":" + config.port + "/"+config.database);
        this.poolConfig.setUsername(config.user);
        this.poolConfig.setPassword(config.pass);
        this.poolConfig.setMinConnectionsPerPartition(5);
        this.poolConfig.setMaxConnectionsPerPartition(10);
        this.poolConfig.setPartitionCount(1);
        this.connectionPool = new BoneCP(this.poolConfig);

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

    @Override
    public Connection getConnection() throws SQLException
    {
        return this.connectionPool.getConnection();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return this.getConnection().getMetaData();
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
    public static String prepareFieldName(String name)
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
        this.connectionPool.shutdown();
    }

    @Override
    public String getName()
    {
        return "MySQL";
    }
}
