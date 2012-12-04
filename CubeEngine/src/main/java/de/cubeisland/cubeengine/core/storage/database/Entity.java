package de.cubeisland.cubeengine.core.storage.database;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the tableName and/or engine/defaultCharset for a Model.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented 
public @interface Entity
{
    public String name();

    public String engine() default "InnoDB";

    public String charset() default "utf8";
    
    public CompositeKey[] compositeKeys() default {};
}
