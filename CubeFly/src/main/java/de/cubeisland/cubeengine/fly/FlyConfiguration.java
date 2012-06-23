package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Option;

/**
 *
 * @author Faithcaio
 */
public class FlyConfiguration extends Configuration
{
    @Option("debug")
    public boolean debugMode = false;
    @Option("mode.flycommand")
    public boolean flycommand = true; //if false fly command does not work
    @Option("mode.flyfeather")
    public boolean flyfeather = true; //if false feather fly does not work
}