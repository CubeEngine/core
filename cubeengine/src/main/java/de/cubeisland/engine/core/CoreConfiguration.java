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

import de.cubeisland.engine.configuration.Section;
import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.core.util.time.Duration;
import de.cubeisland.engine.logging.LogLevel;

/**
 * This Configuration holds all basic settings for CubeEngine.
 * Changes in this configuration can/will affect all modules.
 */
public class CoreConfiguration extends YamlConfiguration
{
    @Comment("Sets the locale to choose by default.")
    public Locale defaultLocale = Locale.US;

    public CommandsSection commands;

    public class CommandsSection implements Section
    {
        @Comment("The maximum number of similar commands to offer when more than one command matched a mistyped command.")
        public int maxCorrectionOffers = 5;

        @Comment("The maximum number of offers given for a tab completion request (pressing tab).")
        public int maxTabCompleteOffers = 5;
    }

    public ExecutorSection executor;

    public class ExecutorSection implements Section
    {
        @Comment("The maximum amount of threads used by the executor at one time")
        public int threads = 2;

        @Comment("The time in seconds until timeout after shutdown")
        public int terminate = 10;
    }

    public UsermanagerSection usermanager;

    public class UsermanagerSection implements Section
    {
        @Comment("How often the UserManager should unload offline Players")
        public int cleanup = 10;

        @Comment("After which time should CubeEngine delete all of a users data from database")
        public Duration garbageCollection = new Duration(TimeUnit.DAYS.toMillis(90));

        @Comment("How many Ticks after disconnecting a user should stay in the user manager")
        public int keepInMemory = 300;

        @Comment("How many ticks after PlayerJoinEvent the AfterJoinEvent is fired")
        public long afterJoinEventDelay = 1;
    }

    public LoggingSection logging;

    public class LoggingSection implements Section
    {
        @Comment({"Logging into Console", "ALL > TRACE > DEBUG > INFO > WARN > ERROR > OFF"})
        public LogLevel consoleLevel = LogLevel.INFO;

        @Comment({"Logging to the main log file", "ALL > DEBUG > INFO > WARN > ERROR > OFF"})
        public LogLevel fileLevel = LogLevel.INFO;

        @Comment("Zip all old logs to zip archives")
        public boolean archiveLogs = true;

        @Comment("Whether to log commands executed by players.")
        public boolean logCommands = false;
    }

    @Comment("Whether to enable the Web API server")
    public boolean useWebapi = false;

    public SecuritySection security;

    public class SecuritySection implements Section
    {
        @Comment("Enable fail2ban on login")
        public boolean fail2ban = true;

        @Comment("Ban duration on fail2ban")
        public int banDuration = 10;
    }

    @Override
    public String[] head()
    {
        return new String[] {
                "This is the CubeEngine CoreConfiguration.",
                "Changes here can affect every CubeEngine-Module"
        };
    }
}
