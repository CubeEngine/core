package de.cubeisland.cubeengine.core.command.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is used to define flags in a command.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Flag
{
    public String name();

    public String longName() default "";
}
