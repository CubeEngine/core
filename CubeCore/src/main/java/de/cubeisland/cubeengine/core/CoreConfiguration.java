package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;

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
    
    @Option("executor.threads")
    @Comment("The maximum amount of threads used by the executor at one time")
    public Integer executorThreads = 2;
    
    @Option("executor.terminate")
    @Comment("The time in seconds until timeout after shutdown")
    public Integer executorTermination = 10;
    
    @Option("usermanager.cleanup")
    @Comment("How often the UserManager should unload offline Players")
    public Integer userManagerCleanup = 10;
}