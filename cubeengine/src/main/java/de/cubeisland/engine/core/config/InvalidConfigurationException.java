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
package de.cubeisland.engine.core.config;

import java.lang.reflect.Field;
import java.nio.file.Path;

/**
 * This exception is thrown when a configuration is invalid.
 */
public class InvalidConfigurationException extends RuntimeException
{
    private static final long serialVersionUID = -492268712863444129L;

    public InvalidConfigurationException(String message)
    {
        super(message);
    }

    public InvalidConfigurationException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public static InvalidConfigurationException of(String msg, Path file, String path, Class<? extends Configuration> clazz, Field field , Throwable t)
    {
        if (file != null)
        {
            msg += "\nFile: " + file.toAbsolutePath();
        }
        msg += "\nPath: " + path;
        msg += "\nConfig: " + clazz.toString();
        msg += "\nField: " + field.getName();
        return new InvalidConfigurationException(msg,t);
    }
}
