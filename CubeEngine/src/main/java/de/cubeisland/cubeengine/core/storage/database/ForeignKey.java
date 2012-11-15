package de.cubeisland.cubeengine.core.storage.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a field as a ForeignKey for the database.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ForeignKey
{
    public String table();

    public String field();

    /**
     * default: CASCADE.
     * other: SET NULL; RESTRICT; NO ACTION
     * 
     * @return 
     */
    public String onDelete() default "CASCADE";
}
