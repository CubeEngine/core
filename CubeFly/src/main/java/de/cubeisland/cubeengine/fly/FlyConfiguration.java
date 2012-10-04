package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;

/**
 *
 * @author Anselm Brehme
 */
@Codec("yml")
public class FlyConfiguration extends Configuration
{
    @Option("enablemode.flycommand")
    public boolean flycommand = true; //if false fly command does not work
    
    @Option("enablemode.flyfeather")
    public boolean flyfeather = true; //if false feather fly does not work
}