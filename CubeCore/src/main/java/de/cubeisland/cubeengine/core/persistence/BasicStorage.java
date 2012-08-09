package de.cubeisland.cubeengine.core.persistence;

import de.cubeisland.cubeengine.core.persistence.testingdbstuff.RandomStorage;
import java.lang.reflect.Field;

/**
 *
 * @author Anselm Brehme
 */
public abstract class BasicStorage<K, V extends Model<K>> implements Storage<K, V>
{
    private String QUOTE = "`";
    protected Class<V> model;

    public BasicStorage(Class<V> model)
    {
        this.model = model;
    }

    public void initialize()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `{{").append(model.getName()).append("}}` (");
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
                sb.append(QUOTE).append(name).append(QUOTE).append(" ");

                AttrType type = attribute.type();
                sb.append(type.type);
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
                    case CUSTOM:
                        sb.append(attribute.customtype());
                        //TODO length etc..
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
                if (attribute.autoinc())
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
