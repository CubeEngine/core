package de.cubeisland.cubeengine.core.command.reflected.readable;

import de.cubeisland.cubeengine.core.permission.PermDefault;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReadableCmd
{
    String[] names();

    String desc();

    String usage();

    String pattern();

    int patternFlags() default Pattern.CASE_INSENSITIVE;

    boolean async() default false;

    boolean loggable() default true;

    boolean checkPerm() default true;

    String permNode() default "";

    PermDefault permDefault() default PermDefault.OP;
}
