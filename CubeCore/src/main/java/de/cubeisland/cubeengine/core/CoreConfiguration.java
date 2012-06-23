package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Comment;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Configuration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.Option;

/**
 *
 * @author Faithcaio
 */
public class CoreConfiguration extends Configuration
{
    @Comment("If enabled shows debug-messages")
    @Option("debug")
    public boolean debugMode = false;
    
    @Comment("Sets the language to choose by default")
    @Option("defaultLanguage")
    public String defaultLanguage = "en_US";
}
