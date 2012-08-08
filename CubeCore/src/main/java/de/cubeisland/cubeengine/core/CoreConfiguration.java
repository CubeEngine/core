package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Codec;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Comment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.annotations.Option;

/**
 *
 * @author Anselm Brehme
 */
@Codec("yml")
public class CoreConfiguration extends Configuration
{
    @Option("debug")
    @Comment("If enabled shows debug-messages")
    public boolean debugMode = false;
    
    @Option("defaultLanguage")
    @Comment("Sets the language to choose by default")
    public String defaultLanguage = "en_US";
}