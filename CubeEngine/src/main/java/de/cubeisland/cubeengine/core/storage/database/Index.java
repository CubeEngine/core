/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.core.storage.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a field as a ForeignKey for the database.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Index
{
    public IndexType value();

    public String[] fields();    
    
    /**
     * Needed for FOREIGN_KEY
     */
    public String f_table() default "";

    /**
     * Needed for FOREIGN_KEY
     */
    public String[] f_field() default {};

    /**
     * Needed for FOREIGN_KEY.
     * default: CASCADE. 
     * other: SET NULL; RESTRICT; NO ACTION
     *
     * @return the delete action
     */
    public String onDelete() default "CASCADE";

    public static enum IndexType
    {
        FOREIGN_KEY,
        UNIQUE,
        INDEX
    }
}
