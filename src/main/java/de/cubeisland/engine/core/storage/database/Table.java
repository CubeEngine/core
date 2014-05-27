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
package de.cubeisland.engine.core.storage.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.storage.database.mysql.Keys;
import de.cubeisland.engine.core.util.Version;
import org.jooq.DataType;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;
import org.jooq.types.UShort;

public abstract class Table<R extends Record> extends TableImpl<R> implements TableCreator<R>
{
    public static final DataType<UInteger> U_INTEGER = new DefaultDataType<>(SQLDialect.MYSQL, SQLDataType.INTEGERUNSIGNED, "integer unsigned", "integer unsigned");
    public static final DataType<UShort> U_SMALLINT = new DefaultDataType<>(SQLDialect.MYSQL, SQLDataType.SMALLINTUNSIGNED, "smallint unsigned", "smallint unsigned");
    public static final DataType<UInteger> U_MEDIUMINT = new DefaultDataType<>(SQLDialect.MYSQL, SQLDataType.INTEGERUNSIGNED, "mediumint unsigned", "mediumint unsigned");
    public static final DataType<Boolean> BOOLEAN = new DefaultDataType<>(SQLDialect.MYSQL, SQLDataType.BOOLEAN, "boolean", "boolean");
    public static final DataType<String> LONGTEXT = new DefaultDataType<>(SQLDialect.MYSQL, SQLDataType.CLOB, "longtext", "longtext");

    public Table(String name, Version version)
    {
        super(name);
        this.version = version;
    }

    private final Version version;
    private UniqueKey<R> primaryKey;
    private final List<ForeignKey<R, ?>> foreignKeys = new ArrayList<>();
    private final List<UniqueKey<R>> uniqueKeys = new ArrayList<>();

    private final List<TableField<R, ? >[]> indices = new ArrayList<>();

    private TableField<R, ?>[] fields;

    public final void setPrimaryKey(TableField<R, ?>... fields)
    {
        this.primaryKey = Keys.uniqueKey(this, fields);
        this.uniqueKeys.add(primaryKey);
    }

    public final void addForeignKey(UniqueKey<?> referencedKey, TableField<R, ?>... fields)
    {
        this.foreignKeys.add(Keys.foreignKey(referencedKey, this, fields));
    }

    public final void addUniqueKey(TableField<R, ?>... fields)
    {
        this.uniqueKeys.add(Keys.uniqueKey(this, fields));
    }

    public final void addFields(TableField<R, ?>... fields)
    {
        this.fields = fields;
    }

    @Override
    public final UniqueKey<R> getPrimaryKey()
    {
        return primaryKey;
    }

    @Override
    public final List<UniqueKey<R>> getKeys()
    {
        return uniqueKeys;
    }

    @Override
    public final List<ForeignKey<R, ?>> getReferences() {
        return foreignKeys;
    }

    @Override
    public abstract Class<R> getRecordType();

    @Override
    public final Version getTableVersion()
    {
        return version;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        if (this.fields == null)
        {
            throw new IllegalStateException("Add your fields to the table OR implement createTable yourself!");
        }
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sb.append(QUOTE).append(this.getName()).append(QUOTE).append(" (\n");
        boolean first = true;
        for (TableField<R, ?> field : this.fields)
        {
            if (!first)
            {
                sb.append(",\n");
            }
            this.appendColumnDefinition(sb, field);
            first = false;
        }
        if (this.primaryKey != null)
        {
            sb.append(",\nPRIMARY KEY ");
            this.appendFieldList(sb, primaryKey.getFields());
        }
        for (TableField<R, ?>[] index : this.indices)
        {
            sb.append(",\nINDEX ");
            // TODO index Key Name
            this.appendFieldList(sb, Arrays.asList(index));
        }
        for (UniqueKey<R> uniqueKey : this.uniqueKeys)
        {
            if (uniqueKey == primaryKey)
            {
                continue;
            }
            sb.append(",\nUNIQUE KEY ");
            // TODO unique Key Name
            this.appendFieldList(sb, uniqueKey.getFields());
        }
        for (ForeignKey<R, ?> foreignKey : this.foreignKeys)
        {
            sb.append(",\nFOREIGN KEY ");
            // TODO foreign Key Name
            this.appendFieldList(sb, foreignKey.getFields());
            UniqueKey<? extends Record> key = foreignKey.getKey();
            sb.append(" REFERENCES ").append(QUOTE).append(key.getTable().getName()).append(QUOTE);
            sb.append("(");
            first = true;
            for (TableField field : key.getFields())
            {
                if (!first)
                {
                    sb.append(",");
                }
                sb.append(QUOTE).append(field.getName()).append(QUOTE);
                first = false;
            }
            sb.append(") ON UPDATE CASCADE ON DELETE CASCADE");
        }
        sb.append(")\n");
        sb.append("ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n"); // TODO configurable?
        sb.append("COMMENT='").append(this.version.toString()).append("'");
        CubeEngine.getCore().getLogFactory().getDatabaseLog().info(sb.toString());
        connection.prepareStatement(sb.toString()).execute();
    }
    private static final char QUOTE = '`';

    private void appendFieldList(StringBuilder sb, List<TableField<R, ?>> fields)
    {
        sb.append("(");
        boolean first = true;
        for (TableField field : fields)
        {
            if (!first)
            {
                sb.append(",");
            }
            sb.append(QUOTE).append(field.getName()).append(QUOTE);
            first = false;
        }
        sb.append(")");
    }

    protected void appendColumnDefinition(StringBuilder sb, TableField<R, ?> field)
    {
        sb.append(QUOTE).append(field.getName()).append(QUOTE).append(" ");
        sb.append(field.getDataType().getCastTypeName());
        if (field.getDataType().nullable())
        {
            sb.append(" DEFAULT NULL");
        }
        else
        {
            sb.append(" NOT NULL");
        }
    }

    public void addIndex(TableField<R,?>... fields)
    {
        this.indices.add(fields);
    }
}
