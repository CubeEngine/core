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
package org.cubeengine.service.logging;

import de.cubeisland.engine.logscribe.LogLevel;
import de.cubeisland.engine.reflect.annotations.Comment;
import de.cubeisland.engine.reflect.codec.yaml.ReflectedYaml;

public class LoggerConfiguration extends ReflectedYaml
{
    @Comment({"Logging into Console", "ALL > TRACE > DEBUG > INFO > WARN > ERROR > NONE"})
    public LogLevel consoleLevel = LogLevel.INFO;

    @Comment({"Logging to the main log file", "ALL > DEBUG > INFO > WARN > ERROR > NONE"})
    public LogLevel fileLevel = LogLevel.INFO;

    @Comment("Zip all old logs to zip archives")
    public boolean archiveLogs = true;

    @Comment("Whether to log commands executed by players.")
    public boolean logCommands = false;
}
