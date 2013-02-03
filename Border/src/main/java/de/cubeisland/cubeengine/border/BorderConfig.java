package de.cubeisland.cubeengine.border;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
@DefaultConfig
public class BorderConfig extends Configuration
{
    @Option("chunk-radius")
    public int radius = 30;

    @Option("square-area")
    @Comment("Whether the radius should define a square instead of a circle around the spawn point")
    public boolean square = false;

    @Option("allow-bypass")
    @Comment("Whether players can bypass the restriction with a permission")
    public boolean allowBypass = false;
}
