package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleIntKeyEntity;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.TableBuilder;
import de.cubeisland.cubeengine.core.util.Callback;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Storage-Implementation for single Integer-Key-Models
 */
public class BasicStorage<M extends Model<Integer>> extends AbstractStorage<Integer, M, SingleIntKeyEntity>
{
    protected String dbKey = null;
    protected boolean keyIsAi = false;
    private boolean storeAsync = false;

    public BasicStorage(Database database, Class<M> model, int revision)
    {
        super(database, model, SingleIntKeyEntity.class, revision);
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
        TableBuilder tbuilder = builder.createTable(this.tableName, true).beginFields();
        for (Field field : this.reverseFieldNames.values())
        {
            Attribute attribute = this.attributeAnnotations.get(field);
            String dbName = this.fieldNames.get(field);
            if (this.dbKey.equals(dbName))
            {
                tbuilder.field(dbName, attribute.type(), attribute.unsigned(), attribute.length(), attribute.notnull());
                if (this.keyIsAi)
                {
                    tbuilder.autoIncrement();
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
                    tbuilder.enumField(dbName, list.toArray(new String[list.size()]), attribute.notnull());
                }
                else
                {
                    tbuilder.field(dbName, attribute.type(), attribute.unsigned(), attribute.length(), attribute.notnull());
                }
            }
            if (attribute.defaultIsValue())
            {
                try
                {
                    M model = this.modelClass.newInstance();
                    tbuilder.defaultValue(field.get(model).toString());
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException("Default value is not set OR Default-Constructor is not accessible.");
                }
            }
            if (this.indexAnnotations.get(field) != null)
            {
                Index index = this.indexAnnotations.get(field);
                switch (index.value())
                {
                    case FOREIGNKEY:
                        tbuilder.foreignKey(dbName).references(index.f_table(), index.f_field()).onDelete(index.onDelete());
                        break;
                    case UNIQUE:
                        tbuilder.unique(dbName);
                        break;
                    case INDEX:
                        tbuilder.index();
                }
            }
        }
        tbuilder.primaryKey(this.dbKey).endFields();

        tbuilder.engine(this.storageType.engine()).defaultcharset(this.storageType.charset());
        if (this.keyIsAi)
        {
            tbuilder.autoIncrement(1);
        }
        try
        {
            this.database.execute(tbuilder.end().end());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while creating Table", ex);
        }
        this.prepareStatements();
        tableManager.registerTable(this.tableName, this.revision);
    }

    /**
     * Prepares the Default-Statements
     */
    private void prepareStatements()
    {
        try
        {
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

            this.database.storeStatement(this.modelClass, "delete", builder.delete().from(this.tableName).where().field(this.dbKey).isEqual().value().limit(1).end().end());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while preparing statements for " + this.tableName, ex);
        }
    }

    @Override
    public M get(Integer key)
    {
        M loadedModel = null;
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(this.modelClass, "get", key);
            if (resulsSet.next())
            {
                if (this.modelConstructor == null)
                {
                    loadedModel = this.modelClass.newInstance();
                    for (Field field : this.fieldNames.keySet())
                    {
                        field.set(loadedModel, resulsSet.getObject(this.fieldNames.get(field)));
                    }
                }
                else
                {
                    ArrayList<Object> values = new ArrayList<Object>();
                    for (String name : this.reverseFieldNames.keySet())
                    {
                        values.add(resulsSet.getObject(name));
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
                model.setKey(this.database.getLastInsertedId(this.modelClass, "store", values.toArray()));
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
            for (Callback cb : this.createCallbacks)
            {
                cb.call(model.getKey());
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
            for (Callback cb : this.updateCallbacks)
            {
                cb.call(model.getKey());
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("An SQL related error occurred while updating the Model", ex);
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
            for (Callback cb : this.updateCallbacks)
            {
                cb.call(model.getKey());
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("An unknown error occurred while merging the Model", ex);
        }
        catch (Exception ex)
        {
            throw new StorageException("Error while reading Model to update", ex);
        }
    }

    @Override
    public void deleteByKey(Integer key, boolean async)
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
            for (Callback cb : this.deleteCallbacks)
            {
                cb.call(key);
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
