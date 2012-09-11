package de.cubeisland.cubeengine.core.command.annotation;

/**
 *
 * @author Phillip Schichtel
 */
public @interface Alias
{
    public String[] names();
    public String[] parentPath() default {};
}
