package de.cubeisland.cubeengine.core.command.annotation;

/**
 *
 * @author Phillip Schichtel
 */
public @interface Param
{
    public String[] names() default {};
    public Class type();
    public boolean optional() default false;
}
