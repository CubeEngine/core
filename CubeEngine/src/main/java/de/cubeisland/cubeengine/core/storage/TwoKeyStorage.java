package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.TwoKeyEntity;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.TableBuilder;
import de.cubeisland.cubeengine.core.util.Callback;
import de.cubeisland.cubeengine.core.util.Pair;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TwoKeyStorage<Key_f, Key_s, M extends TwoKeyModel<Key_f, Key_s>> extends AbstractStorage<Pair<Key_f, Key_s>, M, TwoKeyEntity>
{
    protected Field key = null;
    protected String f_dbKey = null;
    protected String s_dbKey = null;

    public TwoKeyStorage(Database database, Class<M> model, int revision)
    {
        super(database, model, TwoKeyEntity.class, revision);
        this.tableName = this.storageType.tableName();
        this.f_dbKey = this.storageType.firstPrimaryKey();
        this.s_dbKey = this.storageType.secondPrimaryKey();
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
            tbuilder.field(dbName, attribute.type(), attribute.length(), attribute.notnull(), attribute.unsigned());
            //TODO default value
            //TODO enum
            //for enum i can get the possible enum from field.getType().getEnumConstants();
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
                    //TODO tbuilder.index(dbName)

                }
            }
        }
        tbuilder.primaryKey(this.f_dbKey, this.s_dbKey).endFields();

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

            this.database.storeStatement(this.modelClass, "merge",
                    builder.merge().into(this.tableName).cols(this.allFields).updateCols(fields).end().end());

            this.database.storeStatement(this.modelClass, "get",
                    builder.select(allFields).from(this.tableName).where().field(this.f_dbKey).isEqual().value().and().field(this.s_dbKey).isEqual().value().end().end());

            this.database.storeStatement(this.modelClass, "update",
                    builder.update(this.tableName).set(fields).where().field(this.f_dbKey).isEqual().value().and().field(this.s_dbKey).isEqual().value().end().end());

            this.database.storeStatement(this.modelClass, "delete",
                    builder.delete().from(this.tableName).where().field(this.f_dbKey).isEqual().value().and().field(this.s_dbKey).isEqual().value().limit(1).end().end());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while preparing statements for " + this.tableName, ex);
        }
    }

    @Override
    public M get(Pair<Key_f, Key_s> key)
    {
        M loadedModel = null;
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(this.modelClass, "get", key.getLeft(), key.getRight());
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
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.reverseFieldNames.keySet())
            {
                if (!name.equals(this.f_dbKey) || !name.equals(this.s_dbKey))
                {
                    values.add(this.reverseFieldNames.get(name).get(model));
                }
            }
            values.add(this.reverseFieldNames.get(this.f_dbKey).get(model));
            values.add(this.reverseFieldNames.get(this.s_dbKey).get(model));
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
    public void deleteByKey(Pair<Key_f, Key_s> key, boolean async)
    {
        try
        {
            if (async)
            {
                this.database.asyncPreparedExecute(this.modelClass, "delete", key.getLeft(), key.getRight());
            }
            else
            {
                this.database.preparedExecute(this.modelClass, "delete", key.getLeft(), key.getRight());
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
