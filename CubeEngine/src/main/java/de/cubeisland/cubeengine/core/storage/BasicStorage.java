package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.*;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.TableBuilder;
import de.cubeisland.cubeengine.core.util.Callback;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Basic Storage-implementation (1 Key only)
 */
public class BasicStorage<V extends Model> implements Storage<V>
{
    protected static TableManager tableManager = null; //Init in TableManager.class
    protected final Database database;
    protected final Class<V> modelClass;
    protected Constructor<V> modelConstructor = null;
    protected final String table;
    protected Collection<Callback> createCallbacks = new ArrayList<Callback>();
    protected Collection<Callback> deleteCallbacks = new ArrayList<Callback>();
    protected Collection<Callback> updateCallbacks = new ArrayList<Callback>();
    protected String key = null;
    protected boolean keyIsAI = false;
    protected ArrayList<String> attributes;
    protected TIntObjectHashMap<DatabaseUpdater> updaters;
    private int revision;
    private boolean initialized = false;

    public BasicStorage(Database database, Class<V> model, int revision)
    {
        Entity entity = model.getAnnotation(Entity.class);
        if (entity == null)
        {
            throw new IllegalArgumentException("Every model needs the Entity annotation!");
        }
        this.table = entity.name();
        this.database = database;
        this.modelClass = model;
        this.attributes = new ArrayList<String>();
        this.revision = revision;
        this.updaters = new TIntObjectHashMap<DatabaseUpdater>();
    }

    protected void initialize()
    {
        if (this.initialized)
        {
            return;
        }
        else
        {
            this.initialized = true;
        }
        //Constructor:
        for (Constructor c : this.modelClass.getConstructors())
        {
            if (c.isAnnotationPresent(DatabaseConstructor.class))
            {
                this.modelConstructor = c;
            }
        }
        if (this.modelConstructor == null)
        {
            throw new IllegalStateException("DatabaseConstructor Annotation is missing!");
        }
        //Fields:
        Entity entity = this.modelClass.getAnnotation(Entity.class);
        QueryBuilder builder = this.database.getQueryBuilder();

        this.updateStructure();

        Attribute attribute;
        TableBuilder tbuilder = builder.createTable(this.table, true).beginFields();
        for (Field field : this.modelClass.getFields())
        {
            attribute = field.getAnnotation(Attribute.class);
            if (attribute != null)
            {
                String name = attribute.name();
                if (name.isEmpty())
                {
                    name = field.getName();
                }                        
                tbuilder.field(name, attribute.type(), attribute.length(), attribute.notnull(), attribute.unsigned(), attribute.ai());
                if (field.isAnnotationPresent(Key.class))
                {
                    key = name;
                    this.keyIsAI = field.getAnnotation(Attribute.class).ai();
                }
                else
                {
                    attributes.add(name);
                }
                if (field.isAnnotationPresent(ForeignKey.class))
                {
                    ForeignKey fKey = field.getAnnotation(ForeignKey.class);
                    tbuilder.foreignKey(name).references(fKey.table(), fKey.field()).onDelete(fKey.onDelete());
                }
                if (attribute.unique())
                {
                    tbuilder.unique(name);
                }
            }
        }

        if (key == null)
        {
            throw new IllegalArgumentException("The given model does not declare a keys!");
        }
        tbuilder.primaryKey(key).endFields();

        tbuilder.engine(entity.engine()).defaultcharset(entity.charset());
        if (keyIsAI)
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
        this.prepareStatements(key, attributes.toArray(new String[attributes.size()]));
        tableManager.registerTable(this.table, this.revision);
    }

    /**
     * Prepares the Default-Statements
     *
     * @param key    the key
     * @param fields the fields
     */
    private void prepareStatements(String key, String[] fields)
    {
        try
        {
            String[] allFields = new String[fields.length + 1];
            allFields[0] = key;
            System.arraycopy(fields, 0, allFields, 1, fields.length);
            QueryBuilder builder = this.database.getQueryBuilder();

            if (this.keyIsAI)
            {
                builder.insert()
                    .into(this.table)
                    .cols(fields)
                    .end();
            }
            else
            {
                builder.insert()
                    .into(this.table)
                    .cols(allFields)
                    .end();
            }
            this.database.prepareAndStoreStatement(modelClass, "store", builder.end());

            this.database.prepareAndStoreStatement(modelClass, "merge", builder
                .merge()
                .into(this.table)
                .cols(allFields)
                .updateCols(fields)
                .end()
                .end());

            this.database.prepareAndStoreStatement(modelClass, "get", builder
                .select(allFields)
                .from(this.table)
                .where()
                .field(key).is(EQUAL).value()
                .end()
                .end());

            this.database.prepareAndStoreStatement(modelClass, "getall", builder
                .select(allFields)
                .from(this.table)
                .end()
                .end());

            this.database.prepareAndStoreStatement(modelClass, "update", builder
                .update(this.table)
                .set(fields)
                .where()
                .field(key).is(EQUAL).value()
                .end()
                .end());

            this.database.prepareAndStoreStatement(modelClass, "delete", builder
                .delete()
                .from(this.table)
                .where()
                .field(key).is(EQUAL).value()
                .limit(1)
                .end()
                .end());

            this.database.prepareAndStoreStatement(modelClass, "clear", builder
                .truncateTable(this.table)
                .end());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while preparing statements for " + this.table, ex);
        }
    }

