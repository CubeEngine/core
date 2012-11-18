package de.cubeisland.cubeengine.core.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to add a comment to a value in a configuration
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Comment
{
    /**
     * Adds a Comment to this field This Annotation does nothing without the
     * Option Annotation
     *
     * @return the comment
     */
    public String value();
}
