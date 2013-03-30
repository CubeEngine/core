package de.cubeisland.cubeengine.travel;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
@DefaultConfig
public class TravelConfig extends Configuration
{

    @Comment("If users should be able to have multiple homes")
    @Option("homes.multiple-homes")
    public boolean multipleHomes = true;

}