    @Override
    public void subscribe(SubcribeType type, Callback callback)
    {
        switch (type)
        {
            case CREATE:
                this.createCallbacks.add(callback);
                break;
            case DELETE:
                this.deleteCallbacks.add(callback);
                break;
            case UPDATE:
                this.updateCallbacks.add(callback);
                break;
        }
    }

    @Override
    public V get(Object key)
    {
        V loadedModel = null;
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "get", key);
            if (resulsSet.next())
            {
                ArrayList<Object> values = new ArrayList<Object>();
                values.add(resulsSet.getObject(this.key));
                for (String name : this.attributes)
                {
                    values.add(resulsSet.getObject(name));
                }
                loadedModel = this.modelConstructor.newInstance(values);
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
    public Collection<V> getAll()
    {
        Collection<V> loadedModels = new ArrayList<V>();
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getall");

            while (resulsSet.next())
            {
                ArrayList<Object> values = new ArrayList<Object>();
                values.add(resulsSet.getObject(this.key));
                for (String name : this.attributes)
                {
                    values.add(resulsSet.getObject(name));
                }
                V loadedModel = this.modelConstructor.newInstance(values);
                loadedModels.add(loadedModel);
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
        return loadedModels;
    }

    @Override
    public void store(final V model)
    {
        this.store(model, true);
    }

    @Override
    public void store(final V model, boolean async)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            if (!keyIsAI)
            {
                values.add(modelClass.getField(key).get(model));
            }
            for (String name : this.attributes)
            {
                values.add(modelClass.getField(name).get(model));
            }
            if (keyIsAI)
            {
                // This is never async
                model.setKey(this.database.getLastInsertedId(modelClass, "store", values.toArray()));
            }
            else
            {
                if (async)
                {
                    this.database.asyncPreparedExecute(modelClass, "store", values.toArray());
                }
                else
                {
                    this.database.preparedExecute(modelClass, "store", values.toArray());
                }
            }
            for (Callback cb : createCallbacks)
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
    public void update(final V model)
    {
        this.update(model, true);
    }

    @Override
    public void update(V model, boolean async)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.attributes)
            {
                values.add(this.modelClass.getField(name).get(model));
            }
            values.add(this.modelClass.getField(this.key).get(model));
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
    public void merge(final V model)
    {
        this.merge(model, true);
    }

    @Override
    public void merge(V model, boolean async)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            values.add(this.modelClass.getField(this.key).get(model));
            for (String name : this.attributes)
            {
                values.add(this.modelClass.getField(name).get(model));
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
    public void delete(V model)
    {
        this.delete(model, true);
    }

    @Override
    public void delete(V model, boolean async)
    {
        this.deleteByKey(model.getKey(), async);
    }

    @Override
    public void deleteByKey(Object key)
    {
        this.deleteByKey(key, true);
    }

    @Override
    public void deleteByKey(Object key, boolean async)
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

    @Override
    public void clear()
    {
        try
        {
            this.database.preparedExecute(modelClass, "clear");
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while clearing Database", ex);
        }
    }

    @Override
    public void updateStructure()
    {
        if (tableManager == null) //Tablemanager is null when creating TableManager
        {
            return;
        }
        try
        {
            int dbRevision = tableManager.getRevision(this.table);
            DatabaseUpdater updater = this.updaters.get(dbRevision);
            if (updater != null)//No Updater for this
            {
                updater.update(database);
                tableManager.registerTable(this.table, this.revision);
            }
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Error while updating DatabaseStructure", e);
        }
    }

    @Override
    public void registerUpdater(DatabaseUpdater updater, int... fromRevision)
    {
        for (int i : fromRevision)
        {
            this.updaters.put(i, updater);
        }
    }

    public void notAssignKey()
    {
        this.keyIsAI = false;
        String[] allFields = new String[this.attributes.size() + 1];
        allFields[0] = this.key;
        System.arraycopy(this.attributes.toArray(), 0, allFields, 1, this.attributes.size());
        QueryBuilder builder = this.database.getQueryBuilder();
        builder.insert()
            .into(this.table)
            .cols(allFields)
            .end();
        try
        {
            this.database.prepareAndStoreStatement(modelClass, "store", builder.end());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error overriding store not to return key.", ex);
        }
    }
}
