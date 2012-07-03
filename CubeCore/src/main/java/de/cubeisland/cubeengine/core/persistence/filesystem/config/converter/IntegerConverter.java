package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

/**
 *
 * @author Faithcaio
 */
public class IntegerConverter implements Converter<Integer>
{
    public Object from(Integer object)
    {
        return object;
    }

    public Integer to(Object object)
    {
        Double t = Double.parseDouble(object.toString());
        return t.intValue();
    }
}
