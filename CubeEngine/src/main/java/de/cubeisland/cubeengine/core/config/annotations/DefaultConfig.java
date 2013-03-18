package de.cubeisland.cubeengine.core.config.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Configuration with this annotation will be named "config" by default and automatically loaded into the fields in the module
 * before calling onEnable
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface DefaultConfig
{
    public String name() default "config";
}
