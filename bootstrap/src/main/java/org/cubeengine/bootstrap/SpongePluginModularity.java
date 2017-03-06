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
package org.cubeengine.bootstrap;

import de.cubeisland.engine.modularity.core.BasicModularity;
import org.slf4j.Logger;

public class SpongePluginModularity extends BasicModularity
{

    private Logger pluginLogger;

    public SpongePluginModularity(org.slf4j.Logger pluginLogger)
    {
        this.pluginLogger = pluginLogger;
    }

    @Override
    public void log(String message)
    {
        this.pluginLogger.info(message);
    }

    @Override
    public void logError(String message, Throwable t)
    {
        this.pluginLogger.error(message, t);
    }
}
