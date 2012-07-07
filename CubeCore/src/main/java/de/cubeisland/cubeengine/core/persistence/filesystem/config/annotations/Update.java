package de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.AbstractUpdater;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Faithcaio
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Update
{
    Class<? extends AbstractUpdater> value();
}
