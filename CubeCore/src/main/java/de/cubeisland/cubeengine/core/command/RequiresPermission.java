package de.cubeisland.cubeengine.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bukkit.permissions.PermissionDefault;

/**
 * Annotates the permission of a command
 *
 * @author Phillip Schichtel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresPermission
{
    public String value() default "";
    public PermissionDefault def() default PermissionDefault.OP;
    public boolean addParent() default true;
}
