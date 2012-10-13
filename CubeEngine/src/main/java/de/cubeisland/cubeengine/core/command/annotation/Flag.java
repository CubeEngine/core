package de.cubeisland.cubeengine.core.command.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Phillip Schichtel
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Flag
{
    public String name();

    public String longName() default "";
}