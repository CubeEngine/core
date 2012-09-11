package de.cubeisland.cubeengine.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Phillip Schichtel
 */
@Retention(RetentionPolicy.SOURCE)
public @interface BukkitDependend
{
    public String value();
}