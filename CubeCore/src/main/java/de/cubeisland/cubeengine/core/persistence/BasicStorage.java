package de.cubeisland.cubeengine.core.persistence;

import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author Anselm Brehme
 */
public abstract class BasicStorage<K, V extends Model<K>> implements Storage<K, V>
{
    private final Database database;
    private final Class<V> model;

    public BasicStorage(Database database, Class<V> model)
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
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(this.database.prefix(entity.name())).append(" (");
        ArrayList<Field> foreignKey = new ArrayList<Field>();

        final LinkedList<String> keys = new LinkedList<String>();
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
                query.append(this.database.quote(name)).append(" ");

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
                if (field.isAnnotationPresent(Relation.class))
                {
                    foreignKey.add(field);
                }
            }
        }

        if (keys.isEmpty())
        {
            throw new IllegalArgumentException("The given model does not declare any keys!");
        }

        query.append("PRIMARY KEY (");
        Iterator<String> keyIter = keys.iterator();
        query.append(this.database.quote(keyIter.next()));
        while (keyIter.hasNext())
        {
            query.append(this.database.quote(keyIter.next()));
        }
        query.append(")");
        if (!foreignKey.isEmpty())
        {
            for (Field field : foreignKey)
            {
                Relation relat = field.getAnnotation(Relation.class);
                query.append(", FOREIGN KEY (").append(this.database.quote(field.getName()));
                query.append(") REFERENCES ").append(this.database.prefix(relat.model().getAnnotation(Entity.class).name()));
                query.append("(").append(relat.field()).append(")");
            }
        }
        query.append(") ENGINE=").append(entity.engine()).append(" DEFAULT CHARSET=").append(entity.charset()).append(" AUTO_INCREMENT=1;");

        this.database.execute(query.toString());
        this.prepareStatements();
    }

    private void prepareStatements()
    {
        String primaryKey = "";
        ArrayList<String> fields = new ArrayList<String>();
        String[] attributes;

        // TODO all the reflection again? NO
        for (Field field : model.getFields())
        {
            if (field.isAnnotationPresent(Attribute.class))
            {
                if (field.isAnnotationPresent(Key.class))
                {
                    Key key = field.getAnnotation(Key.class);
                    if (key.primary())
                    {
                        primaryKey = field.getName(); //TODO handle if no primary Key found (or multiple)
                    }
                }
                else
                {
                    fields.add(field.getName());
                }
            }
        }
        attributes = (String[])fields.toArray();

        try
        {
            String pre = this.model.getSimpleName().toLowerCase(Locale.ENGLISH);

            // TODO I'll change the prepare methods to take a Class as the owner which
            // the queries are then stored in either Map<Class, Map<String, PreparedStatement>>
            // or Map<String, PreparedStatement>, where the key is prefixed by the class'es FQDN
            this.database.prepareAndStoreStatement(pre + "_get",
                this.getSELECT(attributes, pre, 1, primaryKey));
            this.database.prepareAndStoreStatement(pre + "getall",
                this.getSELECT(attributes, pre, null, (String)null));
            this.database.prepareAndStoreStatement(pre + "store",
                this.getINSERT_INTO(pre, attributes));//TODO remove first (id)
            this.database.prepareAndStoreStatement(pre + "update",
                this.getUPDATE(pre, attributes, primaryKey));

            this.database.prepareAndStoreStatement(pre + "delete",
                this.getDELETE(pre, 1, primaryKey));
            this.database.prepareAndStoreStatement(pre + "clear",
                this.getCLEAR(pre));
            //TODO convert

            this.database.prepareAndStoreStatement(pre + "merge", "INSERT INTO {{users}} (name,language) VALUES (?,?) ON DUPLICATE KEY UPDATE language=values(language)");

        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
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

    public String getINSERT_INTO(String table, String[] insert)
    {
        //"INSERT INTO {{users}} (name,flags,language) VALUES (?,?,?)");
        return "";
    }

    public String getUPDATE(String table, String[] set, String... where)
    {
        //"UPDATE {{users}} SET language=? WHERE id=?"
        return "";
    }

    public String getMERGE()//TODO on duplicate Key update
    {
        //"INSERT INTO {{users}} (name,language) VALUES (?,?) ON DUPLICATE KEY UPDATE language=values(language)"
        return "";
    }

    public String getDELETE(String table, Integer limit, String... where)
    {
        //"DELETE FROM {{users}} WHERE id=? LIMIT 1"
        return "";
    }

    public String getCLEAR(String table)
    {
        //"DELETE FROM {{users}}"
        return "";
    }

     enum QueryTypes
    {
        SELECT, INSERT, UPDATE, DELETE
    }
}
