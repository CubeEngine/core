package de.cubeisland.cubeengine.core.storage.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to define a Attribute in a model for saving in database.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Attribute
{
    public AttrType type();

    public int length() default -1;

    public boolean notnull() default true;

    public boolean ai() default false;

    public boolean unsigned() default false;

    public boolean unique() default false;
}
