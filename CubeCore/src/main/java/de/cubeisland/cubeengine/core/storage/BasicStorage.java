package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.storage.database.OrderedBuilder;
import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.TableBuilder;
import de.cubeisland.cubeengine.core.util.StringUtils;
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
    protected final String TABLE;

    public BasicStorage(Database database, Class<V> model)
    {
        this.database = database;
        this.model = model;
        this.TABLE = this.database.prefix(this.model.getAnnotation(Entity.class).name());
        if (!model.isAnnotationPresent(Entity.class))
        {
            throw new IllegalArgumentException("Every model needs the Entity annotation!");
        }
    }

    public void initialize() throws SQLException
    {
        Entity entity = this.model.getAnnotation(Entity.class);
        QueryBuilder builder = this.database.buildQuery().initialize();

        final LinkedList<String> keys = new LinkedList<String>();
        final LinkedList<String> attributes = new LinkedList<String>();
        Attribute attribute;
        TableBuilder tbuilder = builder.createTable(TABLE, true).startFields();
        for (Field field : this.model.getFields())
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
        this.database.execute(tbuilder.endCreateTable().end());
        this.prepareStatements((String[])keys.toArray(),(String[])attributes.toArray(),TABLE);
    }

    private void prepareStatements(String[] keys, String[] attributes, String tablename)
    {
        try
        {
            String attr = StringUtils.implode(",", attributes);
            QueryBuilder builder = this.database.buildQuery();
            this.database.prepareAndStoreStatement(model, "get", 
                 builder.initialize()
                        .select().cols(keys[0],attr)
                        .from(tablename)
                          .beginWhere()
                          .col(keys[0]).op(OrderedBuilder.EQUAL).value()
                          .endWhere()
                        .end().end());
            this.database.prepareAndStoreStatement(model, "getall", 
                 builder.initialize()
                        .select().cols(keys[0],attr)
                        .from(tablename)
                        .end().end());
            this.database.prepareAndStoreStatement(model, "store", 
                 builder.initialize()
                        .insert().into(tablename)
                        .cols(attr)
                        .values(attr.length())
                        .end().end());

            this.database.prepareAndStoreStatement(model, "update", 
                 builder.initialize()
                        .update().tables(tablename)
                        .set(attr)
                          .beginWhere()
                          .col(keys[0]).op(OrderedBuilder.EQUAL).value()
                          .endWhere()
                        .end().end());

            this.database.prepareAndStoreStatement(model, "delete", 
                 builder.initialize()
                        .delete().from(tablename)
                          .beginWhere()
                          .col(keys[0]).op(OrderedBuilder.EQUAL).value()
                          .endWhere()
                        .limit(1)
                        .end().end());
            
            this.database.prepareAndStoreStatement(model, "clear", 
                 builder.initialize()
                        .delete().from(tablename)
                        .end().end());

            this.database.prepareAndStoreStatement(model, "merge", 
                 builder.initialize()
                        .insert().into(tablename)
                        .cols(keys[0]+attr)
                        .values(attr.length()+1)
                        .end()
                        .onDuplicateUpdate()
                        .values(attr)
                        .end().end());
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

    private String prepareSELECT(String[] select, String table, Integer limit, String... where)
    {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }
    
    public String getSELECT(String[] select, String table, Integer limit, String... where)
    {
        //(pre + "get", "SELECT id,name,language FROM {{users}} WHERE id=? LIMIT 1");
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(StringUtils.implode(",", select));
        sb.append(" FROM ").append(table);
        if (where.length != 0)
        {
            sb.append(" WHERE ");
            for (int i = 0; i < where.length; ++i)
            {
                where[i] += "=?";
            }
            sb.append(StringUtils.implode(",", where));
        }
        if (limit != null)
        {
            sb.append(" LIMIT ").append(limit);
        }
        return sb.toString();
    }
}
