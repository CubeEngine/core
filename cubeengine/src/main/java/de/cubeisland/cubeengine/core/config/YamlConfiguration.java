package de.cubeisland.cubeengine.core.config;

import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.codec.MultiConfigurationCodec;

/**
 * An Configuration using YAML
 */
@Codec("yml")
public abstract class YamlConfiguration extends MultiConfiguration<MultiConfigurationCodec>
{
}
