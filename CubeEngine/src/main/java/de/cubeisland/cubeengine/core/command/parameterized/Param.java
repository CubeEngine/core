package de.cubeisland.cubeengine.core.command.parameterized;

public @interface Param
{
    String[] names();

    Class type() default String.class;

    boolean required() default false;
}
