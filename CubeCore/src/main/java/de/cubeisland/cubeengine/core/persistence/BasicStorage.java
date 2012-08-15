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
public abstract class BasicStorage<V> implements Storage<V>
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
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
            .append(this.database.prefix(entity.name()))
            .append(" (");

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

        query.append(") ENGINE=")
            .append(entity.engine())
            .append(" DEFAULT CHARSET=")
            .append(entity.charset())
            .append(" AUTO_INCREMENT=1;");

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
            this.database.prepareAndStoreStatement(model, "get", "");
            this.database.prepareAndStoreStatement(model, "getall", "");
            this.database.prepareAndStoreStatement(model, "store", "");//TODO remove first (id)
            this.database.prepareAndStoreStatement(model, "update", "");

            this.database.prepareAndStoreStatement(model, "delete", "");
            this.database.prepareAndStoreStatement(model, "clear", "");
            //TODO convert

            this.database.prepareAndStoreStatement(model, "merge", "INSERT INTO {{users}} (name,language) VALUES (?,?) ON DUPLICATE KEY UPDATE language=values(language)");

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
}
