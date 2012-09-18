package de.cubeisland.cubeengine.cubefun;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;

/**
 *
 * @author Wolfi
 */
@Codec("yml")
class FunConfiguration extends Configuration
{
    @Option("command.lightning")
    public boolean lightning = true;
}
