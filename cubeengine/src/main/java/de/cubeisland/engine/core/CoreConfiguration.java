/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.config.annotations.Codec;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.Option;

import ch.qos.logback.classic.Level;
import de.cubeisland.engine.core.util.time.Duration;

/**
 * This Configuration holds all basic settings for CubeEngine.
 * Changes in this configuration can/will affect all modules.
 */
@Codec("yml")
public class CoreConfiguration extends Configuration
{
    @Option("default-locale")
    @Comment("Sets the locale to choose by default.")
    public Locale defaultLocale = Locale.US;

    @Option("commands.max-correction-offers")
    @Comment("The maximum number of similar commands to offer when more than one command matched a mistyped command.")
    public int commandOffers = 5;

    @Option("commands.max-tab-completion-offers")
    @Comment("The maximum number of offers given for a tab completion request (pressing tab).")
    public int commandTabCompleteOffers = 5;

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
    public Duration userManagerCleanupDatabase = new Duration(TimeUnit.DAYS.toMillis(90));

    @Option("usermanager.keep-in-memory")
    @Comment("How many Ticks after disconnecting a user should stay in the user manager")
    public Integer userManagerKeepUserLoaded = 300;

    @Option("logging.console-Level")
    @Comment("Logging into Console \nALL > TRACE > DEBUG > INFO > WARN > ERROR > OFF")
    public Level loggingConsoleLevel = Level.INFO;

    @Option("logging.file-Level")
    @Comment("Logging to the main log file \nALL > DEBUG > INFO > WARN > ERROR > OFF")
    public Level loggingFileLevel = Level.INFO;

    @Option("logging.archive-logs")
    @Comment("Zip all old logs to zip archives")
    public boolean loggingArchiveLogs = true;

    @Option("logging.log-commands")
    @Comment("Whether to log commands executed by players.")
    public boolean logCommands = false;

    @Option("after-join-event-delay")
    @Comment("How many ticks after PlayerJoinEvent the AfterJoinEvent is fired")
    public long afterJoinEventDelay = 1;

    @Option("use-webapi")
    @Comment("Whether to enable the Web API server")
    public boolean userWebapi = false;

    @Option("security.fail2ban")
    @Comment("Enable fail2ban on login")
    public boolean fail2ban = true;

    @Option("security.ban-duration")
    @Comment("Ban duration on fail2ban")
    public int banDuration = 10;

    @Override
    public String[] head()
    {
        return new String[] {
                "This is the CubeEngine CoreConfiguration.",
                "Changes here can affect every CubeEngine-Module"
        };
    }
}
