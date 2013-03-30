package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
@DefaultConfig
public class FlyConfig extends Configuration
{
    @Option("enablemode.flyfeather")
    public boolean flyfeather = true; //if false feather fly does not work
}
