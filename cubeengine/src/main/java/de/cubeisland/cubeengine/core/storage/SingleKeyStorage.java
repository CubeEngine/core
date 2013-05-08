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
package de.cubeisland.cubeengine.core.storage;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.TableBuilder;

/**
 * Storage-Implementation for single Integer-Key-Models
 */
public class SingleKeyStorage<Key_f, M extends Model<Key_f>> extends AbstractStorage<Key_f, M, SingleKeyEntity>
{
    protected String dbKey = null;
    protected boolean keyIsAi = false;
    private boolean storeAsync = false;

    public SingleKeyStorage(Database database, Class<M> model, int revision)
    {
        super(database, model, SingleKeyEntity.class, revision);
        this.tableName = this.storageType.tableName();
        this.dbKey = this.storageType.primaryKey();
        this.keyIsAi = this.storageType.autoIncrement();
    }

    @Override
    public void initialize()
    {
        super.initialize();
        //Fields:
        QueryBuilder builder = this.database.getQueryBuilder();
        TableBuilder tableBuilder = builder.createTable(this.tableName, true).beginFields();
        for (Field field : this.reverseFieldNames.values())
        {
            Attribute attribute = this.attributeAnnotations.get(field);
            String dbName = this.fieldNames.get(field);
            if (this.dbKey.equals(dbName))
            {
                tableBuilder.field(dbName, attribute.type(), attribute.unsigned(), attribute.length(), attribute.notnull());
                if (this.keyIsAi)
                {
                    tableBuilder.autoIncrement();
                }
            }
            else
            {
                if (attribute.type().equals(AttrType.ENUM))
                {
                    if (!field.getType().isEnum())
                    {
                        throw new IllegalArgumentException("The field " + field.getName() + " is not an enum!");
                    }
                    Field[] enumConst = field.getClass().getEnumConstants();
                    List<String> list = new ArrayList<String>();
                    for (Field f : enumConst)
                    {
                        list.add(field.getName());
                    }
                    tableBuilder.enumField(dbName, list.toArray(new String[list.size()]), attribute.notnull());
                }
                else
                {
                    tableBuilder.field(dbName, attribute.type(), attribute.unsigned(), attribute.length(), attribute.notnull());
                }
            }
            if (attribute.defaultIsValue())
            {
                try
                {
                    M model = this.modelClass.newInstance();
                    tableBuilder.defaultValue(field.get(model).toString());
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException("Default value is not set OR Default-Constructor is not accessible.");
                }
            }

        }
        for (Index index : this.storageType.indices())
        {
            for (String indexField : index.fields())
            {
                if (!this.fieldNames.containsValue(indexField))
                {
                    throw new IllegalStateException("Cannot create Index! Field " + indexField + " not found!");
                }
            }
            switch (index.value())
            {
                case FOREIGN_KEY:
                    tableBuilder.foreignKey(index.fields()).references(index.f_table(), index.f_field()).onDelete(index.onDelete());
                    break;
                case UNIQUE:
                    tableBuilder.unique(index.fields());
                    break;
                case INDEX:
                    tableBuilder.index(index.fields());
            }
        }

        tableBuilder.primaryKey(this.dbKey).endFields();

        tableBuilder.engine(this.storageType.engine()).defaultcharset(this.storageType.charset());
        if (this.keyIsAi)
        {
            tableBuilder.autoIncrement(1);
        }
        try
        {
            this.database.execute(tableBuilder.end().end());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while creating Table", ex);
        }
        try
        {
            this.prepareStatements();
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while preparing statements for the table "+ this.tableName, ex);
        }
        tableManager.registerTable(this.tableName, this.revision);
    }

