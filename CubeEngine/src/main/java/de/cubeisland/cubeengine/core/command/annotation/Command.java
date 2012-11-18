package de.cubeisland.cubeengine.core.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bukkit.permissions.PermissionDefault;

/**
 * Annotates a method as a command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command
{
    public String[] names() default {};

    public String desc();

    public String usage() default "";

    public int min() default 0;

    public int max() default -1;

    public boolean checkPerm() default true;

    public String permNode() default "";

    public PermissionDefault permDefault() default PermissionDefault.OP;

    public Flag[] flags() default {};

    public Param[] params() default {};
}
