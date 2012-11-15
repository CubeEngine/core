package de.cubeisland.cubeengine.core.webapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static de.cubeisland.cubeengine.core.webapi.RequestMethod.GET;

/**
 *
 * @author Phillip Schichtel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action
{
    public String route() default "";

    public boolean auth() default true;

    public String[] parameters() default {};
    
    public RequestMethod[] methods() default GET;
}