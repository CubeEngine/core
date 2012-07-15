package de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Anselm Brehme
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Comment
{
    /**
     * Adds a Comment to this field
     * This Annotation does nothing without the Option Annotation
     * 
     * @return the comment
     */
    public String value();
}
