package de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Faithcaio
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SectionComment
{
    /**
     * Needed to add a Comment at given path
     * 
     * @return the path
     */
    public String path();
    /**
     * Needed to add a Comment at given path
     * 
     * @return the comment
     */
    public String text();
}
