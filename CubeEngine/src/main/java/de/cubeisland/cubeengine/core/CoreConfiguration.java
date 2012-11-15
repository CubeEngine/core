package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.util.log.LogLevel;

/**
 * This Configuration holds all basic settings for CubeEngine.
 * Changes in this configuration can/will affect all modules.
 */
@Codec("yml")
@Revision(1)
public class CoreConfiguration extends Configuration
{
    @Option("debug")
    @Comment("If enabled shows debug-messages")
    public boolean  debugMode                  = false;
    @Option("default-Language")
    @Comment("Sets the language to choose by default.\nCurrently supported en_US de_DE fr_FR")
    public String   defaultLanguage            = "en_US";
    @Option("executor.threads")
    @Comment("The maximum amount of threads used by the executor at one time")
    public Integer  executorThreads            = 2;
    @Option("executor.terminate")
    @Comment("The time in seconds until timeout after shutdown")
    public Integer  executorTermination        = 10;
    @Option("usermanager.cleanup")
    @Comment("How often the UserManager should unload offline Players")
    public Integer  userManagerCleanup         = 10;
    @Option("usermanager.garbagecollection")
    @Comment("After which time should CubeEngine delete all of a users data from database")
    public String   userManagerCleanupDatabase = "3M";
    @Option("usermanager.keepInMemory")
    @Comment("How many Ticks after disconecting a user should stay in the usermanager")
    public Integer  userManagerKeepUserLoaded  = 300;
    @Option("database")
    @Comment("Currently available: mysql")
    public String   database                   = "mysql";
    @Option("logging.Level")
    @Comment("Logging into Console \nALL > DEBUG > INFO > NOTICE > WARNING > ERROR > OFF")
    public LogLevel loggingLevel               = LogLevel.NOTICE;
    @Option("after-join-event-delay")
    @Comment("How many ticks after PlayerJoinEvent the AfterJoingEvent is fired")
    public long     afterJoinEventDelay        = 1;

    @Override
    public String[] head()
    {
        return new String[] {
                "This is the CubeEngine CoreConfiguration.",
                "Changes here can affect every CubeEngine-Module"
        };
    }

    @Override
    public void onLoaded()
    {
        this.defaultLanguage = I18n.normalizeLanguage(this.defaultLanguage);
    }
}
