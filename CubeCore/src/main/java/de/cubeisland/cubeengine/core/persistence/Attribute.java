package de.cubeisland.cubeengine.core.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Attribute
{
    public String name() default "";
    public AttrType type();
    public int length() default -1;
    public boolean notnull() default true;
    public boolean autoinc() default false;
    public boolean unsigned() default false;
    public String customtype() default "";
    
}