package de.cubeisland.cubeengine.core.persistence;

import de.cubeisland.cubeengine.core.persistence.database.Database;
import java.lang.reflect.Field;
import java.sql.SQLException;
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
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
            .append(this.database.prefix(this.model.getSimpleName().toLowerCase(Locale.ENGLISH)))
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
        
        Entity entity = this.model.getAnnotation(Entity.class);
        query.append(") ENGINE=")
            .append(entity.engine())
            .append(" DEFAULT CHARSET=")
            .append(entity.charset())
            .append(" AUTO_INCREMENT=1;");
        
        this.database.execute(query.toString());
    }
}
