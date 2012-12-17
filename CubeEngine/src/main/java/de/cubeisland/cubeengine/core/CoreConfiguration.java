package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.util.log.CubeLevel;
import de.cubeisland.cubeengine.core.util.log.LogLevel;

/**
 * This Configuration holds all basic settings for CubeEngine.
 * Changes in this configuration can/will affect all modules.
 */
@Codec("yml")
@Revision(1)
public class CoreConfiguration extends Configuration
{
    @Option("default-Language")
    @Comment("Sets the language to choose by default.\nCurrently supported en_US de_DE fr_FR")
    public String defaultLanguage = "en_US";

    @Option("command-offers")
    @Comment("The maximum number of similar commands to offer when more than one command matched a mistyped command.")
    public int commandOffers = 5;

    @Option("executor.threads")
    @Comment("The maximum amount of threads used by the executor at one time")
    public Integer executorThreads = 2;

    @Option("executor.terminate")
    @Comment("The time in seconds until timeout after shutdown")
    public Integer executorTermination = 10;

    @Option("usermanager.cleanup")
    @Comment("How often the UserManager should unload offline Players")
    public Integer userManagerCleanup = 10;

    @Option("usermanager.garbage-collection")
    @Comment("After which time should CubeEngine delete all of a users data from database")
    public String userManagerCleanupDatabase = "3M";

    @Option("usermanager.keep-in-memory")
    @Comment("How many Ticks after disconnecting a user should stay in the user manager")
    public Integer userManagerKeepUserLoaded = 300;

    @Option("database")
    @Comment("Currently available: mysql")
    public String database = "mysql";

    @Option("logging.Level")
    @Comment("Logging into Console \nALL > DEBUG > INFO > NOTICE > WARNING > ERROR > OFF")
    public CubeLevel loggingLevel = LogLevel.NOTICE;

    @Option("logging.log-commands")
    @Comment("Whether to log commands executed by players.")
    public boolean logCommands = false;

    @Option("logging.log-color-codes")
    @Comment("Whether to keep color codes in the log file.")
    public boolean logColorCodes = false;

    @Option("after-join-event-delay")
    @Comment("How many ticks after PlayerJoinEvent the AfterJoinEvent is fired")
    public long afterJoinEventDelay = 1;

    @Option("use-webapi")
    @Comment("Whether to enable the Web API server")
    public boolean userWebapi = false;

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
