package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.*;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.TableBuilder;
import de.cubeisland.cubeengine.core.util.Callback;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * BasicStorage (1 Key only)
 *
 * @author Anselm Brehme
 */
public class BasicStorage<V extends Model> implements Storage<V>
{
    protected static TableManager tableManager = null;//Init in TableManager.class
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

    @Override
    public void initialize()
    {
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
        for (Field field : this.modelClass.getDeclaredFields())
        {
            attribute = field.getAnnotation(Attribute.class);
            if (attribute != null)
            {
                String name = field.getName();
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
            }
        }

        if (key == null)
        {
            throw new IllegalArgumentException("The given model does not declare a keys!");
        }
        tbuilder.primaryKey(key).endFields();

        //TODO foreign keys
        //        if (!foreignKey.isEmpty())
//        {
//            for (Field field : foreignKey)
//            {
//                Relation relat = field.getAnnotation(Relation.class);
//                query.append(", FOREIGN KEY (").append(this.database.quote(field.getName()));
//                //query.append(") REFERENCES ").append(this.database.prefix(relat.model().getAnnotation(Entity.class).name()));
//                query.append("(").append(relat.field()).append(")");
//            }
//        }
        tbuilder
            .engine(entity.engine()).defaultcharset(entity.charset());
        if (keyIsAI)
        {
            tbuilder.autoIncrement(1);
        }
        try
        {
            this.database.execute(tbuilder.end().end());
            this.prepareStatements(key, attributes.toArray(new String[attributes.size()]));
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while initializing Database", ex);
        }
        tableManager.registerTable(this.table, this.revision);
    }

    protected void prepareStatements(String key, String[] fields) throws SQLException
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
            .cols(fields)
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
            .clearTable(this.table)
            .end());
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
            ArrayList<Object> values = new ArrayList<Object>();
            if (resulsSet.next())
            {
                values.add(resulsSet.getObject(this.key));
                for (String name : this.attributes)
                {
                    values.add(resulsSet.getObject(name));
                }
            }
            loadedModel = this.modelConstructor.newInstance(values);
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
    public void store(V model)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            if (!keyIsAI)
            {
                values.add(Convert.toObject(modelClass.getDeclaredField(key).get(model)));
            }
            for (String name : this.attributes)
            {
                values.add(Convert.toObject(modelClass.getDeclaredField(name).get(model)));
            }
            if (keyIsAI)
            {
                model.setKey(this.database.getLastInsertedId(modelClass, "store", values.toArray()));
            }
            else
            {
                this.database.preparedExecute(modelClass, "store", values.toArray());
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
    public void update(V model)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            for (String name : this.attributes)
            {
                values.add(Convert.toObject(modelClass.getDeclaredField(name).get(model)));
            }
            values.add(Convert.toObject(modelClass.getDeclaredField(key).get(model)));
            this.database.preparedExecute(modelClass, "update", values.toArray());

            for (Callback cb : updateCallbacks)
            {
                cb.call(model.getKey());
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while updating Model in Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while reading Model to update", ex);
        }
    }

    @Override
    public void merge(V model)
    {
        try
        {
            ArrayList<Object> values = new ArrayList<Object>();
            values.add(Convert.toObject(modelClass.getDeclaredField(key).get(model)));
            for (String name : this.attributes)
            {
                values.add(Convert.toObject(modelClass.getDeclaredField(name).get(model)));
            }
            this.database.preparedExecute(modelClass, "merge", values.toArray());

            for (Callback cb : updateCallbacks)
            {
                cb.call(model.getKey());
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while updating Model in Database", ex);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while reading Model to update", ex);
        }
    }

    @Override
    public void delete(V model)
    {
        this.deleteByKey(model.getKey());
    }

    @Override
    public void deleteByKey(Object key)
    {
        try
        {
            this.database.preparedExecute(modelClass, "delete", key);

            for (Callback cb : createCallbacks)
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
        int dbRevision = tableManager.getRevision(this.table);
        DatabaseUpdater updater = this.updaters.get(dbRevision);
        if (updater != null)//No Updater for this
        {
            updater.update(database);
            tableManager.registerTable(this.table, this.revision);
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
}