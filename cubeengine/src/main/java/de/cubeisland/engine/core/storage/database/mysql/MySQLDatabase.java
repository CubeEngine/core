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
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.storage.database.AbstractPooledDatabase;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.Index;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.Version;

import static de.cubeisland.engine.core.storage.database.AttrType.DataTypeInfo.*;

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

        // Ebeans TEST:
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDriver("com.mysql.jdbc.Driver");
        dataSourceConfig.setUrl("jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database);
        dataSourceConfig.setUsername(config.user);
        dataSourceConfig.setPassword(config.pass);
        dataSourceConfig.setMinConnections(5);
        dataSourceConfig.setMaxConnections(10);
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setDataSourceConfig(dataSourceConfig);
        serverConfig.setName("cubeengine");

        //serverConfig.setDdlGenerate(true);
        //serverConfig.setDdlRun(true);
        EbeanServer ebeanServer = EbeanServerFactory.create(serverConfig);
        ebeanServer.getServerCacheManager().setCaching(UserEntityTest.class, true);
        ebeanServer.getServerCacheManager().init(ebeanServer);

        this.createTableForModel(UserEntityTest.class);
        this.createTableForModel(HasUserEntity.class);

        UserEntityTest created = ebeanServer.createEntityBean(UserEntityTest.class);
        ebeanServer.save(created);
        HasUserEntity created2 = new HasUserEntity();
        created2.setUserEntity(created);
        ebeanServer.save(created2);


        UserEntityTest get = ebeanServer.find(UserEntityTest.class, 1);
        HasUserEntity get2 = ebeanServer.find(HasUserEntity.class, 1);
        UserEntityTest get3 = ebeanServer.find(UserEntityTest.class, 1);
        System.out.print("C:"+created);
        System.out.print("G:"+get);
        System.out.print("G3"+get3);
        System.out.print("IG:" + get2.getUserEntity());

        System.out.print(get.getId() + ":" + get.getPlayer());
        created.setPlayer("CHANGED!");
        ebeanServer.update(created);
        ebeanServer.refresh(get2);
        System.out.print(get2.getId() + ":" + get2.getUserEntity().getPlayer());
        ebeanServer.refresh(get);
        System.out.print(get.getId() + ":" + get.getPlayer());

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

    public void createTableForModel(Class<?> modelClass)
    {
        if (modelClass.isAnnotationPresent(Entity.class) && modelClass.isAnnotationPresent(Table.class))
        {
            Version version = new Version(0);
            StringBuilder builder = new StringBuilder("CREATE");
            // TODO TEMPORARY and alternativ name for temp-table (or just append _temp)
            builder.append(" TABLE");
            builder.append(" IF NOT EXISTS"); // TODO
            Table table = modelClass.getAnnotation(Table.class);
            builder.append(MySQLDatabase.prepareTableName(table.name())).append("\n(");
            boolean first = true;
            boolean autoIncrement = false;
            for (Field field : modelClass.getDeclaredFields())
            {
                if (!first) builder.append(",\n");
                first = false;
                if (field.isAnnotationPresent(javax.persistence.Version.class) && field.getType().equals(Version.class))
                {
                    try
                    {
                        field.setAccessible(true);
                        version = (Version)field.get(null);
                    }
                    catch (IllegalAccessException ignore)
                    {}
                }
                if (field.isAnnotationPresent(Attribute.class))
                {
                    Attribute attribute = field.getAnnotation(Attribute.class);
                    Column column = field.getAnnotation(Column.class);
                    Id id = field.getAnnotation(Id.class);
                    if (id != null)
                    {
                        autoIncrement = true;
                    }
                    if (field.isAnnotationPresent(EmbeddedId.class)) // Multiple Col as Primary Key
                    {
                        Set<String> embededId = new HashSet<String>();
                        for (Field embeddedField : field.getType().getDeclaredFields())
                        {

                        }
                        // TODO MultiKey
                    }
                    else if (id != null || column != null) // Primary Key OR Column
                    {
                        builder.append(getColName(field, column)).append(" ") // col_name
                               .append(attribute.type().name()) // data_type
                               .append(getDataTypeDef(field, column, attribute)) // data_type info
                               .append(getNullOrNotNull(column)) // [NOT NULL | NULL]
                               // TODO DEFAULT //[DEFAULT default_value]
                               .append(getAutoIncrement(id))
                               .append(getKeyType(id, column))
                               .append(getComment(attribute));
                    }
                    else
                    {
                        throw new IllegalStateException("Missing Column Annotation!");
                    }
                    if (field.isAnnotationPresent(ManyToOne.class))
                    {
                        ManyToOne foreignKey = field.getAnnotation(ManyToOne.class);
                        builder.append(",\n").append(getForeignKey(field, column, foreignKey.cascade(), field
                            .getType()));
                    }
                    if (field.isAnnotationPresent(Index.class))
                    {
                        builder.append(",\nINDEX (").append(getColName(field, column)).append(")");
                    }
                }
            }
            // TODO multi unique uses UniqueConstraint in table annotation
            builder.append(")\n");
            if (autoIncrement) builder.append(" AUTO_INCREMENT 1,\n");
            builder.append("ENGINE InnoDB,\n");
            builder.append("COLLATE utf8_unicode_ci,\n");
            builder.append(" COMMENT ").append(MySQLDatabase.prepareString(version.toString()));
            // TODO multi unique uses UniqueConstraint in table annotation
            try
            {
                this.execute(builder.toString());
            }
            catch (SQLException e)
            {
                System.out.print(builder.toString());
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return;
        }
        throw new IllegalArgumentException("The modelClass " + modelClass + " is not a valid DatabaseEntity");
    }

    static String getForeignKey(Field field, Column column, CascadeType[] cascades, Class<?> entityClass)
    {
        Table annotation = entityClass.getAnnotation(Table.class);
        String tableName = MySQLDatabase.prepareTableName(annotation.name());
        StringBuilder builder = new StringBuilder(" FOREIGN KEY ");
        for (Field foreignField : entityClass.getDeclaredFields())
        {
            if (foreignField.isAnnotationPresent(Id.class))
            {
                builder.append(getColName(field, column)).append(" REFERENCES ")
                    .append(tableName).append(" (")
                    .append(getColName(foreignField, foreignField.getAnnotation(Column.class)))
                    .append(")");
                for (CascadeType cascade : cascades)
                {
                    switch (cascade)
                    {
                        case REMOVE:
                            builder.append(" ON DELETE CASCADE");
                            break;
                        case REFRESH:
                            builder.append(" ON UPDATE CASCADE");
                    }
                }
                return builder.toString();
            }
        }
        throw new IllegalStateException("No Primary Key found for a foreign key");

    }

    static String getDataTypeDef(Field field, Column column, Attribute attribute)
    {
        StringBuilder builder = new StringBuilder();
        AttrType type = attribute.type();
        if (type.can(LENGTH))
        {
            if (column.length() != 255)
            {
                builder.append("(").append(column.length());
                if (type.can(DECIMALS))
                {
                    if (column.precision() != 0)
                    {
                        builder.append(", ").append(column.precision());
                    }
                }
                builder.append(")");
            }
        }
        if (type.can(UNSIGNED) && attribute.unsigned())
        {
            builder.append(" UNSIGNED ");
        }
        if (type.can(ZEROFILL) && false) // TODO ZEROFILL
        {
            builder.append(" ZEROFILL ");
        }
        if (type.can(BINARY) && false)
        {
            builder.append(" BINARY ");
        }
        if (type.can(VALUES))
        {
            if (!field.getType().isEnum())
            {
                throw new IllegalStateException("Illegal Field Type for the attribute " + type.name());
            }
            List<String> list = new ArrayList<String>();
            for (Enum e : ((Class<? extends Enum<?>>)field.getType()).getEnumConstants())
            {
                list.add(e.name());
            }
            builder.append("(").append(StringUtils.implode(", ", list)).append(")");
        }
        if (type.can(CHARSET) && false)
        {
            builder.append(" CHARACTER SET "); // TODO CHARSET
        }
        if (type.can(COLLATE)) // TODO COLLATE configurable
        {
            builder.append(" COLLATE ").append("utf8_unicode_ci");
        }
        return builder.toString();
    }

    static String getComment(Attribute attribute)
    {
        return attribute.comment().isEmpty() ? "" : " COMMENT " + MySQLDatabase.prepareString(attribute.comment());
    }

    static String getKeyType(Id id, Column column)
    {
        return id == null ? (column.unique() ? " UNIQUE KEY " : "") : " PRIMARY KEY ";
    }

    static String getAutoIncrement(Id id)
    {
        return id == null ? "" : " AUTO_INCREMENT ";
    }

    // TODO move into QueryBuilder class
    static String getColName(Field field, Column column)
    {
        if (column != null && !column.name().isEmpty())
        {
            return column.name();
        }
        else
        {
            return field.getName();
        }
    }

    static String getNullOrNotNull(Column column)
    {
        return column == null || !column.nullable() ? "NOT NULL" : "NULL";
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
