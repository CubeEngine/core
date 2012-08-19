package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.FunctionBuilder;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.TableBuilder;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Anselm Brehme
 */
public abstract class BasicStorage<V> implements Storage<V>
{
    protected final Database database;
    protected final Class<V> model;
    protected final String table;

    public BasicStorage(Database database, Class<V> model)
    {
        Entity entity = model.getAnnotation(Entity.class);
        if (entity == null)
        {
            throw new IllegalArgumentException("Every model needs the Entity annotation!");
        }
        this.table = entity.name();
        this.database = database;
        this.model = model;
    }

    public void initialize() throws SQLException
    {
        Entity entity = this.model.getAnnotation(Entity.class);
        QueryBuilder builder = this.database.getQueryBuilder();

        final LinkedList<String> keys = new LinkedList<String>();
        final LinkedList<String> attributes = new LinkedList<String>();
        Attribute attribute;
        TableBuilder tbuilder = builder.createTable(this.table, true).beginFields();
        for (Field field : this.model.getDeclaredFields())
        {
            attribute = field.getAnnotation(Attribute.class);
            if (attribute != null)
            {
                String name = field.getName();
                if (!"".equals(attribute.name()))
                {
                    name = attribute.name();
                }
                tbuilder.field(name, attribute.type(), attribute.length(), attribute.notnull(), attribute.unsigned(), attribute.ai());
                if (field.isAnnotationPresent(Key.class))
                {
                    keys.add(name);
                }
                else
                {
                    attributes.add(name);
                }
            }
        }

        if (keys.isEmpty())
        {
            throw new IllegalArgumentException("The given model does not declare any keys!");
        }
        else if (keys.size() > 1)
        {
            throw new IllegalArgumentException("The given model has declared too much keys! Use MultiKeyStorage");//TODO implement the MultiKeyStorage
        }
       
        Iterator<String> keyIter = keys.iterator();
        while (keyIter.hasNext())
        {
            tbuilder.primaryKey(keyIter.next());
        }
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
        tbuilder.endFields()
                .engine(entity.engine()).defaultcharset(entity.charset())
                //TODO AI if no ai
                .autoIncrement(1);
        this.database.execute(tbuilder.endBuilder().endQuery());
        this.prepareStatements(keys.get(0), attributes.toArray(new String[attributes.size()]));
    }

    private void prepareStatements(String key, String[] fields)
    {
        try
        {
            String[] allFields = new String[fields.length + 1];
            allFields[0] = key;
            System.arraycopy(fields, 0, allFields, 1, fields.length);
            QueryBuilder builder = this.database.getQueryBuilder();
            
            this.database.prepareAndStoreStatement(model, "store", builder
                .insert()
                    .into(this.table)
                    .cols(fields)
                .endBuilder()
            .endQuery());

            this.database.prepareAndStoreStatement(model, "merge", builder
                .merge()
                    .into(this.table)
                    .cols(allFields)
                    .updateCols(fields)
                .endBuilder()
            .endQuery());
            
            this.database.prepareAndStoreStatement(model, "get", builder
                .select().cols(allFields)
                    .from(this.table)
                    .beginFunctions().where()
                         .field(key).is(FunctionBuilder.EQUAL).value()
                    .endFunctions()
                .endBuilder()
            .endQuery());
            
            this.database.prepareAndStoreStatement(model, "getall", builder
                .select().cols(allFields)
                    .from(this.table)
                .endBuilder()
            .endQuery());

            this.database.prepareAndStoreStatement(model, "update", builder
                .update().tables(this.table)
                    .cols(fields)
                    .beginFunctions().where()
                        .field(key).is(FunctionBuilder.EQUAL).value()
                    .endFunctions()
                .endBuilder()
            .endQuery());

            this.database.prepareAndStoreStatement(model, "delete", builder
                .delete()
                    .from(this.table)
                    .beginFunctions().where()
                        .field(key).is(FunctionBuilder.EQUAL).value()
                    .endFunctions()
                    .limit(1)
                .endBuilder()
            .endQuery());
            
            this.database.prepareAndStoreStatement(model, "clear", builder
                .clearTable(this.table)
            .endQuery());
            
            
            /*
            
            this.database.prepareAndStoreStatement(model, "get", "SELECT " + keys[0] + "," + attr
                + " FROM " + tablename + " WHERE " + keys[0] + "=?");
            this.database.prepareAndStoreStatement(model, "getall", "SELECT " + keys[0] + "," + attr
                + " FROM " + tablename);
            this.database.prepareAndStoreStatement(model, "store", "INSERT INTO " + tablename + " (" + attr + ") "
                + "VALUES (?" + StringUtils.repeat(",?", attributes.length - 1) + ")");
            this.database.prepareAndStoreStatement(model, "update", "UPDATE " + tablename + " SET " + StringUtils.implode("=?,", attributes)
                + "=? WHERE " + keys[0] + "=?");
            this.database.prepareAndStoreStatement(model, "delete", "DELETE FROM " + tablename + " WHERE " + keys[0] + "=? LIMIT 1");
            this.database.prepareAndStoreStatement(model, "clear", "DELETE FROM " + tablename);
            
            StringBuilder mergevalues = new StringBuilder();
            for (String attribute : attributes)
            {
                mergevalues.append(",").append(attribute).append("=values(").append(attribute).append(")");
            }
            this.database.prepareAndStoreStatement(model, "merge", "INSERT INTO " + tablename + " (" + keys[0] + "," + attr + ")"
                + " VALUES (?" + StringUtils.repeat(",?", attributes.length - 1) + ")"
                + " ON DUPLICATE KEY UPDATE " + keys[0] + "=values(" + keys[0] + ")" + mergevalues);
                
            */
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
}