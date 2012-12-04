package de.cubeisland.cubeengine.core.storage.database;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to define a Attribute in a model for saving in database.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Attribute
{
    /*
     * data_type [NOT NULL | NULL] [DEFAULT default_value]
      [AUTO_INCREMENT] [UNIQUE [KEY] | [PRIMARY] KEY]
      [COMMENT 'string']
      [COLUMN_FORMAT {FIXED|DYNAMIC|DEFAULT}]
      [STORAGE {DISK|MEMORY|DEFAULT}]
      [reference_definition]
     */
    
    public AttrType type();

    public int length() default -1;

    public boolean notnull() default true;

    public boolean unsigned() default false;//TODO maybe move to AttrType have a U_INT version etc

    /**
     * If not given will be set to fieldName
     */
    public String name() default "";
    
    /**
     * If true the default value will be set to the fields value
     */
    public boolean defaultIsValue() default false;
    
    //TODO later check annotation?
}
