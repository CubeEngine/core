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
package de.cubeisland.engine.core.logging;

import de.cubeisland.engine.core.module.ModuleInfo;

public interface LogFactory
{
    /**
     * Get or create the logging for the core
     *
     * @return The logging for the core
     */
    Log getCoreLog();

    /**
     * Get or create a logging for the module
     * @param module The module
     * @return The logging for the module
     */
    Log createModuleLog(ModuleInfo module);

    long getBirthTime();

    Log getLog(String name);

    void shutdown();

    void shutdown(Log log);
}
