package de.cubeisland.cubeengine.core.command.parameterized;

public @interface Flag
{
    String name();

    String longName() default "";
}
