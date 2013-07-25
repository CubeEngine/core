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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import de.cubeisland.engine.core.storage.database.Attribute;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.DatabaseConstructor;
import de.cubeisland.engine.core.storage.database.DatabaseUpdater;
import de.cubeisland.engine.core.storage.database.querybuilder.QueryBuilder;

import gnu.trove.map.hash.TIntObjectHashMap;

// TODO possibility to set an order to the fields but how? perhaps give int
// values -> then use TreeMap for fields but what with map.values is it sorted?

public abstract class AbstractStorage<K, M extends Model<K>, T> implements Storage<K, M>
{
    protected final Database database;
    protected static TableManager tableManager = null; //Init in TableManager.class TODO holy cow!!
    // ModelInfo:
    protected T storageType = null;
    protected String tableName = null; // Has to be initialized in the implementation!
    protected final Class<M> modelClass;
    protected Constructor<M> modelConstructor = null;
    protected Map<Field, String> fieldNames; // fieldName in database
    protected Map<String, Field> reverseFieldNames;
    protected Map<Field, Attribute> attributeAnnotations; // coresponding attribute-annotation
    protected String[] allFields;
    //Updaters:
    public final int revision;
    protected TIntObjectHashMap<DatabaseUpdater> updaters;
    private boolean initialized = false;

    @SuppressWarnings("unchecked")
    public AbstractStorage(Database database, Class<M> modelClass, Class<T> storageType, int revision)
    {
        this.database = database;
        this.modelClass = modelClass;
        this.revision = revision;
        // Get tableName from Annotation
        Annotation[] annotations = modelClass.getAnnotations();

        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType().isAssignableFrom(storageType))
            {
                this.storageType = (T)annotation;
                break;
            }
        }
        if (this.storageType == null)
        {
            throw new IllegalArgumentException("Every model needs an Entity annotation! Expected annotation: " + storageType.getName());
        }
        // Search DatabaseConstructor
        for (Constructor c : this.modelClass.getDeclaredConstructors())
        {
            if (c.isAnnotationPresent(DatabaseConstructor.class))
            {
                this.modelConstructor = c;
                c.setAccessible(true);
                break;
            }
        }
        // Init fields:        
        fieldNames = new LinkedHashMap<Field, String>();
        reverseFieldNames = new LinkedHashMap<String, Field>();
        attributeAnnotations = new LinkedHashMap<Field, Attribute>();
        Attribute attribute;
        for (Field field : this.modelClass.getFields())
        {
            attribute = field.getAnnotation(Attribute.class);
            if (attribute != null)
            {
                String name = attribute.name().isEmpty() ? field.getName() : attribute.name();
                this.reverseFieldNames.put(name, field);
                this.attributeAnnotations.put(field, attribute);
                this.fieldNames.put(field, name);
            }
        }
        this.allFields = this.fieldNames.values().toArray(new String[this.fieldNames.size()]);
        this.updaters = new TIntObjectHashMap<DatabaseUpdater>();
    }

    @Override
    public void initialize()
    {
        if (tableName == null)
        {
            throw new IllegalStateException("TableName not initialized in constructor! Use \"this.storageType\" to acess the entity annotation!");
        }
        if (this.initialized)
        {
            return;
        }
        else
        {
            this.initialized = true;
        }
        this.updateStructure();
        try
        {
            this.prepareStatements();
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while preparing statements for the table "+ this.tableName, ex);
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

    @Override
    public void updateStructure()
    {
        if (tableManager == null) //Tablemanager is null when creating TableManager
        {
            return;
        }
        try
        {
            int dbRevision = tableManager.getRevision(this.tableName);
            DatabaseUpdater updater = this.updaters.get(dbRevision);
            if (updater != null)//No Updater for this
            {
                updater.update(this.database);
                tableManager.registerTable(this.tableName, this.revision);
            }
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("Error while updating DatabaseStructure", e);
        }
    }

    protected void prepareStatements() throws SQLException
    {
        QueryBuilder builder = this.database.getQueryBuilder();
        this.database.storeStatement(this.modelClass, "clear", builder.truncateTable(this.tableName).end());
        this.database.storeStatement(this.modelClass, "getall", builder.select().wildcard().from(this.tableName).end().end());
    }

    @Override
    public Collection<M> getAll()
    {
        Collection<M> loadedModels = new ArrayList<M>();
        try
        {
            ResultSet resultSet = this.database.preparedQuery(this.modelClass, "getall");
            while (resultSet.next())
            {
                loadedModels.add( this.createModel(resultSet));
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
    public M createModel(ResultSet resultSet) throws IllegalAccessException, InstantiationException, SQLException, InvocationTargetException
    {
        M loadedModel;
        if (modelConstructor == null)
        {
            loadedModel = this.modelClass.newInstance();
            for (Field field : this.fieldNames.keySet())
            {
                field.set(loadedModel, resultSet.getObject(this.fieldNames.get(field)));
            }
        }
        else
        {
            Map<String, Object> values = new LinkedHashMap<String, Object>();
            for (String name : this.reverseFieldNames.keySet())
            {
                values.put(name, resultSet.getObject(name));
            }
            loadedModel = this.modelConstructor.newInstance(values);
        }
        return loadedModel;
    }

    @Override
    public void merge(final M model)
    {
        this.merge(model, true);
    }

    @Override
    public void store(final M model)
    {
        this.store(model, true);
    }

    @Override
    public void delete(M model)
    {
        this.delete(model, true);
    }

    @Override
    public void update(final M model)
    {
        this.update(model, true);
    }

    @Override
    public void delete(M model, boolean async)
    {
        this.deleteByKey(model.getId(), async);
    }

    @Override
    public void deleteByKey(K key)
    {
        this.deleteByKey(key, true);
    }

    @Override
    public void clear()
    {
        try
        {
            this.database.preparedExecute(this.modelClass, "clear");
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while clearing Database", ex);
        }
    }
}
