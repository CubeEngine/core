/*
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
package org.cubeengine.libcube.service.database;

import static org.cubeengine.libcube.service.database.TableVersion.TABLE_VERSION;
import static org.jooq.impl.DSL.constraint;

import org.cubeengine.libcube.service.database.mysql.Keys;
import org.cubeengine.libcube.util.Version;
import org.jooq.CreateTableColumnStep;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Table<R extends Record> extends TableImpl<R> implements TableCreator<R>
{
    // This is not working because DataType#hasLength returns false for the converted type UUID
    // jOOQ issue: https://github.com/jOOQ/jOOQ/issues/5807
    // public static final DataType<UUID> UUID_TYPE = SQLDataType.VARCHAR(36).asConvertedDataType(new UUIDConverter());
    public static final DataType<UUID> UUID_TYPE = new UUIDDataType(false);

    public Table(Class<R> model, String name, Version version)
    {
        super(name);
        this.model = model;
        this.version = version;
    }

    Table(Class<R> model, String name) // NonVersioned
    {
        this(model, name, null);
    }

    private Class<R> model;
    private final Version version;
    private UniqueKey<R> primaryKey;
    private final List<ForeignKey<R, ?>> foreignKeys = new ArrayList<>();
    private final List<UniqueKey<R>> uniqueKeys = new ArrayList<>();

    private final List<TableField<R, ?>[]> indices = new ArrayList<>();

    private TableField<R, ?>[] fields;

    protected final void setPrimaryKey(TableField<R, ?>... fields)
    {
        this.primaryKey = Keys.uniqueKey(this, fields);
        this.uniqueKeys.add(primaryKey);
    }

    protected final void addForeignKey(UniqueKey<?> referencedKey, TableField<R, ?>... fields)
    {
        this.foreignKeys.add(Keys.foreignKey(referencedKey, this, fields));
    }

    protected final void addUniqueKey(TableField<R, ?>... fields)
    {
        this.uniqueKeys.add(Keys.uniqueKey(this, fields));
    }

    protected final void addFields(TableField<R, ?>... fields)
    {
        this.fields = fields;
    }

    protected void addIndex(TableField<R, ?>... fields)
    {
        this.indices.add(fields);
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
    public final List<ForeignKey<R, ?>> getReferences()
    {
        return foreignKeys;
    }

    @Override
    public Class<R> getRecordType()
    {
        return model;
    }

    @Override
    public final Version getTableVersion()
    {
        return version;
    }

    @Override
    public void createTable(Database db) throws SQLException
    {
        DSLContext dsl = db.getDSL();

        if (this.fields == null)
        {
            throw new IllegalStateException("Add your fields to the table OR implement createTable yourself!");
        }

        CreateTableColumnStep tableCreator = dsl.createTableIfNotExists(this).columns(this.fields);

        if (this.primaryKey != null)
        {
            tableCreator.constraint(constraint().primaryKey(this.primaryKey.getFieldsArray()));
        }

        for (UniqueKey<R> uniqueKey : this.uniqueKeys)
        {
            tableCreator.constraint(constraint().unique(uniqueKey.getFieldsArray()));
        }

        for (ForeignKey<R, ?> foreignKey : this.foreignKeys)
        {
            TableField<R, ?>[] fields = foreignKey.getFieldsArray();
            UniqueKey pKey = foreignKey.getKey();
            if (fields.length == 1)
            {
                tableCreator.constraint(
                        constraint().foreignKey(fields[0])
                                    .references(pKey.getTable(), pKey.getFieldsArray()[0])
                                    .onDeleteCascade());
            }
            else
            {
                tableCreator.constraint(
                        constraint().foreignKey(fields)
                                    .references(pKey.getTable(), pKey.getFieldsArray()));
            }
        }

        tableCreator.execute();

        int i = 0;
        for (TableField<R, ?>[] index : this.indices)
        {
            i++;
            dsl.createIndexIfNotExists("I" + i + "_" + this.getName()).on(this, index).execute();
        }

        if (this.version != null)
        {
            dsl.mergeInto(TABLE_VERSION).values(getName(), getTableVersion().toString()).execute();
        }
    }
}
