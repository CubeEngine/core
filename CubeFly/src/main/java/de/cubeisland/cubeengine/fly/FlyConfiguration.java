package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Codec;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;

/**
 *
 * @author Faithcaio
 */
@Codec("yml")
public class FlyConfiguration extends Configuration
{
    @Option("mode.flycommand")
    public boolean flycommand = true; //if false fly command does not work
    
    @Option("mode.flyfeather")
    public boolean flyfeather = true; //if false feather fly does not work
}