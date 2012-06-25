package de.cubeisland.cubeengine.core.command.annotation;

/**
 *
 * @author Phillip Schichtel
 */
public @interface Flag
{
    public String name();
    public String longName() default "";
}