    /**
     * Prepares the Default-Statements
     */
    @Override
    protected void prepareStatements() throws SQLException
    {
        super.prepareStatements();
        String[] fields = new String[this.fieldNames.size() - 1];
        int i = 0;
        for (String fieldName : this.fieldNames.values())
        {
            if (!fieldName.equals(this.dbKey))
            {
                fields[i++] = fieldName;
            }
        }
        QueryBuilder builder = this.database.getQueryBuilder();
        if (this.keyIsAi)
        {
            builder.insert()
                   .into(this.tableName)
                   .cols(fields)
                   .end();
        }
        else
        {
            builder.insert()
                   .into(this.tableName)
                   .cols(this.allFields)
                   .end();
        }
        this.database.storeStatement(this.modelClass, "store", builder.end());

        this.database.storeStatement(this.modelClass, "merge", builder.merge().into(this.tableName).cols(this.allFields).updateCols(fields).end().end());

        this.database.storeStatement(this.modelClass, "get", builder.select(this.allFields).from(this.tableName).where().field(this.dbKey).isEqual().value().end().end());

        this.database.storeStatement(this.modelClass, "update", builder.update(this.tableName).set(fields).where().field(this.dbKey).isEqual().value().end().end());

        this.database.storeStatement(this.modelClass, "delete", builder.deleteFrom(this.tableName).where().field(this.dbKey).isEqual().value().limit(1).end().end());
    }

    @Override
    public M get(Key_f key)
    {
        M loadedModel = null;
        try
        {
            ResultSet resultSet = this.database.preparedQuery(this.modelClass, "get", key);
            if (resultSet.next())
            {
                if (this.modelConstructor == null)
                {
                    loadedModel = this.modelClass.newInstance();
                    for (Field field : this.fieldNames.keySet())
                    {
                        field.set(loadedModel, resultSet.getObject(this.fieldNames.get(field)));
                    }
                }
                else
                {
                    ArrayList<Object> values = new ArrayList<Object>();
                    for (String name : this.reverseFieldNames.keySet())
                    {
                        values.add(resultSet.getObject(name));
                    }
                    loadedModel = this.modelConstructor.newInstance(values);
                }
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while getting Model from Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
        return loadedModel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void store(final M model, boolean async)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.reverseFieldNames.keySet())
            {
                if (!name.equals(this.dbKey) || !this.keyIsAi)
                {
                    values.add(this.reverseFieldNames.get(name).get(model));
                }
            }
            if (this.keyIsAi && !storeAsync)
            {
                // This is never async
                model.setId((Key_f)this.database.getLastInsertedId(this.modelClass, "store", values.toArray()));
            }
            else
            {
                if (async)
                {
                    this.database.asyncPreparedExecute(this.modelClass, "store", values.toArray());
                }
                else
                {
                    this.database.preparedExecute(this.modelClass, "store", values.toArray());
                }
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while storing Model into Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while reading Model to store", ex);
        }
    }

    @Override
    public void update(M model, boolean async)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.reverseFieldNames.keySet())
            {
                if (!name.equals(this.dbKey))
                {
                    values.add(this.reverseFieldNames.get(name).get(model));
                }
            }
            values.add(this.reverseFieldNames.get(this.dbKey).get(model));
            if (async)
            {
                this.database.asyncPreparedExecute(this.modelClass, "update", values.toArray());
            }
            else
            {
                this.database.preparedExecute(this.modelClass, "update", values.toArray());
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("An SQL-Error occurred while updating the Model", ex,this.database.getStoredStatement(modelClass,"update"));
        }
        catch (Exception ex)
        {
            throw new StorageException("An unknown error occurred while updating the Model", ex);
        }
    }

    @Override
    public void merge(M model, boolean async)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.allFields)
            {
                values.add(this.reverseFieldNames.get(name).get(model));
            }
            if (async)
            {
                this.database.asyncPreparedExecute(this.modelClass, "merge", values.toArray());
            }
            else
            {
                this.database.preparedExecute(this.modelClass, "merge", values.toArray());
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("An SQL-Error occurred while merging the model", ex,this.database.getStoredStatement(modelClass,"merge"));
        }
        catch (Exception ex)
        {
            throw new StorageException("An unknown error occurred while merging a model", ex);
        }
    }

    @Override
    public void deleteByKey(Key_f key, boolean async)
    {
        try
        {
            if (async)
            {
                this.database.asyncPreparedExecute(this.modelClass, "delete", key);
            }
            else
            {
                this.database.preparedExecute(this.modelClass, "delete", key);
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while deleting from Database", ex);
        }
    }

    public void doStoreAsync()
    {
        this.storeAsync = true;
    }
}
