package de.cubeisland.cubeengine.core.command.reflected;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Alias
{
    String[] names();

    String[] parents() default {};

    String prefix() default "";

    String suffix() default "";
}
