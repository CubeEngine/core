package de.cubeisland.cubeengine.core.storage.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the tableName and/or engine/defaultCharset for a two-key model.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TripletKeyEntity
{
    public String tableName();

    public String engine() default "InnoDB";

    public String charset() default "utf8";

    public String firstPrimaryKey();

    public String secondPrimaryKey();

    public String thirdPrimaryKey();
    
    public Index[] indices() default {};
}
