package de.cubeisland.cubeengine.core.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Anselm Brehme
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Relation
{
    public Class<? extends Model> model();
    public String field() default "id";
}
