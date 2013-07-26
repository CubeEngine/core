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
package de.cubeisland.engine.core.storage;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.Index;
import de.cubeisland.engine.core.storage.database.TripletKeyEntity;
import de.cubeisland.engine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.engine.core.storage.database.querybuilder.TableBuilder;
import de.cubeisland.engine.core.util.Triplet;

public class TripletKeyStorage<Key_f, Key_s, Key_t, M extends TripletKeyModel<Key_f, Key_s, Key_t>> extends AbstractStorage<Triplet<Key_f, Key_s, Key_t>, M, TripletKeyEntity>
{
    protected Field key = null;
    protected String f_dbKey = null;
    protected String s_dbKey = null;
    protected String t_dbKey = null;

    public TripletKeyStorage(Database database, Class<M> model, int revision)
    {
        super(database, model, TripletKeyEntity.class, revision);
        this.tableName = this.storageType.tableName();
        this.f_dbKey = this.storageType.firstPrimaryKey();
        this.s_dbKey = this.storageType.secondPrimaryKey();
        this.t_dbKey = this.storageType.thirdPrimaryKey();
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
        tableBuilder.primaryKey(this.f_dbKey, this.s_dbKey, this.t_dbKey).endFields();

        tableBuilder.engine(this.storageType.engine()).defaultcharset(this.storageType.charset());
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
        String[] fields = new String[this.fieldNames.size() - 2];
        int i = 0;
        for (String fieldName : this.fieldNames.values())
        {
            if (!(fieldName.equals(this.f_dbKey) || fieldName.equals(this.s_dbKey)))
            {
                fields[i++] = fieldName;
            }
        }
        QueryBuilder builder = this.database.getQueryBuilder();

        this.database.storeStatement(this.modelClass, "store",
                                     builder.insert().into(this.tableName).cols(this.allFields).end().end());

        if (fields.length != 0)
        {
            this.database.storeStatement(this.modelClass, "merge",
                                         builder.merge().into(this.tableName).cols(this.allFields).updateCols(fields).end().end());
            this.database.storeStatement(this.modelClass, "update",
                                         builder.update(this.tableName).set(fields).where().
                                             field(this.f_dbKey).isEqual().value().and().
                                                    field(this.s_dbKey).isEqual().value().and().
                                                    field(this.t_dbKey).isEqual().value().end().end());
        }

        this.database.storeStatement(this.modelClass, "get",
                                     builder.select(allFields).from(this.tableName).where().
                                         field(this.f_dbKey).isEqual().value().and().
                                                field(this.s_dbKey).isEqual().value().and().
                                                field(this.t_dbKey).isEqual().value().end().end());

        this.database.storeStatement(this.modelClass, "delete",
                                     builder.deleteFrom(this.tableName).where().
                                         field(this.f_dbKey).isEqual().value().and().
                                                field(this.s_dbKey).isEqual().value().and().
                                                field(this.t_dbKey).isEqual().value().limit(1).end().end());
    }

    @Override
    public M get(Triplet<Key_f, Key_s, Key_t> key)
    {
        M loadedModel = null;
        try
        {
            ResultSet resultSet = this.database.preparedQuery(this.modelClass, "get", key.getFirst(), key.getSecond(), key.getThird());
            if (resultSet.next())
            {
                loadedModel = this.createModel(resultSet);
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
    public void store(final M model, boolean async)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.reverseFieldNames.keySet())
            {
                values.add(this.reverseFieldNames.get(name).get(model));
            }
            if (async)
            {
                this.database.asyncPreparedExecute(this.modelClass, "store", values.toArray());
            }
            else
            {
                this.database.preparedExecute(this.modelClass, "store", values.toArray());
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
        if (this.allFields.length <= 3)
        {
            throw new UnsupportedOperationException("Updating is not supported for only-key storages!");
        }
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.reverseFieldNames.keySet())
            {
                if (!name.equals(this.f_dbKey) || !name.equals(this.s_dbKey) || !name.equals(this.t_dbKey))
                {
                    values.add(this.reverseFieldNames.get(name).get(model));
                }
            }
            values.add(this.reverseFieldNames.get(this.f_dbKey).get(model));
            values.add(this.reverseFieldNames.get(this.s_dbKey).get(model));
            values.add(this.reverseFieldNames.get(this.t_dbKey).get(model));
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
            throw new StorageException("An SQL-Error occurred while updating the Model", ex,this.database.getStoredStatement(modelClass, "update"));
        }
        catch (Exception ex)
        {
            throw new StorageException("An unknown error occurred while updating the Model", ex);
        }
    }

    @Override
    public void merge(M model, boolean async)
    {
        if (this.allFields.length <= 3)
        {
            throw new UnsupportedOperationException("Merging is not supported for only-key storages!");
        }
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
            throw new StorageException("An SQL-Error occurred while merging the Model", ex,this.database.getStoredStatement(modelClass, "merge"));
        }
        catch (Exception ex)
        {
            throw new StorageException("An unknown error occurred while reading merginga a model", ex);
        }
    }

    @Override
    public void deleteByKey(Triplet<Key_f, Key_s, Key_t> key, boolean async)
    {
        try
        {
            if (async)
            {
                this.database.asyncPreparedExecute(this.modelClass, "delete", key.getFirst(), key.getSecond(), key.getThird());
            }
            else
            {
                this.database.preparedExecute(this.modelClass, "delete", key.getFirst(), key.getSecond(), key.getThird());
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while deleting from Database", ex);
        }
    }
}
