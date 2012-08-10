package de.cubeisland.cubeengine.core.persistence;

import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.lang.reflect.Field;
import java.sql.SQLException;
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
    }

    public void initialize() throws SQLException
    {
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
            .append(this.database.prefix(this.model.getSimpleName().toLowerCase(Locale.ENGLISH)))
            .append(" (");
        
        final LinkedList<CharSequence> keys = new LinkedList<CharSequence>();
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
                query.append("`").append(name).append("`").append(" ");

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
                    query.append(" NOT_NULL"); // is there really an underscore?
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
        query.append("PRIMARY KEY (`")
            .append(StringUtils.implode("`,`", keys))
            .append("`) ENGINE=")
            .append("MyISAM") // make me configurable
            .append("DEFAULT CHARSET=")
            .append("latin1") // make me configurable
            .append(" AUTO_INCREMENT=1;");
        
        this.database.execute(query.toString());
    }
    //TODO this.exec ...
}
