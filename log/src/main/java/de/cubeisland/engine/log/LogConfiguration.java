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
package de.cubeisland.engine.log;

import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.configuration.Section;
import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import org.joda.time.Duration;

public class LogConfiguration extends YamlConfiguration
{
    @Comment("The maximum of logs that may be logged at once.")
    @Name("logging.batch-size")
    public int loggingBatchSize = 2000;
    @Comment("Shows log info in the console when logging at least that amount of logs at once")
    @Name("info.show-log-info")
    public int showLogInfoInConsole = 200;

    public Cleanup cleanup;
    public class Cleanup implements Section
    {
        @Comment("Logs from worlds that do no longer exist are removed")
        @Name("deleted-worlds")
        public boolean deletedWorlds = false;

        @Comment("Delete logs that are older than specified under old-logs.time")
        public OldLogs oldLogs;
        public class OldLogs implements Section
        {
            public boolean enable = true;
            public Duration time = new Duration(TimeUnit.DAYS.toMillis(70));
        }

        @Comment("How many logs may be deleted in a single query")
        public int steps = 10000;

        @Name("cleanup.delay")
        public Duration delay = new Duration(TimeUnit.DAYS.toMillis(1));
    }
}
