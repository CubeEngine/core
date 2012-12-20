package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.TripletKeyEntity;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.TableBuilder;
import de.cubeisland.cubeengine.core.util.Callback;
import de.cubeisland.cubeengine.core.util.Triplet;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        TableBuilder tbuilder = builder.createTable(this.tableName, true).beginFields();
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
                tbuilder.enumField(dbName, list.toArray(new String[list.size()]), attribute.notnull());
            }
            else
            {
                tbuilder.field(dbName, attribute.type(), attribute.unsigned(), attribute.length(), attribute.notnull());
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
                    case FOREIGN_KEY:
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
        tbuilder.primaryKey(this.f_dbKey, this.s_dbKey, this.t_dbKey).endFields();

        tbuilder.engine(this.storageType.engine()).defaultcharset(this.storageType.charset());
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
                    builder.delete().from(this.tableName).where().
                    field(this.f_dbKey).isEqual().value().and().
                    field(this.s_dbKey).isEqual().value().and().
                    field(this.t_dbKey).isEqual().value().limit(1).end().end());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while preparing statements for " + this.tableName, ex);
        }
    }

    @Override
    public M get(Triplet<Key_f, Key_s, Key_t> key)
    {
        M loadedModel = null;
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(this.modelClass, "get", key.getFirst(), key.getSecond(), key.getThird());
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
}
