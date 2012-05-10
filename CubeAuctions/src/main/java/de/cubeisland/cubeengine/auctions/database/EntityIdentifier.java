package de.cubeisland.cubeengine.auctions.database;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author CodeInfection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EntityIdentifier
{
    public String name() default "";
    public String toDBString() default "";
    public String toDBCreateString() default "";
}
