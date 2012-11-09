package de.cubeisland.cubeengine.core.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used declare a field to be loaded by the configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Option
{
    /**
     * The path to save this Field in a config
     *
     * @return the path
     */
    public String value();

    /**
     * Needed to deserialize Objects in Collections or Maps correctly
     *
     * @return the valueType
     */
    public Class<?> valueType() default Object.class;
    
    /**
     * Needed to deserialize Keys of Maps that are not Strings correctly
     *
     * @return the keyType
     */
    public Class<?> keyType() default String.class;
}