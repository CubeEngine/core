package de.cubeisland.cubeengine.core.webapi.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Phillip Schichtel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller
{
    public String name();

    public boolean authenticate() default true;

    public String serializer() default "plain";

    public boolean unknownToDefault() default false;
}