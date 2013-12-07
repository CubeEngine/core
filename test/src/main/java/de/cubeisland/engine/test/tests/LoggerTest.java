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
package de.cubeisland.engine.test.tests;

import de.cubeisland.engine.core.CubeEngine;

public class LoggerTest extends Test
{
    private final de.cubeisland.engine.test.Test module;

    public LoggerTest(de.cubeisland.engine.test.Test module)
    {
        this.module = module;
    }
    
    @Override
    public void onEnable()
    {
        CubeEngine.getLog().trace("Trace log on core's logging");
        CubeEngine.getLog().debug("Debug log on core's logging");
        CubeEngine.getLog().info("Info log on core's logging");
        CubeEngine.getLog().warn("Warn log on core's logging");
        CubeEngine.getLog().error("Error log on core's logging");
        
        module.getLog().trace("Trace log on test's logging");
        module.getLog().debug("Debug log on test's logging");
        module.getLog().info("Info log on test's logging");
        module.getLog().warn("Warn log on test's logging");
        module.getLog().error("Error log on test's logging");

        Exception ex = new Exception("Nothing dangerous!");
        module.getLog().debug(ex, ex.getLocalizedMessage());
        this.setSuccess(true);
    }
}
