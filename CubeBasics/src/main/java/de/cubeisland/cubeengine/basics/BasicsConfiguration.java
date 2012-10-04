package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;

/**
 *
 * @author Anselm Brehme
 */
@Codec("yml")
public class BasicsConfiguration extends Configuration
{
    @Option("spawnmob.limit")
    public int spawnmobLimit = 20;
    @Option("remove.defaultradius")
    public int removeCmdDefaultRadius = 20;
}
