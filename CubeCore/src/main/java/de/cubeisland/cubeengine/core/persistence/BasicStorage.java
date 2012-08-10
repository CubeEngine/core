package de.cubeisland.cubeengine.core.persistence;

import de.cubeisland.cubeengine.core.persistence.database.Database;
import java.lang.reflect.Field;
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

    public void initialize()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(this.database.prefix(this.model.getSimpleName().toLowerCase(Locale.ENGLISH))).append(" (");
        for (Field field : this.model.getFields())
        {
            if (field.isAnnotationPresent(Attribute.class))
            {
                Attribute attribute = field.getAnnotation(Attribute.class);
                Integer length;
                String name = field.getName();
                if (!"".equals(attribute.name()))
                {
                    name = attribute.name();
                }
                sb.append("`").append(name).append("`").append(" ");

                AttrType type = attribute.type();
                sb.append(type.getType());
                switch (type)
                {
                    case INT:
                        length = attribute.length();
                        sb.append("(").append(length).append(")");
                        if (attribute.unsigned())
                        {
                            sb.append(" unsigned");
                        }
                        break;
                    case VARCHAR:
                        length = attribute.length();
                        sb.append("(").append(length).append(")");
                        break;
                }
                if (attribute.notnull())
                {
                    sb.append(" NOT_NULL");
                }
                else
                {
                    sb.append(" NULL");
                }
                if (attribute.ai())
                {
                    sb.append(" AUTO_INCREMENT");
                }
                sb.append(",");
            }
        }
        sb.append("PRIMARY KEY (`id`)").append(") ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
    }
    //TODO this.exec ...
}
