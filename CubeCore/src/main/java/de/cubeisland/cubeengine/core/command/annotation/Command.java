package de.cubeisland.cubeengine.core.command.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a method as a command
 *
 * @author Phillip Schichtel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command
{
    public String name() default "";
    public String[] aliases() default {};
    public int min() default -1;
    public int max() default -1;
    public String desc();
    public boolean permission() default true;
    public String usage() default "auto";
    public Flag[] flags() default {};
}
