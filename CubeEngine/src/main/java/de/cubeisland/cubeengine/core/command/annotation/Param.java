package de.cubeisland.cubeengine.core.command.annotation;

import de.cubeisland.cubeengine.core.command.Argument;
import de.cubeisland.cubeengine.core.command.args.StringArg;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is used to define named parameters in a command.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param
{
    public String[] names();

    public Class<? extends Argument<?>> type() default StringArg.class;
}