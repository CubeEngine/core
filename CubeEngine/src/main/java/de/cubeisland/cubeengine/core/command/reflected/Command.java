package de.cubeisland.cubeengine.core.command.reflected;

import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.permission.PermDefault;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command
{
    String[] names() default {};

    String desc();

    String usage() default "";

    int min() default 0;

    int max() default -1;

    boolean checkPerm() default true;

    String permNode() default "";

    PermDefault permDefault() default PermDefault.OP;

    Flag[] flags() default {};

    Param[] params() default {};

    boolean async() default false;

    boolean loggable() default true;
}
