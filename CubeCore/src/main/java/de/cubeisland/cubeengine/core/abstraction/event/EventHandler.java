package de.cubeisland.cubeengine.core.abstraction.event;

/**
 *
 * @author CodeInfection
 */
public @interface EventHandler
{
    public boolean ignoreCancelled() default false;
}
