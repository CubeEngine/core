package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.storage.database.mysql.MySQLDatabase;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Anselm Brehme
 */
public abstract class BasicStorage<V> implements Storage<V>
{
    private final MySQLDatabase database;
    private final Class<V> model;

    public BasicStorage(MySQLDatabase database, Class<V> model)
    {
        this.database = database;
        this.model = model;
        if (!model.isAnnotationPresent(Entity.class))
        {
            throw new IllegalArgumentException("Every model needs the Entity annotation!");
        }
    }

    public void initialize() throws SQLException
    {
        Entity entity = this.model.getAnnotation(Entity.class);
        String tablename = this.database.prefix(entity.name());
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
            .append(tablename);
        final LinkedList<String> keys = new LinkedList<String>();
        final LinkedList<String> attributes = new LinkedList<String>();
        Attribute attribute;
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
                query.append(this.database.quote(name))
                    .append(" ");

                AttrType type = attribute.type();
                query.append(type.getType());
                if (type.hasLength())
                {
                    query.append("(").append(attribute.length()).append(")");
                }
                if (type.canBeSigned() && attribute.unsigned())
                {
                    query.append(" UNSIGNED");
                }
                if (attribute.notnull())
                {
                    query.append(" NOT NULL");
                }
                else
                {
                    query.append(" NULL");
                }
                if (attribute.ai())
                {
                    query.append(" AUTO_INCREMENT");
                }
                query.append(",");

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

        query.append("PRIMARY KEY (");
        Iterator<String> keyIter = keys.iterator();
        query.append(this.database.quote(keyIter.next()));
        while (keyIter.hasNext())
        {
            query.append(this.database.quote(keyIter.next()));
        }
        query.append(")");
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
        query.append(") ENGINE=").append(entity.engine()).append(" DEFAULT CHARSET=").append(entity.charset()).append(" AUTO_INCREMENT=1;");

        this.database.execute(query.toString());
        this.prepareStatements((String[])keys.toArray(),(String[])attributes.toArray(),tablename);
    }

    private void prepareStatements(String[] keys, String[] attributes, String tablename)
    {
        try
        {
            String attr = StringUtils.implode(",", attributes);
            //this.database.prepareAndStoreStatement(model, "get", this.prepareSELECT(attributes, tablename, 1 , keys[0]));
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
