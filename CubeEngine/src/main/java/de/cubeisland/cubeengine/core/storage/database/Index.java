/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.core.storage.database;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a field as a ForeignKey for the database.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Index
{
    public IndexType value();

    /**
     * Needed for FOREIGNKEY
     */
    public String f_table() default "";

    /**
     * Needed for FOREIGNKEY
     */
    public String f_field() default "";

    /**
     * Needed for FOREIGNKEY.
     * default: CASCADE. 
     * other: SET NULL; RESTRICT; NO ACTION
     *
     * @return
     */
    public String onDelete() default "CASCADE";

    public static enum IndexType
    {
        FOREIGNKEY,
        UNIQUE,
        INDEX;
    }
}
